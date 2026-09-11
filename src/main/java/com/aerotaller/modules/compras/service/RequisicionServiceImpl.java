package com.aerotaller.modules.compras.service;

import com.aerotaller.modelos.*;
import com.aerotaller.modules.compras.dto.DetalleRequisicionDTO;
import com.aerotaller.modules.compras.dto.RequisicionRequest;
import com.aerotaller.modules.compras.dto.RequisicionResponse;
import com.aerotaller.modules.compras.repository.RequisicionCompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequisicionServiceImpl implements RequisicionService {

    private final RequisicionCompraRepository requisicionRepository;
    private final EntityManager entityManager;

    public RequisicionServiceImpl(RequisicionCompraRepository requisicionRepository, EntityManager entityManager) {
        this.requisicionRepository = requisicionRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public RequisicionResponse crear(RequisicionRequest request) {
        Usuario usuario = entityManager.getReference(Usuario.class, request.getIdUsuarioSolicita());
        EstadoCompra estadoPendiente = entityManager.createQuery(
                        "SELECT e FROM EstadoCompra e WHERE e.codigo = 'PENDIENTE'", EstadoCompra.class)
                .getSingleResult();

        RequisicionCompra req = RequisicionCompra.builder()
                .folio(request.getFolio())
                .usuarioSolicita(usuario)
                .estado(estadoPendiente)
                .observaciones(request.getObservaciones())
                .build();

        List<DetalleRequisicion> detalles = request.getDetalles().stream().map(d -> {
            Articulo art = (d.getIdArticulo() != null) ? entityManager.getReference(Articulo.class, d.getIdArticulo()) : null;
            UnidadMedida um = entityManager.getReference(UnidadMedida.class, d.getIdUnidadMedida());

            return DetalleRequisicion.builder()
                    .requisicion(req)
                    .articulo(art)
                    .descripcionLibre(d.getDescripcionLibre())
                    .numeroParteLibre(d.getNumeroParteLibre())
                    .unidadMedida(um)
                    .cantidadSolicitada(d.getCantidadSolicitada())
                    .build();
        }).collect(Collectors.toList());

        req.setDetalles(detalles);
        RequisicionCompra guardada = requisicionRepository.save(req);
        return mapearAResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public RequisicionResponse obtenerPorId(Integer id) {
        RequisicionCompra req = requisicionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisición no encontrada con ID: " + id));
        return mapearAResponse(req);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequisicionResponse> listarTodas() {
        return requisicionRepository.findAll().stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    private RequisicionResponse mapearAResponse(RequisicionCompra req) {
        List<DetalleRequisicionDTO> detallesDTO = req.getDetalles().stream().map(d -> {
            DetalleRequisicionDTO dto = new DetalleRequisicionDTO();
            dto.setIdDetalleReq(d.getIdDetalleReq());
            if (d.getArticulo() != null) {
                dto.setIdArticulo(d.getArticulo().getIdArticulo());
            }
            dto.setDescripcionLibre(d.getDescripcionLibre());
            dto.setNumeroParteLibre(d.getNumeroParteLibre());
            dto.setIdUnidadMedida(d.getUnidadMedida().getIdUnidad());
            dto.setCantidadSolicitada(d.getCantidadSolicitada());
            return dto;
        }).collect(Collectors.toList());

        return RequisicionResponse.builder()
                .idRequisicion(req.getIdRequisicion())
                .folio(req.getFolio())
                .idUsuarioSolicita(req.getUsuarioSolicita().getIdUsuario())
                .estado(req.getEstado().getCodigo())
                .fechaSolicitud(req.getFechaSolicitud())
                .observaciones(req.getObservaciones())
                .detalles(detallesDTO)
                .build();
    }
}