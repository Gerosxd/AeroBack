package com.aerotaller.modules.compras.controller;

import com.aerotaller.modules.compras.dto.RecepcionMercanciaRequest;
import com.aerotaller.modules.compras.dto.RecepcionMercanciaResponse;
import com.aerotaller.modules.compras.service.RecepcionMercanciaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/compras/recepciones")
@CrossOrigin(origins = "*")
public class RecepcionMercanciaController {

    private final RecepcionMercanciaService recepcionService;

    public RecepcionMercanciaController(RecepcionMercanciaService recepcionService) {
        this.recepcionService = recepcionService;
    }

    @PostMapping
    public ResponseEntity<RecepcionMercanciaResponse> registrarRecepcion(@RequestBody RecepcionMercanciaRequest request) {
        return new ResponseEntity<>(recepcionService.registrarRecepcion(request), HttpStatus.CREATED);
    }

    @GetMapping("/orden/{idOrdenCompra}")
    public ResponseEntity<List<RecepcionMercanciaResponse>> listarPorOrden(@PathVariable Integer idOrdenCompra) {
        return ResponseEntity.ok(recepcionService.listarPorOrdenCompra(idOrdenCompra));
    }
}