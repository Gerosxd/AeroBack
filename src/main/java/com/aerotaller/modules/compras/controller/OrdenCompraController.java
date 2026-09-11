package com.aerotaller.modules.compras.controller;

import com.aerotaller.modules.compras.dto.OrdenCompraRequest;
import com.aerotaller.modules.compras.dto.OrdenCompraResponse;
import com.aerotaller.modules.compras.service.OrdenCompraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/compras/ordenes")
@CrossOrigin(origins = "*")
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    public OrdenCompraController(OrdenCompraService ordenCompraService) {
        this.ordenCompraService = ordenCompraService;
    }

    @PostMapping
    public ResponseEntity<OrdenCompraResponse> generarOrden(@RequestBody OrdenCompraRequest request) {
        return new ResponseEntity<>(ordenCompraService.generarOrdenCompra(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrdenCompraResponse>> listar() {
        return ResponseEntity.ok(ordenCompraService.listarOrdenes());
    }
}