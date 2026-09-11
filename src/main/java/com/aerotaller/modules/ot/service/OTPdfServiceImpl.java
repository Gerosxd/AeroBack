package com.aerotaller.modules.ot.service;

import com.aerotaller.modelos.Aeronave;
import com.aerotaller.modelos.Cliente;
import com.aerotaller.modelos.NuevaOT;
import com.aerotaller.modelos.OTDiscrepancia;
import com.aerotaller.modelos.OTTareaMantenimiento;
import com.aerotaller.modules.catalogo.repository.ModeloAeronaveRepository;
import com.aerotaller.modules.ot.repository.NuevaOTRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.aerotaller.modules.ot.service.AGFormatoPdfHelper.*;

@Service
public class OTPdfServiceImpl implements OTPdfService {

    private final NuevaOTRepository nuevaOTRepository;
    private final ModeloAeronaveRepository modeloAeronaveRepository;

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public OTPdfServiceImpl(NuevaOTRepository nuevaOTRepository,
                            ModeloAeronaveRepository modeloAeronaveRepository) {
        this.nuevaOTRepository = nuevaOTRepository;
        this.modeloAeronaveRepository = modeloAeronaveRepository;
    }

    @Override
    public byte[] generarCaratula(Integer idOT) {
        NuevaOT ot = nuevaOTRepository.findById(idOT)
                .orElseThrow(() -> new RuntimeException("La Orden de Trabajo solicitada no existe."));

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDType1Font reg = fontRegular();
            PDType1Font ita = fontItalic();

            float pageWidth = PDRectangle.A4.getWidth();
            float pageHeight = PDRectangle.A4.getHeight();
            float contentX = PAGE_MARGIN;
            float contentW = pageWidth - 2 * PAGE_MARGIN;

            String noControl = ot.getNoOT();
            String fecha = ot.getFechaCreacion() != null ? ot.getFechaCreacion().format(FMT_FECHA) : "";

            // ===================== PÁGINA 1 =====================
            PDPage page1 = new PDPage(PDRectangle.A4);
            doc.addPage(page1);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
                float y = pageHeight - PAGE_MARGIN;

                y = dibujarEncabezado(doc, cs, pageWidth, y,
                        "ORDEN DE TRABAJO", "WORK ORDER",
                        "NÚMERO DE CONTROL", noControl, fecha);

                y -= 4;
                y = dibujarDeclaracionLegal(cs, contentX, y, contentW, reg);

                y -= 6;
                y = dibujarTipoOT(cs, contentX, y, contentW, ot);

                y -= 6;
                y = dibujarInfoCliente(cs, contentX, y, contentW, ot);

                y -= 6;
                y = dibujarInfoAeronave(cs, contentX, y, contentW, ot);

                dibujarPie(cs, pageWidth, "AG-145/03", 1, 2);
            }

            // ===================== PÁGINA 2 =====================
            PDPage page2 = new PDPage(PDRectangle.A4);
            doc.addPage(page2);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
                float y = pageHeight - PAGE_MARGIN;

                y = dibujarEncabezado(doc, cs, pageWidth, y,
                        "ORDEN DE TRABAJO", "WORK ORDER",
                        "NÚMERO DE CONTROL", noControl, fecha);

                y -= 6;
                // Siempre visible conforme a auditoría AFAC (Punto 6)
                y = dibujarInfoComponente(cs, contentX, y, contentW, ot);

                y -= 6;
                y = dibujarTablaTareas(cs, contentX, y, contentW, ot.getTareasMantenimiento());

                y -= 6;
                y = dibujarBloqueComentario(cs, contentX, y, contentW,
                        "COMENTARIOS / REQUERIMIENTOS ADICIONALES DEL CLIENTE / OPERADOR",
                        "CLIENT / OPERATOR COMMENTS / ADDITIONAL REQUIREMENTS",
                        ot.getComentarioCliente(), 32f);

                y -= 6;
                y = dibujarBloqueComentario(cs, contentX, y, contentW,
                        "COMENTARIOS RESPONSABLE DE TALLER",
                        "WORKSHOP REPRESENTATIVE COMMENTS",
                        ot.getComentarioTaller(), 32f);

                y -= 6;
                dibujarFirmaResponsable(cs, contentX, y, contentW, reg, ita);

