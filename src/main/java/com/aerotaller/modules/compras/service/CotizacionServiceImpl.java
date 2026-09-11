package com.aerotaller.modules.compras.service;

import com.aerotaller.modelos.*;
import com.aerotaller.modules.compras.dto.ComparativoCotizacionResponse;
import com.aerotaller.modules.compras.dto.CotizacionRequest;
import com.aerotaller.modules.compras.dto.CotizacionResponse;
import com.aerotaller.modules.compras.repository.CotizacionRepository;
import com.aerotaller.modules.compras.repository.RequisicionCompraRepository;
import com.aerotaller.modules.compras.repository.SolicitudCotizacionRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CotizacionServiceImpl implements CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final SolicitudCotizacionRepository solicitudCotizacionRepository;
    private final RequisicionCompraRepository requisicionRepository;
    private final EntityManager entityManager;

    public CotizacionServiceImpl(CotizacionRepository cotizacionRepository,
                                 SolicitudCotizacionRepository solicitudCotizacionRepository,
                                 RequisicionCompraRepository requisicionRepository,
                                 EntityManager entityManager) {
        this.cotizacionRepository = cotizacionRepository;
        this.solicitudCotizacionRepository = solicitudCotizacionRepository;
        this.requisicionRepository = requisicionRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public CotizacionResponse registrarCotizacion(CotizacionRequest request) {
        SolicitudCotizacion solicitud = solicitudCotizacionRepository.findById(request.getIdSolicitudCot())
                .orElseThrow(() -> new RuntimeException("Solicitud de Cotización no encontrada"));
        Proveedor proveedor = entityManager.getReference(Proveedor.class, request.getIdProveedor());

        BigDecimal tipoCambio = (request.getTipoCambio() != null && request.getTipoCambio().compareTo(BigDecimal.ZERO) > 0)
                ? request.getTipoCambio()
                : BigDecimal.ONE;

        Cotizacion cotizacion = Cotizacion.builder()
                .solicitudCotizacion(solicitud)
                .proveedor(proveedor)
                .folioCotizacionProveedor(request.getFolioCotizacionProveedor())
                .moneda(request.getMoneda() != null ? request.getMoneda() : "MXN")
                .tipoCambio(tipoCambio)
                .rutaPdf(request.getRutaPdf())
                .esGanadora(false)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<DetalleCotizacion> detalles = new ArrayList<>();

        for (CotizacionRequest.DetalleCotizacionDTO itemDto : request.getDetalles()) {
            DetalleRequisicion dReq = entityManager.getReference(DetalleRequisicion.class, itemDto.getIdDetalleReq());
            BigDecimal importe = itemDto.getPrecioUnitario().multiply(dReq.getCantidadSolicitada()).setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(importe);

            DetalleCotizacion detalle = DetalleCotizacion.builder()
                    .cotizacion(cotizacion)
                    .detalleRequisicion(dReq)
                    .precioUnitario(itemDto.getPrecioUnitario())
                    .importe(importe)
                    .tiempoEntregaDias(itemDto.getTiempoEntregaDias())
                    .build();
            detalles.add(detalle);
        }

        BigDecimal iva = subtotal.multiply(new BigDecimal("0.16")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(iva);

        cotizacion.setSubtotal(subtotal);
        cotizacion.setIva(iva);
        cotizacion.setTotal(total);
        cotizacion.setDetalles(detalles);

        Cotizacion guardada = cotizacionRepository.save(cotizacion);

        return CotizacionResponse.builder()
                .idCotizacion(guardada.getIdCotizacion())
                .idSolicitudCot(solicitud.getIdSolicitudCot())
                .idProveedor(proveedor.getIdProveedor())
                .nombreProveedor(proveedor.getNombre())
                .folioCotizacionProveedor(guardada.getFolioCotizacionProveedor())
                .fechaRecepcion(guardada.getFechaRecepcion())
                .moneda(guardada.getMoneda())
                .tipoCambio(guardada.getTipoCambio())
                .subtotal(guardada.getSubtotal())
                .iva(guardada.getIva())
                .total(guardada.getTotal())
                .rutaPdf(guardada.getRutaPdf())
                .esGanadora(guardada.getEsGanadora())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ComparativoCotizacionResponse obtenerComparativoPorRequisicion(Integer idRequisicion) {
        RequisicionCompra requisicion = requisicionRepository.findById(idRequisicion)
                .orElseThrow(() -> new RuntimeException("Requisición no encontrada: " + idRequisicion));

        List<Cotizacion> cotizaciones = cotizacionRepository.findByRequisicionId(idRequisicion);

        List<ComparativoCotizacionResponse.ProveedorColumnaDTO> columnasProveedores = cotizaciones.stream()
                .map(c -> ComparativoCotizacionResponse.ProveedorColumnaDTO.builder()
                        .idCotizacion(c.getIdCotizacion())
                        .idProveedor(c.getProveedor().getIdProveedor())
                        .nombreProveedor(c.getProveedor().getNombre())
                        .moneda(c.getMoneda())
                        .totalCotizado(c.getTotal())
                        .esGanadora(c.getEsGanadora())
                        .build())
                .collect(Collectors.toList());

        List<ComparativoCotizacionResponse.FilaComparativaDTO> filas = requisicion.getDetalles().stream().map(dReq -> {
            String desc = (dReq.getArticulo() != null) ? dReq.getArticulo().getDescripcion() : dReq.getDescripcionLibre();
            String numParte = (dReq.getArticulo() != null) ? dReq.getArticulo().getNoParte() : dReq.getNumeroParteLibre();

            List<ComparativoCotizacionResponse.OfertaProveedorDTO> ofertas = new ArrayList<>();
            for (Cotizacion cot : cotizaciones) {
                cot.getDetalles().stream()
                        .filter(dCot -> dCot.getDetalleRequisicion().getIdDetalleReq().equals(dReq.getIdDetalleReq()))
                        .findFirst()
                        .ifPresent(dCot -> ofertas.add(ComparativoCotizacionResponse.OfertaProveedorDTO.builder()
                                .idCotizacion(cot.getIdCotizacion())
                                .idProveedor(cot.getProveedor().getIdProveedor())
                                .precioUnitario(dCot.getPrecioUnitario())
                                .importe(dCot.getImporte())
                                .tiempoEntregaDias(dCot.getTiempoEntregaDias())
                                .build()));
            }

            return ComparativoCotizacionResponse.FilaComparativaDTO.builder()
                    .idDetalleReq(dReq.getIdDetalleReq())
                    .descripcionArticulo(desc)
                    .numeroParte(numParte)
                    .cantidad(dReq.getCantidadSolicitada())
                    .unidadMedida(dReq.getUnidadMedida().getNombre())
                    .ofertas(ofertas)
                    .build();
        }).collect(Collectors.toList());

        return ComparativoCotizacionResponse.builder()
                .idRequisicion(requisicion.getIdRequisicion())
                .folioRequisicion(requisicion.getFolio())
                .proveedores(columnasProveedores)
                .filas(filas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CotizacionResponse> listarPorRequisicion(Integer idRequisicion) {
        return cotizacionRepository.findByRequisicionId(idRequisicion).stream()
                .map(c -> CotizacionResponse.builder()
                        .idCotizacion(c.getIdCotizacion())
                        .idSolicitudCot(c.getSolicitudCotizacion().getIdSolicitudCot())
                        .idProveedor(c.getProveedor().getIdProveedor())
                        .nombreProveedor(c.getProveedor().getNombre())
                        .folioCotizacionProveedor(c.getFolioCotizacionProveedor())
                        .fechaRecepcion(c.getFechaRecepcion())
                        .moneda(c.getMoneda())
                        .tipoCambio(c.getTipoCambio())
                        .subtotal(c.getSubtotal())
                        .iva(c.getIva())
                        .total(c.getTotal())
                        .rutaPdf(c.getRutaPdf())
                        .esGanadora(c.getEsGanadora())
                        .build())
                .collect(Collectors.toList());
    }
}