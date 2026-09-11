package com.aerotaller.modules.compras.service;

import com.aerotaller.modelos.*;
import com.aerotaller.modules.compras.dto.OrdenCompraRequest;
import com.aerotaller.modules.compras.dto.OrdenCompraResponse;
import com.aerotaller.modules.compras.repository.CotizacionRepository;
import com.aerotaller.modules.compras.repository.OrdenCompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrdenCompraServiceImpl implements OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final CotizacionRepository cotizacionRepository;
    private final EntityManager entityManager;

    public OrdenCompraServiceImpl(OrdenCompraRepository ordenCompraRepository,
                                  CotizacionRepository cotizacionRepository,
                                  EntityManager entityManager) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.cotizacionRepository = cotizacionRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public OrdenCompraResponse generarOrdenCompra(OrdenCompraRequest request) {
        Cotizacion cotizacion = cotizacionRepository.findById(request.getIdCotizacionGanadora())
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        cotizacion.setEsGanadora(true);

        Proveedor proveedor = entityManager.getReference(Proveedor.class, request.getIdProveedor());
        Almacen almacen = entityManager.getReference(Almacen.class, request.getIdAlmacenDestino());
        EstadoCompra estadoTransito = entityManager.createQuery(
                        "SELECT e FROM EstadoCompra e WHERE e.codigo = 'EN_TRANSITO'", EstadoCompra.class)
                .getSingleResult();

        OrdenCompra oc = OrdenCompra.builder()
                .folio(request.getFolio())
                .cotizacionGanadora(cotizacion)
                .proveedor(proveedor)
                .almacenDestino(almacen)
                .estado(estadoTransito)
                .fechaEntregaEstimada(request.getFechaEntregaEstimada())
                .moneda(request.getMoneda())
                .tipoCambio(request.getTipoCambio())
                .condicionesPago(request.getCondicionesPago())
                .subtotal(cotizacion.getSubtotal())
                .iva(cotizacion.getIva())
                .total(cotizacion.getTotal())
                .build();

        List<DetalleOrdenCompra> itemsOC = cotizacion.getDetalles().stream().map(itemCot ->
                DetalleOrdenCompra.builder()
                        .ordenCompra(oc)
                        .detalleRequisicion(itemCot.getDetalleRequisicion())
                        .cantidadOrdenada(itemCot.getDetalleRequisicion().getCantidadSolicitada())
                        .precioUnitario(itemCot.getPrecioUnitario())
                        .importe(itemCot.getImporte())
                        .build()
        ).collect(Collectors.toList());

        oc.setDetalles(itemsOC);
        OrdenCompra guardada = ordenCompraRepository.save(oc);

        return OrdenCompraResponse.builder()
                .idOrdenCompra(guardada.getIdOrdenCompra())
                .folio(guardada.getFolio())
                .estado(guardada.getEstado().getCodigo())
                .fechaEmision(guardada.getFechaEmision())
                .fechaEntregaEstimada(guardada.getFechaEntregaEstimada())
                .moneda(guardada.getMoneda())
                .total(guardada.getTotal())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraResponse> listarOrdenes() {
        return ordenCompraRepository.findAll().stream().map(oc ->
                OrdenCompraResponse.builder()
                        .idOrdenCompra(oc.getIdOrdenCompra())
                        .folio(oc.getFolio())
                        .estado(oc.getEstado().getCodigo())
                        .fechaEmision(oc.getFechaEmision())
                        .fechaEntregaEstimada(oc.getFechaEntregaEstimada())
                        .moneda(oc.getMoneda())
                        .total(oc.getTotal())
                        .build()
        ).collect(Collectors.toList());
    }
}