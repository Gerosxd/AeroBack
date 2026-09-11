package com.aerotaller.modules.compras.controller;

import com.aerotaller.modules.compras.dto.RequisicionRequest;
import com.aerotaller.modules.compras.dto.RequisicionResponse;
import com.aerotaller.modules.compras.service.RequisicionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/compras/requisiciones")
@CrossOrigin(origins = "*")
public class RequisicionController {

    private final RequisicionService requisicionService;

    public RequisicionController(RequisicionService requisicionService) {
        this.requisicionService = requisicionService;
    }

    @PostMapping
    public ResponseEntity<RequisicionResponse> crear(@RequestBody RequisicionRequest request) {
        return new ResponseEntity<>(requisicionService.crear(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequisicionResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(requisicionService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<RequisicionResponse>> listarTodas() {
        return ResponseEntity.ok(requisicionService.listarTodas());
    }
}