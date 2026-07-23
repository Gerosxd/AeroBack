package com.aerotaller.modules.salidaArt.service;

import com.aerotaller.modelos.Articulo;
import com.aerotaller.modelos.DetalleSalidaArt;
import com.aerotaller.modelos.SalidaArt;
import com.aerotaller.modules.articulo.repository.ArticuloRepository;
import com.aerotaller.modules.detalleSalidaArt.dto.CreateDetalleSalidaArtRequest;
import com.aerotaller.modules.detalleSalidaArt.repository.DetalleSalidaArtRepository;
import com.aerotaller.modules.salidaArt.dto.ArticuloSalidaResponse;
import com.aerotaller.modules.salidaArt.dto.CreateSalidaArtRequest;
import com.aerotaller.modules.salidaArt.dto.SalidaArtDetalleResponse;
import com.aerotaller.modules.salidaArt.dto.SalidaArtResponse;
import com.aerotaller.modules.salidaArt.repository.SalidaArtRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalidaArtServiceImpl implements SalidaArtService {

    private final SalidaArtRepository salidaRepository;
    private final DetalleSalidaArtRepository detalleSalidaArtRepository;
    private final ArticuloRepository articuloRepository;

    // Constructor para Inyección de Dependencias
    public SalidaArtServiceImpl(SalidaArtRepository salidaRepository,
                                DetalleSalidaArtRepository detalleSalidaArtRepository,
                                ArticuloRepository articuloRepository) {
        this.salidaRepository = salidaRepository;
        this.detalleSalidaArtRepository = detalleSalidaArtRepository;
        this.articuloRepository = articuloRepository;
    }

    @Override
    @Transactional
    public SalidaArtDetalleResponse crearSalida(CreateSalidaArtRequest request) {
        SalidaArt salida = new SalidaArt();
        salida.setNoSalida(request.getNoSalida());
        salida.setFecha(LocalDate.now());
        salida.setReferencia(request.getReferencia());
        salida.setDestinatario(request.getDestinatario());
        salida.setDireccionDestinatario(request.getDireccionDestinatario());
        salida.setEncargadoAlmacen(request.getEncargadoAlmacen());
        salida.setTraslada(request.getTraslada());
        salida.setRecibe(request.getRecibe());

        SalidaArt salidaGuardada = salidaRepository.save(salida);

        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new RuntimeException("La salida debe incluir al menos un artículo.");
        }

        for (CreateDetalleSalidaArtRequest detalleReq : request.getDetalles()) {
            Articulo articulo = resolverArticulo(detalleReq);

            if (articulo.getStock() < detalleReq.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + articulo.getCodigo());
            }

            // Actualizar Stock
            articulo.setStock(articulo.getStock() - detalleReq.getCantidad());
            articuloRepository.save(articulo);

            // Guardar Detalle
            DetalleSalidaArt detalle = new DetalleSalidaArt();
            detalle.setSalidaArt(salidaGuardada);
            detalle.setArticulo(articulo);
            detalle.setCantidad(detalleReq.getCantidad());
            detalle.setObservaciones(detalleReq.getObservaciones());
            detalleSalidaArtRepository.save(detalle);
        }

        return obtenerDetalle(salidaGuardada.getIdSalida());
    }

    @Override
    public List<SalidaArtResponse> listarTodas() {
        return salidaRepository.findAll().stream()
                .map(salida -> {
                    SalidaArtResponse res = new SalidaArtResponse();
                    res.setIdSalida(salida.getIdSalida());
                    res.setNoSalida(salida.getNoSalida());
                    res.setFecha(salida.getFecha());
                    res.setReferencia(salida.getReferencia());
                    res.setDestinatario(salida.getDestinatario());
                    res.setDireccionDestinatario(salida.getDireccionDestinatario());
                    // Importante para tu tabla de Vue
                    res.setTotalArticulos(salida.getDetalles() != null ? salida.getDetalles().size() : 0);
                    return res;
                }).collect(Collectors.toList());
    }

    @Override
    public SalidaArtResponse obtenerPorId(Integer id) {
        SalidaArt salida = salidaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salida no encontrada"));

        SalidaArtResponse res = new SalidaArtResponse();
        res.setIdSalida(salida.getIdSalida());
        res.setNoSalida(salida.getNoSalida());
        res.setFecha(salida.getFecha());
        res.setReferencia(salida.getReferencia());
        return res;
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        SalidaArt salida = salidaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salida no encontrada"));

        // Devolver stock antes de borrar
        if (salida.getDetalles() != null) {
            for (DetalleSalidaArt detalle : salida.getDetalles()) {
                Articulo art = detalle.getArticulo();
                art.setStock(art.getStock() + detalle.getCantidad());
                articuloRepository.save(art);
            }
        }
        salidaRepository.delete(salida);
    }

    @Override
    @Transactional(readOnly = true) // Agregamos Transactional para evitar LazyInitializationException
    public SalidaArtDetalleResponse obtenerDetalle(Integer idSalida) {
        SalidaArt salida = salidaRepository.findById(idSalida)
                .orElseThrow(() -> new RuntimeException("Salida no encontrada"));

        SalidaArtDetalleResponse response = new SalidaArtDetalleResponse();
        response.setIdSalida(salida.getIdSalida());
        response.setNoSalida(salida.getNoSalida());
        response.setFecha(salida.getFecha());
        response.setReferencia(salida.getReferencia());
        response.setDestinatario(salida.getDestinatario());
        response.setDireccionDestinatario(salida.getDireccionDestinatario());
        response.setEncargadoAlmacen(salida.getEncargadoAlmacen());
        response.setTraslada(salida.getTraslada());
        response.setRecibe(salida.getRecibe());

        if (salida.getDetalles() != null && !salida.getDetalles().isEmpty()) {
            response.setArticulos(salida.getDetalles().stream().map(d -> {
                ArticuloSalidaResponse art = new ArticuloSalidaResponse();
                if (d.getArticulo() != null) {
                    // Sincronizado con el DTO 'codigo'
                    art.setCodigo(d.getArticulo().getCodigo());
                    art.setDescripcion(d.getArticulo().getDescripcion());
                    art.setNoSerie(d.getArticulo().getNoSerie());
                    art.setCondicion(d.getArticulo().getCondicion());
                }
                art.setCantidad(d.getCantidad());
                art.setObservaciones(d.getObservaciones());
                return art;
            }).collect(Collectors.toList()));
        } else {
            response.setArticulos(new ArrayList<>());
        }

        return response;
    }

    // Método privado auxiliar para resolver artículos
    private Articulo resolverArticulo(CreateDetalleSalidaArtRequest req) {
        if (req.getIdArticulo() != null) {
            return articuloRepository.findById(req.getIdArticulo())
                    .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
        }
        throw new RuntimeException("Debe proporcionar un ID de artículo válido");
    }
}