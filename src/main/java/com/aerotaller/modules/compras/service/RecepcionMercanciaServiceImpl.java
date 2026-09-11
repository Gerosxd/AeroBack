package com.aerotaller.modules.compras.service;

import com.aerotaller.modelos.*;
import com.aerotaller.modules.compras.dto.RecepcionMercanciaRequest;
import com.aerotaller.modules.compras.dto.RecepcionMercanciaResponse;
import com.aerotaller.modules.compras.repository.OrdenCompraRepository;
import com.aerotaller.modules.compras.repository.RecepcionMercanciaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecepcionMercanciaServiceImpl implements RecepcionMercanciaService {

    private final RecepcionMercanciaRepository recepcionRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final EntityManager entityManager;

    public RecepcionMercanciaServiceImpl(RecepcionMercanciaRepository recepcionRepository,
                                         OrdenCompraRepository ordenCompraRepository,
                                         EntityManager entityManager) {
        this.recepcionRepository = recepcionRepository;
        this.ordenCompraRepository = ordenCompraRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public RecepcionMercanciaResponse registrarRecepcion(RecepcionMercanciaRequest request) {
        OrdenCompra ordenCompra = ordenCompraRepository.findById(request.getIdOrdenCompra())
                .orElseThrow(() -> new RuntimeException("Orden de Compra no encontrada: " + request.getIdOrdenCompra()));

        Usuario usuarioRecibe = entityManager.getReference(Usuario.class, request.getIdUsuarioRecibe());
        EntradaArticulo entrada = (request.getIdEntradaArticulo() != null)
                ? entityManager.getReference(EntradaArticulo.class, request.getIdEntradaArticulo())
                : null;

        RecepcionMercancia recepcion = RecepcionMercancia.builder()
                .folio(request.getFolio())
                .ordenCompra(ordenCompra)
                .usuarioRecibe(usuarioRecibe)
                .entradaArticulo(entrada)
                .rutaPdf(request.getRutaPdf())
                .observaciones(request.getObservaciones())
                .build();

        List<DetalleRecepcionMercancia> detalles = new ArrayList<>();
        for (RecepcionMercanciaRequest.DetalleRecepcionRequest pReq : request.getPartidas()) {
            DetalleOrdenCompra doc = entityManager.getReference(DetalleOrdenCompra.class, pReq.getIdDetalleOc());
            DetalleRecepcionMercancia dRec = DetalleRecepcionMercancia.builder()
                    .recepcion(recepcion)
                    .detalleOrdenCompra(doc)
                    .cantidadRecibida(pReq.getCantidadRecibida())
                    .observacionesItem(pReq.getObservacionesItem())
                    .build();
            detalles.add(dRec);
        }
        recepcion.setDetalles(detalles);
        RecepcionMercancia guardada = recepcionRepository.save(recepcion);

        // Actualizar estado de la Orden de Compra según entregas acumuladas
        List<RecepcionMercancia> todasLasRecepciones = recepcionRepository.findByOrdenCompraIdOrdenCompra(ordenCompra.getIdOrdenCompra());
        boolean completamenteRecibida = true;

        for (DetalleOrdenCompra itemOC : ordenCompra.getDetalles()) {
            BigDecimal totalRecibidoItem = todasLasRecepciones.stream()
                    .flatMap(r -> r.getDetalles().stream())
                    .filter(d -> d.getDetalleOrdenCompra().getIdDetalleOc().equals(itemOC.getIdDetalleOc()))
                    .map(DetalleRecepcionMercancia::getCantidadRecibida)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalRecibidoItem.compareTo(itemOC.getCantidadOrdenada()) < 0) {
                completamenteRecibida = false;
                break;
            }
        }

        String codigoEstado = completamenteRecibida ? "RECEPCION_COMPLETA" : "RECEPCION_PARCIAL";
        EstadoCompra nuevoEstado = entityManager.createQuery(
                        "SELECT e FROM EstadoCompra e WHERE e.codigo = :cod", EstadoCompra.class)
                .setParameter("cod", codigoEstado)
                .getSingleResult();
        ordenCompra.setEstado(nuevoEstado);
        ordenCompraRepository.save(ordenCompra);

        return construirResponse(guardada, ordenCompra);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecepcionMercanciaResponse> listarPorOrdenCompra(Integer idOrdenCompra) {
        OrdenCompra oc = ordenCompraRepository.findById(idOrdenCompra)
                .orElseThrow(() -> new RuntimeException("Orden de Compra no encontrada: " + idOrdenCompra));

        return recepcionRepository.findByOrdenCompraIdOrdenCompra(idOrdenCompra).stream()
                .map(rec -> construirResponse(rec, oc))
                .collect(Collectors.toList());
    }

    private RecepcionMercanciaResponse construirResponse(RecepcionMercancia rec, OrdenCompra oc) {
        List<RecepcionMercancia> historial = recepcionRepository.findByOrdenCompraIdOrdenCompra(oc.getIdOrdenCompra());

        List<RecepcionMercanciaResponse.DetalleRecepcionResponse> itemsResponse = rec.getDetalles().stream().map(d -> {
            DetalleOrdenCompra doc = d.getDetalleOrdenCompra();
            String desc = (doc.getDetalleRequisicion().getArticulo() != null)
                    ? doc.getDetalleRequisicion().getArticulo().getDescripcion()
                    : doc.getDetalleRequisicion().getDescripcionLibre();

            BigDecimal acumulado = historial.stream()
                    .flatMap(h -> h.getDetalles().stream())
                    .filter(hItem -> hItem.getDetalleOrdenCompra().getIdDetalleOc().equals(doc.getIdDetalleOc()))
                    .map(DetalleRecepcionMercancia::getCantidadRecibida)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal saldo = doc.getCantidadOrdenada().subtract(acumulado);

            return RecepcionMercanciaResponse.DetalleRecepcionResponse.builder()
                    .idDetalleRecepcion(d.getIdDetalleRecepcion())
                    .idDetalleOc(doc.getIdDetalleOc())
                    .descripcionArticulo(desc)
                    .cantidadOrdenada(doc.getCantidadOrdenada())
                    .cantidadRecibidaAhora(d.getCantidadRecibida())
                    .totalRecibidoAcumulado(acumulado)
                    .saldoPendiente(saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo)
                    .observacionesItem(d.getObservacionesItem())
                    .build();
        }).collect(Collectors.toList());

        return RecepcionMercanciaResponse.builder()
                .idRecepcion(rec.getIdRecepcion())
                .folio(rec.getFolio())
                .idOrdenCompra(oc.getIdOrdenCompra())
                .folioOrdenCompra(oc.getFolio())
                .estadoOrdenResultante(oc.getEstado().getCodigo())
                .fechaRecepcion(rec.getFechaRecepcion())
                .usuarioRecibe(rec.getUsuarioRecibe().getNombre())
                .observaciones(rec.getObservaciones())
                .partidas(itemsResponse)
                .build();
    }
}
