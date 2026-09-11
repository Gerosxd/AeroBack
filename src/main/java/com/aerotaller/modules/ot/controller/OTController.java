package com.aerotaller.modules.ot.controller;

import com.aerotaller.modules.ot.dto.AeronaveComboResponse;
import com.aerotaller.modules.ot.dto.CrearOTRequest;
import com.aerotaller.modules.ot.dto.CrearOTResponse;
import com.aerotaller.modules.ot.dto.SiguienteNoOTResponse;
import com.aerotaller.modules.ot.service.OTService;
import com.aerotaller.modules.ot.service.OTPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aerotaller.modules.ot.dto.OTListadoResponse;

import java.util.List;

@RestController
@RequestMapping("/api/ots")
public class OTController
{

    private final OTService otService;
    private final OTPdfService otPdfService;

    public OTController(OTService otService, OTPdfService otPdfService)
    {
        this.otService = otService;
        this.otPdfService = otPdfService;
    }

    @GetMapping("/matriculas")
    public ResponseEntity<List<AeronaveComboResponse>> obtenerMatriculas()
    {
        return ResponseEntity.ok(otService.obtenerMatriculas());
    }

    @GetMapping("/siguiente-noot")
    public ResponseEntity<SiguienteNoOTResponse> obtenerSiguienteNoOT()
    {
        return ResponseEntity.ok(otService.obtenerSiguienteNoOT());
    }

    @PostMapping
    public ResponseEntity<?> crearOT(@RequestBody CrearOTRequest request)
    {
        try
        {
            CrearOTResponse response = otService.crearOT(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al crear la OT.");
        }
    }

    @GetMapping
    public ResponseEntity<List<OTListadoResponse>> listarOTs()
    {
        return ResponseEntity.ok(otService.listarOTs());
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(otService.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al recuperar los detalles de la OT.");
        }
    }

    // ==========================================
    // NUEVA IMPLEMENTACIÓN: ACTUALIZAR OT (Punto 2 y CORS resuelto)
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarOT(@PathVariable Integer id, @RequestBody CrearOTRequest request) {
        try {
            // SOLUCIÓN: Al cambiar la firma a 'CrearOTRequest', ahora coincide perfectamente
            // con lo que espera el metodo 'otService.actualizarOT(Integer, CrearOTRequest)'
            otService.actualizarOT(id, request);
            return ResponseEntity.ok().body("Orden de Trabajo actualizada con éxito.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al intentar guardar los cambios de la OT.");
        }
    }

    // ==========================================
    // P-02: Generación de carátula AG-145-03 (PDF)
    // ==========================================

    @GetMapping("/{id}/caratula-pdf")
    public ResponseEntity<?> generarCaratulaPdf(@PathVariable Integer id) {
        try {
            byte[] data = otPdfService.generarCaratula(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=caratula-ot-" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al generar la carátula de la OT.");
        }
    }

    // ==========================================
    // P-03: Hoja de Servicio AG-145-04 (PDF)
    // ==========================================

    @GetMapping("/{id}/hoja-servicio-pdf/{idTarea}")
    public ResponseEntity<?> generarHojaServicioPdf(@PathVariable Integer id, @PathVariable Integer idTarea) {
        try {
            byte[] data = otPdfService.generarHojaServicio(id, idTarea);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=hoja-servicio-" + id + "-" + idTarea + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al generar la Hoja de Servicio.");
        }
    }

    @GetMapping("/{id}/hojas-servicio-pdf")
    public ResponseEntity<?> generarHojasServicioPdf(@PathVariable Integer id) {
        try {
            byte[] data = otPdfService.generarHojasServicioTodas(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=hojas-servicio-" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al generar las Hojas de Servicio.");
        }
    }

    // ==========================================
    // P-04: Discrepancias AG-145-12 (PDF)
    // ==========================================

    @GetMapping("/{id}/discrepancia-pdf/{idDiscrepancia}")
    public ResponseEntity<?> generarDiscrepanciaPdf(@PathVariable Integer id, @PathVariable Integer idDiscrepancia) {
        try {
            byte[] data = otPdfService.generarDiscrepancia(id, idDiscrepancia);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=discrepancia-" + id + "-" + idDiscrepancia + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al generar el formato de Discrepancias.");
        }
    }

    @GetMapping({"/{id}/discrepancias-pdf", "/{id}/discrepancias/pdf"})
    public ResponseEntity<?> generarDiscrepanciasPdf(@PathVariable Integer id) {
        try {
            byte[] data = otPdfService.generarDiscrepanciasTodas(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=discrepancias-" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (RuntimeException e) {
            // Si la OT no existe sí es 404, de lo contrario es un 400 Bad Request
            if (e.getMessage() != null && e.getMessage().contains("no existe")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al generar el formato de Discrepancias.");
        }
    }
}