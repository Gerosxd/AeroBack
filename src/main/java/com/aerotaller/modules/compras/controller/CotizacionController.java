package com.aerotaller.modules.compras.controller;

import com.aerotaller.modules.compras.dto.ComparativoCotizacionResponse;
import com.aerotaller.modules.compras.dto.CotizacionRequest;
import com.aerotaller.modules.compras.dto.CotizacionResponse;
import com.aerotaller.modules.compras.service.CotizacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/compras/cotizaciones")
@CrossOrigin(origins = "*")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @PostMapping
    public ResponseEntity<CotizacionResponse> registrar(@RequestBody CotizacionRequest request) {
        return new ResponseEntity<>(cotizacionService.registrarCotizacion(request), HttpStatus.CREATED);
    }

    @GetMapping("/requisicion/{idRequisicion}")
    public ResponseEntity<List<CotizacionResponse>> listarPorRequisicion(@PathVariable Integer idRequisicion) {
        return ResponseEntity.ok(cotizacionService.listarPorRequisicion(idRequisicion));
    }

    @GetMapping("/comparativo/{idRequisicion}")
    public ResponseEntity<ComparativoCotizacionResponse> obtenerComparativo(@PathVariable Integer idRequisicion) {
        return ResponseEntity.ok(cotizacionService.obtenerComparativoPorRequisicion(idRequisicion));
    }
}