                dibujarPie(cs, pageWidth, "AG-145/03", 2, 2);
            }

            doc.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar la carátula de la OT: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------
    // Secciones Carátula
    // ---------------------------------------------------------------------

    private float dibujarDeclaracionLegal(PDPageContentStream cs, float x, float y, float w,
                                          PDType1Font reg) throws IOException {
        String textoEs = "Se establece que quien firma se reconoce con la facultad necesaria para autorizar la ejecución de los "
                + "SERVICIOS DE MANTENIMIENTO PROGRAMADO/ NO PROGRAMADO Y/O REQUERIMIENTOS ADICIONALES para lo cual proporcionará "
                + "al Taller Aeronáutico las guías de inspección y mantenimiento, así como la información técnica aprobada por la "
                + "A.F.A.C. en su programa de mantenimiento. La presente Orden de Trabajo se genera en concordancia a los "
                + "requerimientos de la CO AV 145.01/24 párrafo 15.5.";

        List<String> lineasEs = partirLineas(textoEs, reg, 6.5f, w - 12);
        float lineH = 7.5f;
        float boxH = (lineasEs.size()) * lineH + 8;

        cs.setNonStrokingColor(AZUL_CLARO);
        cs.addRect(x, y - boxH, w, boxH);
        cs.fill();
        rect(cs, x, y - boxH, w, boxH);

        float ty = y - 9;
        cs.setNonStrokingColor(Color.BLACK);
        for (String linea : lineasEs) {
            cs.beginText();
            cs.setFont(reg, 6.5f);
            cs.newLineAtOffset(x + 6, ty);
            cs.showText(linea);
            cs.endText();
            ty -= lineH;
        }

        return y - boxH;
    }

    private float dibujarTipoOT(PDPageContentStream cs, float x, float y, float w, NuevaOT ot) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w, "TIPO DE ORDEN DE TRABAJO", "WORK ORDER TYPE");

        float rowH = 18f;
        float colW = w / 2;

        boolean esAeronave = "AERONAVE".equalsIgnoreCase(ot.getTipoMantenimiento());
        boolean esComponente = "COMPONENTE".equalsIgnoreCase(ot.getTipoMantenimiento());
        boolean esProgramado = "PROGRAMADO".equalsIgnoreCase(ot.getModalidadMantenimiento());
        boolean esNoProgramado = "NO_PROGRAMADO".equalsIgnoreCase(ot.getModalidadMantenimiento());

        // Fila 1
        rect(cs, x, y - rowH, colW, rowH);
        rect(cs, x + colW, y - rowH, colW, rowH);
        checkbox(cs, x + 8, y - 13, esAeronave, "MANTENIMIENTO DE AERONAVE", "AIRCRAFT MAINTENANCE", 6.5f);
        checkbox(cs, x + colW + 8, y - 13, esComponente, "REPARACIÓN DE COMPONENTE", "COMPONENT REPAIR", 6.5f);
        y -= rowH;

        // Fila 2
        rect(cs, x, y - rowH, colW, rowH);
        rect(cs, x + colW, y - rowH, colW, rowH);
        checkbox(cs, x + 8, y - 13, esProgramado, "MANTENIMIENTO PROGRAMADO", "SCHEDULED", 6.5f);
        checkbox(cs, x + colW + 8, y - 13, esNoProgramado, "MANTENIMIENTO NO PROGRAMADO", "UNSCHEDULED", 6.5f);
        y -= rowH;

        return y;
    }

    private float dibujarInfoCliente(PDPageContentStream cs, float x, float y, float w, NuevaOT ot) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w, "INFORMACIÓN DEL CLIENTE / OPERADOR", "CLIENT / OPERATOR INFORMATION");

        Cliente c = ot.getCliente();
        String operador = c != null ? c.getCompania() : "";
        String contacto = c != null ? c.getContacto() : "";
        String direccion = c != null ? c.getDireccion() : "";
        String ciudad = (c != null && c.getCiudad() != null && !c.getCiudad().isBlank()) ? c.getCiudad() : "—";
        String estadoRep = (c != null && c.getEstadoRep() != null && !c.getEstadoRep().isBlank()) ? c.getEstadoRep() : "—";
        String telefono = c != null ? c.getTelefono() : "";
        String correo = c != null ? c.getCorreo() : "";

        float rowH1 = 24f;
        float colW = w / 2;

        // Fila 1: Operador / Contacto
        rect(cs, x, y - rowH1, colW, rowH1);
        rect(cs, x + colW, y - rowH1, colW, rowH1);
        etiquetaBilingue(cs, x + 4, y - 9, "OPERADOR / CLIENTE:", "OPERATOR / CUSTOMER", 6.5f);
        valor(cs, x + 4, y - 20, operador, 7.5f);
        etiquetaBilingue(cs, x + colW + 4, y - 9, "CONTACTO:", "CONTACT", 6.5f);
        valor(cs, x + colW + 4, y - 20, contacto, 7.5f);
        y -= rowH1;

        // Fila 2: Dirección (32%), Ciudad (18%), Estado (18%), Teléfono (16%), Correo (16%)
        float rowH2 = 24f;
        float wDir = w * 0.32f;
        float wCd = w * 0.17f;
        float wEdo = w * 0.17f;
        float wTel = w * 0.17f;
        float wCor = w - (wDir + wCd + wEdo + wTel);

        rect(cs, x, y - rowH2, wDir, rowH2);
        rect(cs, x + wDir, y - rowH2, wCd, rowH2);
        rect(cs, x + wDir + wCd, y - rowH2, wEdo, rowH2);
        rect(cs, x + wDir + wCd + wEdo, y - rowH2, wTel, rowH2);
        rect(cs, x + wDir + wCd + wEdo + wTel, y - rowH2, wCor, rowH2);

        etiquetaBilingue(cs, x + 3, y - 9, "DIRECCIÓN:", "ADDRESS", 5.5f);
        valor(cs, x + 3, y - 20, direccion, 6.5f);

        etiquetaBilingue(cs, x + wDir + 3, y - 9, "CIUDAD:", "CITY", 5.5f);
        valor(cs, x + wDir + 3, y - 20, ciudad, 6.5f);

        etiquetaBilingue(cs, x + wDir + wCd + 3, y - 9, "ESTADO:", "STATE", 5.5f);
        valor(cs, x + wDir + wCd + 3, y - 20, estadoRep, 6.5f);

        etiquetaBilingue(cs, x + wDir + wCd + wEdo + 3, y - 9, "TELÉFONO:", "PHONE", 5.5f);
        valor(cs, x + wDir + wCd + wEdo + 3, y - 20, telefono, 6.5f);

        etiquetaBilingue(cs, x + wDir + wCd + wEdo + wTel + 3, y - 9, "CORREO:", "E-MAIL", 5.5f);
        valor(cs, x + wDir + wCd + wEdo + wTel + 3, y - 20, correo, 6.5f);

        y -= rowH2;
        return y;
    }

    private float dibujarInfoAeronave(PDPageContentStream cs, float x, float y, float w, NuevaOT ot) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w, "INFORMACIÓN DE AERONAVE", "AIRCRAFT INFORMATION");

        Aeronave a = ot.getMatricula();
        String matricula = a != null ? a.getMatricula() : "";
        String nsAeronave = a != null ? a.getNsAeronave() : "";
        String marca = "";
        String modelo = "";
        if (a != null) {
            var modeloOpt = modeloAeronaveRepository.findById(a.getModeloAeronave());
            if (modeloOpt.isPresent()) {
                marca = modeloOpt.get().getMarca();
                modelo = modeloOpt.get().getModelo();
            }
        }

        // 1. Checkboxes TIPO DE AERONAVE
        float rowH = 18f;
        float c4 = w / 4;
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 4; i++) {
            cs.moveTo(x + c4 * i, y - rowH);
            cs.lineTo(x + c4 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 11, "TIPO DE AERONAVE:", "AIRCRAFT TYPE", 6f);
        checkbox(cs, x + c4 + 6, y - 12, false, "ALA FIJA", "FIXED WING", 6f);
        checkbox(cs, x + c4 * 2 + 6, y - 12, false, "ALA ROTATIVA", "ROTARY WING", 6f);
        etiquetaBilingue(cs, x + c4 * 3 + 6, y - 11, "OTRO / OTHER:", null, 6f);
        y -= rowH;

        // 2. Matrícula / Marca / Modelo
        float c3 = w / 3;
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 3; i++) {
            cs.moveTo(x + c3 * i, y - rowH);
            cs.lineTo(x + c3 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 8, "Matrícula / Registration:", null, 6f);
        valor(cs, x + 4, y - 16, matricula, 7.5f);
        etiquetaBilingue(cs, x + c3 + 4, y - 8, "Marca / Manufacturer:", null, 6f);
        valor(cs, x + c3 + 4, y - 16, marca, 7.5f);
        etiquetaBilingue(cs, x + c3 * 2 + 4, y - 8, "Modelo / Model:", null, 6f);
        valor(cs, x + c3 * 2 + 4, y - 16, modelo, 7.5f);
        y -= rowH;

        // 3. No. Serie / Horas Totales / Ciclos Totales
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 3; i++) {
            cs.moveTo(x + c3 * i, y - rowH);
            cs.lineTo(x + c3 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 8, "Número de Serie / Serial number:", null, 6f);
        valor(cs, x + 4, y - 16, nsAeronave, 7.5f);
        etiquetaBilingue(cs, x + c3 + 4, y - 8, "Horas Totales / Total Hours:", null, 6f);
        valor(cs, x + c3 + 4, y - 16, ot.getHorasTotales() != null ? ot.getHorasTotales().toString() : "", 7.5f);
        etiquetaBilingue(cs, x + c3 * 2 + 4, y - 8, "Ciclos Totales / Total Cycles:", null, 6f);
        valor(cs, x + c3 * 2 + 4, y - 16, ot.getCiclosTotales() != null ? ot.getCiclosTotales().toString() : "", 7.5f);
        y -= rowH;

        // 4. Encabezados de Unidades Mayores (Punto 12: Cuadrícula oficial)
        float c5 = w / 5;
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 5; i++) {
            cs.moveTo(x + c5 * i, y - rowH);
            cs.lineTo(x + c5 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 7, "Ala fija", "Fixed Wing", 6f);
        etiquetaBilingue(cs, x + c5 + 4, y - 7, "Motor 1", "Engine 1 (LH)", 6f);
        etiquetaBilingue(cs, x + c5 * 2 + 4, y - 7, "Motor 2", "Engine 2 (RH)", 6f);
        etiquetaBilingue(cs, x + c5 * 3 + 4, y - 7, "Motor 3", "Engine 3", 6f);
        etiquetaBilingue(cs, x + c5 * 4 + 4, y - 7, "UPA / Hélice", "APU / Propeller", 6f);
        y -= rowH;

        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 5; i++) {
            cs.moveTo(x + c5 * i, y - rowH);
            cs.lineTo(x + c5 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 7, "Ala rotativa", "Rotary Wing", 6f);
        etiquetaBilingue(cs, x + c5 + 4, y - 7, "Motor 1", "Engine 1 (LH)", 6f);
        etiquetaBilingue(cs, x + c5 * 2 + 4, y - 7, "Motor 2", "Engine 2 (RH)", 6f);
        etiquetaBilingue(cs, x + c5 * 3 + 4, y - 7, "Rotor principal", "Main rotor", 6f);
        etiquetaBilingue(cs, x + c5 * 4 + 4, y - 7, "Rotor de cola", "Tail rotor", 6f);
        y -= rowH;

        // Sub-filas Modelo, Serie, Horas, Ciclos
        String[] rubros = {"Modelo:", "Número de Serie:", "Horas Totales:", "Ciclos Totales:"};
        for (String rubro : rubros) {
            rect(cs, x, y - rowH, w, rowH);
            for (int i = 1; i < 5; i++) {
                cs.moveTo(x + c5 * i, y - rowH);
                cs.lineTo(x + c5 * i, y);
                cs.stroke();
            }
            etiquetaBilingue(cs, x + 4, y - 11, rubro, null, 6f);
            if (rubro.startsWith("Horas Totales:")) {
                valor(cs, x + c5 + 4, y - 12, ot.getTiempoMotor1() != null ? ot.getTiempoMotor1().toString() : "—", 6.5f);
                valor(cs, x + c5 * 2 + 4, y - 12, ot.getTiempoMotor2() != null ? ot.getTiempoMotor2().toString() : "—", 6.5f);
                valor(cs, x + c5 * 3 + 4, y - 12, ot.getTiempoMotor3() != null ? ot.getTiempoMotor3().toString() : "—", 6.5f);
                valor(cs, x + c5 * 4 + 4, y - 12, ot.getTiempoAPU() != null ? ot.getTiempoAPU().toString() : "—", 6.5f);
            } else if (rubro.startsWith("Ciclos Totales:")) {
                valor(cs, x + c5 + 4, y - 12, ot.getCicloMotor1() != null ? ot.getCicloMotor1().toString() : "—", 6.5f);
                valor(cs, x + c5 * 2 + 4, y - 12, ot.getCicloMotor2() != null ? ot.getCicloMotor2().toString() : "—", 6.5f);
                valor(cs, x + c5 * 3 + 4, y - 12, ot.getCicloMotor3() != null ? ot.getCicloMotor3().toString() : "—", 6.5f);
                valor(cs, x + c5 * 4 + 4, y - 12, ot.getCicloAPU() != null ? ot.getCicloAPU().toString() : "—", 6.5f);
            }
            y -= rowH;
        }

        return y;
    }

    private float dibujarInfoComponente(PDPageContentStream cs, float x, float y, float w, NuevaOT ot) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w, "INFORMACIÓN DE COMPONENTE", "COMPONENT INFORMATION");

        float rowH = 18f;
        float c3 = w / 3;

        // Si no es componente, llena defensivamente con N/A conforme a formato aprobado
        boolean tieneComp = "COMPONENTE".equalsIgnoreCase(ot.getTipoMantenimiento());
        String desc = tieneComp ? ot.getComponenteDescripcion() : "N/A";
        String pn = tieneComp ? ot.getComponenteNumeroParte() : "N/A";
        String sn = tieneComp ? ot.getComponenteNumeroSerie() : "N/A";
        String cant = tieneComp && ot.getComponenteCantidad() != null ? ot.getComponenteCantidad().toString() : "N/A";
        String hrs = tieneComp && ot.getComponenteHoras() != null ? ot.getComponenteHoras().toString() : "N/A";
        String cic = tieneComp && ot.getComponenteCiclos() != null ? ot.getComponenteCiclos().toString() : "N/A";
        String aero = tieneComp ? ot.getComponenteAeronaveAsociada() : "N/A";
        String rem = tieneComp ? ot.getComponenteHorasCiclosRemocion() : "N/A";

        // Fila 1
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 3; i++) {
            cs.moveTo(x + c3 * i, y - rowH);
            cs.lineTo(x + c3 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 8, "Descripción / Description:", null, 5.5f);
        valor(cs, x + 4, y - 15, desc, 6.5f);
        etiquetaBilingue(cs, x + c3 + 4, y - 8, "Número de parte / Part number:", null, 5.5f);
        valor(cs, x + c3 + 4, y - 15, pn, 6.5f);
        etiquetaBilingue(cs, x + c3 * 2 + 4, y - 8, "Número de Serie / Serial number:", null, 5.5f);
        valor(cs, x + c3 * 2 + 4, y - 15, sn, 6.5f);
        y -= rowH;

        // Fila 2
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 3; i++) {
            cs.moveTo(x + c3 * i, y - rowH);
            cs.lineTo(x + c3 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 8, "Cantidad / Quantity:", null, 5.5f);
        valor(cs, x + 4, y - 15, cant, 6.5f);
        etiquetaBilingue(cs, x + c3 + 4, y - 8, "Horas Totales / Total Hours:", null, 5.5f);
        valor(cs, x + c3 + 4, y - 15, hrs, 6.5f);
        etiquetaBilingue(cs, x + c3 * 2 + 4, y - 8, "Ciclos Totales / Total Cycles:", null, 5.5f);
        valor(cs, x + c3 * 2 + 4, y - 15, cic, 6.5f);
        y -= rowH;

        // Fila 3
        rect(cs, x, y - rowH, w, rowH);
        float c2 = w / 2;
        cs.moveTo(x + c2, y - rowH);
        cs.lineTo(x + c2, y);
        cs.stroke();
        etiquetaBilingue(cs, x + 4, y - 8, "Aeronave asociada / Associated aircraft:", null, 5.5f);
        valor(cs, x + 4, y - 15, aero, 6.5f);
        etiquetaBilingue(cs, x + c2 + 4, y - 8, "Horas / Ciclos Totales a remoción:", null, 5.5f);
        valor(cs, x + c2 + 4, y - 15, rem, 6.5f);
        y -= rowH;

        return y;
    }

    private float dibujarTablaTareas(PDPageContentStream cs, float x, float y, float w,
                                     List<OTTareaMantenimiento> tareas) throws IOException {
        float headH = 16f;
        float colItem = 35f;
        float colTarea = 115f;
        float colDesc = w - colItem - colTarea;

        cs.setNonStrokingColor(AZUL_MEDIO);
        cs.addRect(x, y - headH, w, headH);
        cs.fill();
        textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x, colItem, y - 7, "ÍTEM");
        textoCentrado(cs, fontItalic(), 5.5f, Color.WHITE, x, colItem, y - 13, "ITEM");
        textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x + colItem, colTarea, y - 7, "TAREA");
        textoCentrado(cs, fontItalic(), 5.5f, Color.WHITE, x + colItem, colTarea, y - 13, "TASK");
        textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x + colItem + colTarea, colDesc, y - 7, "DESCRIPCIÓN");
        textoCentrado(cs, fontItalic(), 5.5f, Color.WHITE, x + colItem + colTarea, colDesc, y - 13, "DESCRIPTION");
        cs.setNonStrokingColor(Color.BLACK);
        y -= headH;

        int totalFilas = Math.max(5, tareas != null ? tareas.size() : 0);
        float rowH = 15f;
        for (int i = 0; i < totalFilas; i++) {
            rect(cs, x, y - rowH, colItem, rowH);
            rect(cs, x + colItem, y - rowH, colTarea, rowH);
            rect(cs, x + colItem + colTarea, y - rowH, colDesc, rowH);

            textoCentrado(cs, fontRegular(), 7f, Color.BLACK, x, colItem, y - 11, String.format("%02d", i + 1));

            if (tareas != null && i < tareas.size()) {
                OTTareaMantenimiento t = tareas.get(i);
                valor(cs, x + colItem + 4, y - 11, t.getCodigo(), 7f);
                List<String> lineas = partirLineas(t.getDescripcion(), fontRegular(), 6.5f, colDesc - 8);
                if (!lineas.isEmpty()) {
                    valor(cs, x + colItem + colTarea + 4, y - 11, lineas.get(0), 6.5f);
                }
            }
            y -= rowH;
        }

        return y;
    }

    private float dibujarBloqueComentario(PDPageContentStream cs, float x, float y, float w,
                                          String tituloEs, String tituloEn, String contenido, float boxH) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w, tituloEs, tituloEn);
        rect(cs, x, y - boxH, w, boxH);

        if (contenido != null && !contenido.isBlank()) {
            List<String> lineas = partirLineas(contenido, fontRegular(), 7f, w - 12);
            float ty = y - 10;
            for (String linea : lineas) {
                if (ty < y - boxH + 4) break;
                valor(cs, x + 5, ty, linea, 7f);
                ty -= 9;
            }
        }

        return y - boxH;
    }

    private void dibujarFirmaResponsable(PDPageContentStream cs, float x, float y, float w,
                                         PDType1Font reg, PDType1Font ita) throws IOException {
        // Declaración oficial bilingüe (Punto 10)
        String declEs = "DECLARACIÓN DE AUTORIZACIÓN: Quien firma la presente Orden de Trabajo se reconoce con la facultad necesaria "
                + "para autorizar la ejecución de los servicios de mantenimiento programado/no programado y/o la reparación de componente descrita. "
                + "Para lo cual proporcionará al Taller Aeronáutico las guías de inspección, mantenimiento e información técnica aprobada por la A.F.A.C. "
                + "La presente OT se emite conforme a CO AV 145.01/24 párrafos 15.4, 15.5 y 15.6.";

        String declEn = "AUTHORIZATION STATEMENT: The undersigned, by signing this Work Order, acknowledges having the necessary authority "
                + "to authorize the performance of the scheduled/unscheduled maintenance services and/or the repair of the component described herein. "
                + "For this purpose, the signatory shall provide the Aircraft Maintenance Organization with the applicable inspection guides, maintenance instructions, "
                + "and technical information approved by the Agencia Federal de Aviación Civil (A.F.A.C.). This Work Order is issued in accordance with CO AV 145.01/24, "
                + "paragraphs 15.4, 15.5, and 15.6.";

        List<String> lineasEs = partirLineas(declEs, reg, 5.5f, w - 10);
        List<String> lineasEn = partirLineas(declEn, ita, 5f, w - 10);
        float lineH = 6.5f;
        float declH = (lineasEs.size() + lineasEn.size()) * lineH + 10f;

        cs.setNonStrokingColor(AZUL_CLARO);
        cs.addRect(x, y - declH, w, declH);
        cs.fill();
        rect(cs, x, y - declH, w, declH);

        float ty = y - 8;
        cs.setNonStrokingColor(Color.BLACK);
        for (String linea : lineasEs) {
            cs.beginText();
            cs.setFont(reg, 5.5f);
            cs.newLineAtOffset(x + 5, ty);
            cs.showText(linea);
            cs.endText();
            ty -= lineH;
        }
        ty -= 2;
        for (String linea : lineasEn) {
            cs.beginText();
            cs.setFont(ita, 5f);
            cs.setNonStrokingColor(GRIS_LINEA);
            cs.newLineAtOffset(x + 5, ty);
            cs.showText(linea);
            cs.endText();
            ty -= lineH;
        }
        cs.setNonStrokingColor(Color.BLACK);
        y -= declH;

        // Banda Azul Marino para Responsable de Taller
        float rowH = 16f;
        cs.setNonStrokingColor(AZUL_MARINO);
        cs.addRect(x, y - rowH, w, rowH);
        cs.fill();
        textoCentrado(cs, fontBold(), 7.5f, Color.WHITE, x, w, y - 8, "RESPONSABLE DE TALLER / WORKSHOP REPRESENTATIVE");
        cs.setNonStrokingColor(Color.BLACK);
        rect(cs, x, y - rowH, w, rowH);
        y -= rowH;

        // Celdas de Firma
        String[][] campos = {
                {"FIRMA / SIGNATURE:", null},
                {"NOMBRE / NAME:", null},
                {"FECHA / DATE:", null}
        };
        for (String[] campo : campos) {
            rect(cs, x, y - rowH, w, rowH);
            etiquetaBilingue(cs, x + 5, y - 11, campo[0], campo[1], 6.5f);
            y -= rowH;
        }
    }

    // =====================================================================
    // Hojas de Servicio y Discrepancias
    // =====================================================================

    @Override
    public byte[] generarHojaServicio(Integer idOT, Integer idTareaOT) {
        NuevaOT ot = nuevaOTRepository.findById(idOT)
                .orElseThrow(() -> new RuntimeException("La Orden de Trabajo solicitada no existe."));

        List<OTTareaMantenimiento> tareas = ot.getTareasMantenimiento();
        if (tareas == null || tareas.isEmpty()) {
            throw new RuntimeException("La Orden de Trabajo no tiene tareas registradas.");
        }

        int indice = -1;
        for (int i = 0; i < tareas.size(); i++) {
            if (tareas.get(i).getIdTareaOT() != null && tareas.get(i).getIdTareaOT().equals(idTareaOT)) {
                indice = i;
                break;
            }
        }
        if (indice < 0) {
            throw new RuntimeException("La tarea indicada no pertenece a esta Orden de Trabajo.");
        }

        return construirHojasServicio(ot, List.of(tareas.get(indice)), List.of(indice + 1));
    }

    @Override
    public byte[] generarHojasServicioTodas(Integer idOT) {
        NuevaOT ot = nuevaOTRepository.findById(idOT)
                .orElseThrow(() -> new RuntimeException("La Orden de Trabajo solicitada no existe."));

        List<OTTareaMantenimiento> tareas = ot.getTareasMantenimiento();
        if (tareas == null || tareas.isEmpty()) {
            throw new RuntimeException("La Orden de Trabajo no tiene tareas registradas.");
        }

        List<Integer> items = new java.util.ArrayList<>();
        for (int i = 0; i < tareas.size(); i++) {
            items.add(i + 1);
        }
        return construirHojasServicio(ot, tareas, items);
    }

    private byte[] construirHojasServicio(NuevaOT ot, List<OTTareaMantenimiento> tareas, List<Integer> items) {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float pageWidth = PDRectangle.A4.getWidth();
            float pageHeight = PDRectangle.A4.getHeight();
            float contentX = PAGE_MARGIN;
            float contentW = pageWidth - 2 * PAGE_MARGIN;

            String noControl = ot.getNoOT();
            int total = tareas.size();

            for (int i = 0; i < total; i++) {
                OTTareaMantenimiento tarea = tareas.get(i);
                int item = items.get(i);

                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    float y = pageHeight - PAGE_MARGIN;

                    String fechaCumpl = tarea.getFechaCumplimiento() != null
                            ? tarea.getFechaCumplimiento().format(FMT_FECHA)
                            : "";
                    y = dibujarEncabezado(doc, cs, pageWidth, y,
                            "HOJA DE SERVICIO", "JOB CARD",
                            "ORDEN DE TRABAJO", noControl, fechaCumpl);

                    y -= 6;
                    y = dibujarDatosGenerales(cs, contentX, y, contentW, ot, tarea, item);

                    y -= 6;
                    y = dibujarDescripcionTarea(cs, contentX, y, contentW, tarea);

                    y -= 6;
                    y = dibujarTipoTarea(cs, contentX, y, contentW, tarea);

                    y -= 6;
                    y = dibujarAccionCorrectiva(cs, contentX, y, contentW, tarea);

                    y -= 6;
                    y = dibujarPartesAsociadas(cs, contentX, y, contentW, tarea);

                    y -= 6;
                    dibujarCertificacionFirmas(cs, contentX, y, contentW, tarea);

                    dibujarPie(cs, pageWidth, "AG-145/04", i + 1, total);
                }
            }

            doc.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar la Hoja de Servicio: " + e.getMessage(), e);
        }
    }

    private float dibujarDatosGenerales(PDPageContentStream cs, float x, float y, float w,
                                        NuevaOT ot, OTTareaMantenimiento tarea, int item) throws IOException {
        float headH = 20f;
        float rowH = 18f;
        float c4 = w / 4;

        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x, y - headH, w, headH);
        cs.fill();
        cs.setNonStrokingColor(Color.BLACK);
        String[][] cols = {
                {"OPERADOR / CLIENTE", "OPERATOR / CUSTOMER"},
                {"MATRÍCULA", "REGISTRATION"},
                {"HORAS.HOMBRE = HH", "Man-hours = MH"},
                {"ÍTEM", "ITEM"}
        };
        for (int i = 0; i < 4; i++) {
            textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x + c4 * i, c4, y - 8, cols[i][0]);
            textoCentrado(cs, fontItalic(), 5.5f, Color.WHITE, x + c4 * i, c4, y - 15, cols[i][1]);
        }
        y -= headH;

        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 4; i++) {
            cs.moveTo(x + c4 * i, y - rowH);
            cs.lineTo(x + c4 * i, y);
            cs.stroke();
        }
        String operador = ot.getCliente() != null ? ot.getCliente().getCompania() : "";
        String matricula = ot.getMatricula() != null ? ot.getMatricula().getMatricula() : "";
        String hh = tarea.getHorasTotales() != null ? tarea.getHorasTotales().toString() : "";

        // Ajuste de tamaño para clientes de nombre largo
        float sizeOperador = operador.length() > 25 ? 6.5f : 7.5f;
        textoCentrado(cs, fontRegular(), sizeOperador, Color.BLACK, x, c4, y - 12, operador);
        textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, x + c4, c4, y - 12, matricula);
        textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, x + c4 * 2, c4, y - 12, hh);
        textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, x + c4 * 3, c4, y - 12, String.format("%02d", item));
        y -= rowH;

        return y;
    }

    private float dibujarDescripcionTarea(PDPageContentStream cs, float x, float y, float w,
                                          OTTareaMantenimiento tarea) throws IOException {
        float headH = 20f;
        float colTarea = w * 0.25f;
        float colDesc = w * 0.50f;
        float colInterv = w - colTarea - colDesc;

        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x, y - headH, w, headH);
        cs.fill();
        textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x, colTarea, y - 8, "TAREA");
        textoCentrado(cs, fontItalic(), 5.5f, Color.WHITE, x, colTarea, y - 15, "TASK");
        textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x + colTarea, colDesc, y - 8, "DESCRIPCIÓN");
        textoCentrado(cs, fontItalic(), 5.5f, Color.WHITE, x + colTarea, colDesc, y - 15, "DESCRIPTION");
        textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x + colTarea + colDesc, colInterv, y - 8, "INTERVALO");
        textoCentrado(cs, fontItalic(), 5.5f, Color.WHITE, x + colTarea + colDesc, colInterv, y - 15, "INTERVAL");
        cs.setNonStrokingColor(Color.BLACK);
        y -= headH;

        List<String> lineasDesc = partirLineas(tarea.getDescripcion(), fontRegular(), 7f, colDesc - 8);
        float rowH = Math.max(22f, lineasDesc.size() * 9.5f + 8f);

        rect(cs, x, y - rowH, w, rowH);
        cs.moveTo(x + colTarea, y - rowH);
        cs.lineTo(x + colTarea, y);
        cs.stroke();
        cs.moveTo(x + colTarea + colDesc, y - rowH);
        cs.lineTo(x + colTarea + colDesc, y);
        cs.stroke();

        valor(cs, x + 5, y - 13, tarea.getCodigo(), 7.5f);
        float ty = y - 12;
        for (String linea : lineasDesc) {
            valor(cs, x + colTarea + 5, ty, linea, 7f);
            ty -= 9;
        }
        valor(cs, x + colTarea + colDesc + 5, y - 13, tarea.getIntervalo(), 7.5f);
        y -= rowH;

        return y;
    }

    private float dibujarTipoTarea(PDPageContentStream cs, float x, float y, float w,
                                   OTTareaMantenimiento tarea) throws IOException {
        float headH = 20f;
        float c4 = w / 4;

        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x, y - headH, w, headH);
        cs.fill();
        String[][] cols = {
                {"TIPO DE TAREA", "TASK TYPE"},
                {"NO. DE PARTE", "PART NO."},
                {"NO. DE SERIE", "SERIAL NO."},
                {"¿REQUIERE INSPECCIÓN RII?", "RII INSPECTION REQUIRED?"}
        };
        for (int i = 0; i < 4; i++) {
            textoCentrado(cs, fontBold(), i == 3 ? 5.5f : 6.5f, Color.WHITE, x + c4 * i, c4, y - 8, cols[i][0]);
            textoCentrado(cs, fontItalic(), 5f, Color.WHITE, x + c4 * i, c4, y - 15, cols[i][1]);
        }
        cs.setNonStrokingColor(Color.BLACK);
        y -= headH;

        float rowH = 22f;
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 4; i++) {
            cs.moveTo(x + c4 * i, y - rowH);
            cs.lineTo(x + c4 * i, y);
            cs.stroke();
        }

        textoCentrado(cs, fontRegular(), 7f, Color.BLACK, x, c4, y - 14, tarea.getTipoTarea());
        textoCentrado(cs, fontRegular(), 7f, Color.BLACK, x + c4, c4, y - 14, tarea.getNumeroParte());
        textoCentrado(cs, fontRegular(), 7f, Color.BLACK, x + c4 * 2, c4, y - 14, tarea.getNumeroSerie());

        boolean rii = "SI".equalsIgnoreCase(tarea.getRequiereRII());
        boolean noRii = "NO".equalsIgnoreCase(tarea.getRequiereRII());
        float baseX = x + c4 * 3;
        textoCentrado(cs, fontBold(), 6f, Color.BLACK, baseX, c4 / 2, y - 8, "SI (YES)");
        textoCentrado(cs, fontBold(), 6f, Color.BLACK, baseX + c4 / 2, c4 / 2, y - 8, "NO");
        checkbox(cs, baseX + c4 / 4 - 4, y - 19, rii, null, null, 6f);
        checkbox(cs, baseX + c4 * 3 / 4 - 4, y - 19, noRii, null, null, 6f);
        y -= rowH;

        return y;
    }

    private float dibujarAccionCorrectiva(PDPageContentStream cs, float x, float y, float w,
                                          OTTareaMantenimiento tarea) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w, "ACCIÓN CORRECTIVA", "CORRECTIVE ACTION");

        float boxH = 50f;
        rect(cs, x, y - boxH, w, boxH);

        List<String> lineas = partirLineas(tarea.getAccionCorrectiva(), fontRegular(), 7f, w - 12);
        float ty = y - 11;
        for (String linea : lineas) {
            if (ty < y - boxH + 6) break;
            valor(cs, x + 6, ty, linea, 7f);
            ty -= 9;
        }

        return y - boxH;
    }

    private float dibujarPartesAsociadas(PDPageContentStream cs, float x, float y, float w,
                                         OTTareaMantenimiento tarea) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w,
                "PARTE, COMPONENTE, EQUIPO Y/O MATERIAL ASOCIADO",
                "ASSOCIATED PARTS, COMPONENTS, EQUIPMENT AND/OR MATERIALS");

        float headH = 20f;
        float[] anchos = {w * 0.26f, w * 0.08f, w * 0.13f, w * 0.16f, w * 0.16f, 0};
        anchos[5] = w - (anchos[0] + anchos[1] + anchos[2] + anchos[3] + anchos[4]);
        String[][] cols = {
                {"DESCRIPCIÓN", "DESCRIPTION"},
                {"CANT.", "QTY."},
                {"CONDICIÓN", "CONDITION"},
                {"NO. PARTE INSTALADO", "P/N ON"},
                {"NO. SERIE INSTALADO", "S/N ON"},
                {"NO. PARTE / SERIE REMOVIDO", "P/N - S/N OFF"}
        };

        cs.setNonStrokingColor(AZUL_MEDIO);
        cs.addRect(x, y - headH, w, headH);
        cs.fill();
        float cx = x;
        for (int i = 0; i < 6; i++) {
            textoCentrado(cs, fontBold(), 5.5f, Color.WHITE, cx, anchos[i], y - 8, cols[i][0]);
            textoCentrado(cs, fontItalic(), 4.5f, Color.WHITE, cx, anchos[i], y - 15, cols[i][1]);
            cx += anchos[i];
        }
        cs.setNonStrokingColor(Color.BLACK);
        y -= headH;

        int filas = 4;
        float rowH = 14f;
        for (int f = 0; f < filas; f++) {
            cx = x;
            for (int i = 0; i < 6; i++) {
                rect(cs, cx, y - rowH, anchos[i], rowH);
                cx += anchos[i];
            }
            if (f == 0 && tarea.getParteAsociada() != null && !tarea.getParteAsociada().isBlank()) {
                List<String> l = partirLineas(tarea.getParteAsociada(), fontRegular(), 6.5f, anchos[0] - 6);
                if (!l.isEmpty()) {
                    valor(cs, x + 4, y - 10, l.get(0), 6.5f);
                }
            }
            y -= rowH;
        }

        return y;
    }

    private void dibujarCertificacionFirmas(PDPageContentStream cs, float x, float y, float w,
                                            OTTareaMantenimiento tarea) throws IOException {
        PDType1Font reg = fontRegular();

        String certEs = "Certificamos que las prácticas, métodos, procedimientos y material utilizado en las tareas asignadas "
                + "se encuentran en total conformidad con las normativas/certificaciones vigentes y aplicables de mantenimiento, "
                + "seguridad y calidad en la industria de la aviación. Además de que la herramienta/equipo asociado que requiere "
                + "calibración está vigente.";
        List<String> lineasEs = partirLineas(certEs, reg, 6f, w - 12);
        float certH = lineasEs.size() * 7.5f + 8f;

        cs.setNonStrokingColor(AZUL_CLARO);
        cs.addRect(x, y - certH, w, certH);
        cs.fill();
        rect(cs, x, y - certH, w, certH);
        cs.setNonStrokingColor(Color.BLACK);

        float ty = y - 9;
        for (String linea : lineasEs) {
            cs.beginText();
            cs.setFont(reg, 6f);
            cs.newLineAtOffset(x + 6, ty);
            cs.showText(linea);
            cs.endText();
            ty -= 7.5f;
        }
        y -= certH;

        float headH = 18f;
        float c3 = w / 3;
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x, y - headH, w, headH);
        cs.fill();
        String[][] cols = {
                {"EFECTUADO POR:", "PERFORMED BY"},
                {"INSPECCIONADO POR:", "INSPECTED BY"},
                {"FECHA DE CUMPLIMIENTO", "COMPLETION DATE"}
        };
        for (int i = 0; i < 3; i++) {
            textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x + c3 * i, c3, y - 7, cols[i][0]);
            textoCentrado(cs, fontItalic(), 5.5f, Color.WHITE, x + c3 * i, c3, y - 14, cols[i][1]);
        }
        cs.setNonStrokingColor(Color.BLACK);
        y -= headH;

        float rowH = 26f;
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 3; i++) {
            cs.moveTo(x + c3 * i, y - rowH);
            cs.lineTo(x + c3 * i, y);
            cs.stroke();
        }
        textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, x, c3, y - 17, tarea.getEfectuadoPor());
        textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, x + c3, c3, y - 17, tarea.getInspeccionadoPor());
        String fechaCumpl = tarea.getFechaCumplimiento() != null ? tarea.getFechaCumplimiento().format(FMT_FECHA) : "";
        textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, x + c3 * 2, c3, y - 17, fechaCumpl);
    }

    @Override
    public byte[] generarDiscrepancia(Integer idOT, Integer idOTDiscrepancia) {
        NuevaOT ot = nuevaOTRepository.findById(idOT)
                .orElseThrow(() -> new RuntimeException("La Orden de Trabajo solicitada no existe."));

        List<OTDiscrepancia> discrepancias = ot.getDiscrepancias();
        if (discrepancias == null || discrepancias.isEmpty()) {
            OTDiscrepancia vacia = new OTDiscrepancia();
            vacia.setCodigo("AGD-01");
            vacia.setDescripcion("—");
            return construirDiscrepancias(ot, List.of(vacia), List.of(1));
        }

        int indice = 0;
        for (int i = 0; i < discrepancias.size(); i++) {
            Integer id = discrepancias.get(i).getIdOTDiscrepancia();
            if (id != null && id.equals(idOTDiscrepancia)) {
                indice = i;
                break;
            }
        }

        return construirDiscrepancias(ot, List.of(discrepancias.get(indice)), List.of(indice + 1));
    }

    @Override
    public byte[] generarDiscrepanciasTodas(Integer idOT) {
        NuevaOT ot = nuevaOTRepository.findById(idOT)
                .orElseThrow(() -> new RuntimeException("La Orden de Trabajo solicitada no existe."));

        List<OTDiscrepancia> discrepancias = ot.getDiscrepancias();

        // Si no hay discrepancias, generar hoja AG-145-12 con AGD-01 en blanco (evita 404)
        if (discrepancias == null || discrepancias.isEmpty()) {
            OTDiscrepancia vacia = new OTDiscrepancia();
            vacia.setCodigo("AGD-01");
            vacia.setDescripcion("—");
            discrepancias = List.of(vacia);
        }

        List<Integer> consecutivos = new java.util.ArrayList<>();
        for (int i = 0; i < discrepancias.size(); i++) {
            consecutivos.add(i + 1);
        }
        return construirDiscrepancias(ot, discrepancias, consecutivos);
    }

    private byte[] construirDiscrepancias(NuevaOT ot, List<OTDiscrepancia> discrepancias, List<Integer> consecutivos) {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float pageWidth = PDRectangle.A4.getWidth();
            float pageHeight = PDRectangle.A4.getHeight();
            float contentX = PAGE_MARGIN;
            float contentW = pageWidth - 2 * PAGE_MARGIN;

            String noControl = ot.getNoOT();
            String fecha = ot.getFechaCreacion() != null ? ot.getFechaCreacion().format(FMT_FECHA) : "";

            int porHoja = 2;
            int totalHojas = (int) Math.ceil(discrepancias.size() / (double) porHoja);

            for (int hoja = 0; hoja < totalHojas; hoja++) {
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    float y = pageHeight - PAGE_MARGIN;

                    y = dibujarEncabezado(doc, cs, pageWidth, y,
                            "DISCREPANCIAS", "DISCREPANCIES",
                            "ORDEN DE TRABAJO", noControl, fecha);

                    y -= 8;

                    int desde = hoja * porHoja;
                    int hasta = Math.min(desde + porHoja, discrepancias.size());
                    for (int i = desde; i < hasta; i++) {
                        y = dibujarBloqueDiscrepancia(cs, contentX, y, contentW,
                                discrepancias.get(i), consecutivos.get(i));
                        y -= 8;
                    }

                    dibujarPie(cs, pageWidth, "AG-145/12", hoja + 1, totalHojas);
                }
            }

            doc.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el formato de Discrepancias: " + e.getMessage(), e);
        }
    }

    private float dibujarBloqueDiscrepancia(PDPageContentStream cs, float x, float y, float w,
                                            OTDiscrepancia d, int consecutivo) throws IOException {
        float headH = 30f;
        float colAgd = w * 0.32f;
        float colTipo = w * 0.36f;
        float colHH = w * 0.13f;
        float colFecha = w - colAgd - colTipo - colHH;

        // Celda AGD-XX con color Cian oficial (#33CCCC) (Puntos 11 y 13.8)
        cs.setNonStrokingColor(CIAN_DISCREP);
        cs.addRect(x, y - headH, colAgd, headH);
        cs.fill();
        textoCentrado(cs, fontBold(), 8.5f, Color.WHITE, x, colAgd, y - 13,
                "DISCREPANCIA AGD-" + String.format("%02d", consecutivo));
        textoCentrado(cs, fontItalic(), 6.5f, Color.WHITE, x, colAgd, y - 22, "Discrepancy");

        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x + colAgd, y - 15f, colTipo + colHH + colFecha, 15f);
        cs.fill();
        textoCentrado(cs, fontBold(), 6f, Color.WHITE, x + colAgd, colTipo, y - 6.5f, "TIPO DE DISCREPANCIA");
        textoCentrado(cs, fontItalic(), 5f, Color.WHITE, x + colAgd, colTipo, y - 12.5f, "DISCREPANCY TYPE");
        textoCentrado(cs, fontBold(), 6f, Color.WHITE, x + colAgd + colTipo, colHH, y - 6.5f, "H.H. EST.");
        textoCentrado(cs, fontBold(), 6f, Color.WHITE, x + colAgd + colTipo + colHH, colFecha, y - 6.5f, "FECHA AUTORIZADA");
        textoCentrado(cs, fontItalic(), 5f, Color.WHITE, x + colAgd + colTipo + colHH, colFecha, y - 12.5f, "AUTHORIZED DATE");
        cs.setNonStrokingColor(Color.BLACK);

        float subY = y - 15f;
        float subH = headH - 15f;
        rect(cs, x + colAgd, subY - subH, colTipo, subH);
        rect(cs, x + colAgd + colTipo, subY - subH, colHH, subH);
        rect(cs, x + colAgd + colTipo + colHH, subY - subH, colFecha, subH);

        boolean porOperador = "OPERADOR".equalsIgnoreCase(d.getTipoDiscrepancia());
        boolean enServicio = "SERVICIO".equalsIgnoreCase(d.getTipoDiscrepancia());
        float mitadTipo = colTipo / 2f;
        checkbox(cs, x + colAgd + 4, subY - subH + 4, porOperador,
                "Reportada por el operador", "Reported by Operator", 5.5f);
        checkbox(cs, x + colAgd + mitadTipo + 4, subY - subH + 4, enServicio,
                "Generada en servicio", "Generated in Service", 5.5f);

        String hh = d.getHhEstimadas() != null ? d.getHhEstimadas().toString() : "";
        textoCentrado(cs, fontRegular(), 7f, Color.BLACK, x + colAgd + colTipo, colHH, subY - subH + 5, hh);
        String fechaAut = d.getFechaAutorizada() != null ? d.getFechaAutorizada().format(FMT_FECHA) : "";
        textoCentrado(cs, fontRegular(), 7f, Color.BLACK, x + colAgd + colTipo + colHH, colFecha, subY - subH + 5, fechaAut);

        y -= headH;

        float descH = 28f;
        rect(cs, x, y - descH, w, descH);
        List<String> lineasDesc = partirLineas(d.getDescripcion(), fontRegular(), 7f, w - 12);
        float ty = y - 10;
        for (String linea : lineasDesc) {
            if (ty < y - descH + 4) break;
            valor(cs, x + 5, ty, linea, 7f);
            ty -= 9;
        }
        y -= descH;

        y = dibujarBarraSeccion(cs, x, y, w, "ACCIÓN CORRECTIVA", "CORRECTIVE ACTION");

        float colFirmas = w * 0.30f;
        float colAccion = w - colFirmas;
        float bloqueH = 54f;

        rect(cs, x, y - bloqueH, colAccion, bloqueH);
        List<String> lineasAcc = partirLineas(d.getAccionCorrectiva(), fontRegular(), 7f, colAccion - 10);
        ty = y - 10;
        for (String linea : lineasAcc) {
            if (ty < y - bloqueH + 4) break;
            valor(cs, x + 5, ty, linea, 7f);
            ty -= 9;
        }

        float fx = x + colAccion;
        float parH = bloqueH / 3f;
        String fechaCumpl = d.getFechaLiberacion() != null ? d.getFechaLiberacion().format(FMT_FECHA) : "";
        String[][] firmas = {
                {"FECHA DE CUMPLIMIENTO:", "COMPLETION DATE", fechaCumpl},
                {"EFECTUADO POR:", "PERFORMED BY", d.getEfectuadoPor()},
                {"INSPECCIONADO POR:", "INSPECTED BY", d.getInspeccionadoPor()}
        };
        for (int i = 0; i < 3; i++) {
            float py = y - parH * i;
            float etiquetaH = parH * 0.45f;
            cs.setNonStrokingColor(AZUL_OSCURO);
            cs.addRect(fx, py - etiquetaH, colFirmas, etiquetaH);
            cs.fill();
            textoCentrado(cs, fontBold(), 5.5f, Color.WHITE, fx, colFirmas, py - etiquetaH + 5, firmas[i][0]);
            cs.setNonStrokingColor(Color.BLACK);
            rect(cs, fx, py - parH, colFirmas, parH - etiquetaH);
            textoCentrado(cs, fontRegular(), 6.5f, Color.BLACK, fx, colFirmas, py - parH + 4, firmas[i][2]);
        }
        y -= bloqueH;

        float colDescripcion = w * 0.24f;
        float restante = w - colDescripcion;

        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x, y - 12f, w, 12f);
        cs.fill();
        textoCentrado(cs, fontBold(), 6f, Color.WHITE, x, colDescripcion, y - 8.5f, "DESCRIPCIÓN");
        textoCentrado(cs, fontBold(), 6f, Color.WHITE, x + colDescripcion, restante, y - 8.5f,
                "PARTE, COMPONENTE, EQUIPO Y/O MATERIAL ASOCIADO");
        cs.setNonStrokingColor(Color.BLACK);
        y -= 12f;

        float[] anchos = {restante * 0.10f, restante * 0.16f, restante * 0.20f, restante * 0.20f, 0};
        anchos[4] = restante - (anchos[0] + anchos[1] + anchos[2] + anchos[3]);
        String[][] cols = {
                {"CANT.", "QTY."},
                {"CONDICIÓN", "CONDITION"},
                {"No. PARTE INSTALADO", "INSTALLED P/N"},
                {"No. SERIE INSTALADO", "INSTALLED S/N"},
                {"No. PARTE / SERIE REMOVIDO", "REMOVED P/N - S/N"}
        };
        float subHeadH = 18f;
        cs.setNonStrokingColor(AZUL_MEDIO);
        cs.addRect(x + colDescripcion, y - subHeadH, restante, subHeadH);
        cs.fill();
        float cx = x + colDescripcion;
        for (int i = 0; i < 5; i++) {
            textoCentrado(cs, fontBold(), 4.5f, Color.WHITE, cx, anchos[i], y - 7, cols[i][0]);
            textoCentrado(cs, fontItalic(), 4f, Color.WHITE, cx, anchos[i], y - 13, cols[i][1]);
            cx += anchos[i];
        }
        cs.setNonStrokingColor(Color.BLACK);
        y -= subHeadH;

        int filas = 3;
        float rowH = 12f;
        float alturaFilas = filas * rowH;
        rect(cs, x, y - alturaFilas, colDescripcion, alturaFilas);
        if (d.getParteAsociada() != null && !d.getParteAsociada().isBlank()) {
            List<String> l = partirLineas(d.getParteAsociada(), fontRegular(), 6f, colDescripcion - 6);
            float dy = y - 8;
            for (String linea : l) {
                if (dy < y - alturaFilas + 3) break;
                valor(cs, x + 3, dy, linea, 6f);
                dy -= 8;
            }
        }
        for (int f = 0; f < filas; f++) {
            cx = x + colDescripcion;
            for (int i = 0; i < 5; i++) {
                rect(cs, cx, y - rowH, anchos[i], rowH);
                cx += anchos[i];
            }
            y -= rowH;
        }

        return y;
    }
}