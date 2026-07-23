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

/**
 * P-02: Generación de la carátula oficial AG-145-03 (Orden de Trabajo).
 * Usa PDFBox 3.0.2 y el helper compartido AGFormatoPdfHelper.
 */
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

            PDType1Font bold = fontBold();
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

                // Encabezado institucional
                y = dibujarEncabezado(doc, cs, pageWidth, y,
                        "ORDEN DE TRABAJO", "WORK ORDER",
                        "NÚMERO DE CONTROL", noControl, fecha);

                y -= 6;

                // Declaración legal superior (recuadro con texto bilingüe)
                y = dibujarDeclaracionLegal(cs, contentX, y, contentW, reg, ita);

                y -= 8;

                // Tipo de Orden de Trabajo (4 checkboxes)
                y = dibujarTipoOT(cs, contentX, y, contentW, ot);

                y -= 8;

                // Información del Cliente / Operador
                y = dibujarInfoCliente(cs, contentX, y, contentW, ot);

                y -= 8;

                // Información de Aeronave
                y = dibujarInfoAeronave(cs, contentX, y, contentW, ot);

                // Pie de página
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

                y -= 10;

                // Información de Componente (solo si es reparación de componente)
                if ("COMPONENTE".equalsIgnoreCase(ot.getTipoMantenimiento())) {
                    y = dibujarInfoComponente(cs, contentX, y, contentW, ot);
                    y -= 10;
                }

                // Tabla de tareas
                y = dibujarTablaTareas(cs, contentX, y, contentW, ot.getTareasMantenimiento());

                y -= 10;

                // Comentarios del cliente
                y = dibujarBloqueComentario(cs, contentX, y, contentW,
                        "COMENTARIOS / REQUERIMIENTOS ADICIONALES DEL CLIENTE / OPERADOR",
                        "CLIENT / OPERATOR COMMENTS / ADDITIONAL REQUIREMENTS",
                        ot.getComentarioCliente());

                y -= 8;

                // Comentarios del taller
                y = dibujarBloqueComentario(cs, contentX, y, contentW,
                        "COMENTARIOS RESPONSABLE DE TALLER",
                        "WORKSHOP REPRESENTATIVE COMMENTS",
                        ot.getComentarioTaller());

                y -= 8;

                // Declaración de autorización y firma
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
    // Secciones
    // ---------------------------------------------------------------------

    private float dibujarDeclaracionLegal(PDPageContentStream cs, float x, float y, float w,
                                          PDType1Font reg, PDType1Font ita) throws IOException {
        String textoEs = "Se establece que quien firma se reconoce con la facultad necesaria para autorizar la ejecución de los "
                + "SERVICIOS DE MANTENIMIENTO PROGRAMADO/ NO PROGRAMADO Y/O REQUERIMIENTOS ADICIONALES para lo cual proporcionará "
                + "al Taller Aeronáutico las guías de inspección y mantenimiento, así como la información técnica aprobada por la "
                + "A.F.A.C. en su programa de mantenimiento. La presente Orden de Trabajo se genera en concordancia a los "
                + "requerimientos de la CO AV 145.01/24 párrafo 15.5";

        List<String> lineasEs = partirLineas(textoEs, reg, 7f, w - 16);
        float lineH = 8.5f;
        float boxH = (lineasEs.size() + 1) * lineH + 12;

        cs.setNonStrokingColor(AZUL_CLARO);
        cs.addRect(x, y - boxH, w, boxH);
        cs.fill();
        rect(cs, x, y - boxH, w, boxH);

        float ty = y - 12;
        cs.setNonStrokingColor(Color.BLACK);
        for (String linea : lineasEs) {
            cs.beginText();
            cs.setFont(reg, 7f);
            cs.newLineAtOffset(x + 8, ty);
            cs.showText(linea);
            cs.endText();
            ty -= lineH;
        }

        return y - boxH;
    }

    private float dibujarTipoOT(PDPageContentStream cs, float x, float y, float w, NuevaOT ot) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w, "TIPO DE ORDEN DE TRABAJO", "WORK ORDER TYPE");

        float rowH = 22f;
        float colW = w / 2;

        boolean esAeronave = "AERONAVE".equalsIgnoreCase(ot.getTipoMantenimiento());
        boolean esComponente = "COMPONENTE".equalsIgnoreCase(ot.getTipoMantenimiento());
        boolean esProgramado = "PROGRAMADO".equalsIgnoreCase(ot.getModalidadMantenimiento());
        boolean esNoProgramado = "NO_PROGRAMADO".equalsIgnoreCase(ot.getModalidadMantenimiento());

        // Fila 1
        rect(cs, x, y - rowH, colW, rowH);
        rect(cs, x + colW, y - rowH, colW, rowH);
        checkbox(cs, x + 8, y - 15, esAeronave, "MANTENIMIENTO DE AERONAVE", "AIRCRAFT MAINTENANCE");
        checkbox(cs, x + colW + 8, y - 15, esComponente, "REPARACIÓN DE COMPONENTE", "COMPONENT REPAIR");
        y -= rowH;

        // Fila 2
        rect(cs, x, y - rowH, colW, rowH);
        rect(cs, x + colW, y - rowH, colW, rowH);
        checkbox(cs, x + 8, y - 15, esProgramado, "MANTENIMIENTO PROGRAMADO", "SCHEDULED");
        checkbox(cs, x + colW + 8, y - 15, esNoProgramado, "MANTENIMIENTO NO PROGRAMADO", "UNSCHEDULED");
        y -= rowH;

        return y;
    }

    private float dibujarInfoCliente(PDPageContentStream cs, float x, float y, float w, NuevaOT ot) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w, "INFORMACIÓN DEL CLIENTE / OPERADOR", "CLIENT / OPERATOR INFORMATION");

        Cliente c = ot.getCliente();
        String operador = c != null ? c.getCompania() : "";
        String contacto = c != null ? c.getContacto() : "";
        String direccion = c != null ? c.getDireccion() : "";
        String telefono = c != null ? c.getTelefono() : "";
        String correo = c != null ? c.getCorreo() : "";

        float rowH = 26f;
        float colW = w / 2;

        // Fila 1: Operador / Contacto
        rect(cs, x, y - rowH, colW, rowH);
        rect(cs, x + colW, y - rowH, colW, rowH);
        etiquetaBilingue(cs, x + 5, y - 10, "OPERADOR / CLIENTE:", "OPERATOR / CUSTOMER", 6.5f);
        valor(cs, x + 5, y - 22, operador, 8f);
        etiquetaBilingue(cs, x + colW + 5, y - 10, "CONTACTO:", "CONTACT", 6.5f);
        valor(cs, x + colW + 5, y - 22, contacto, 8f);
        y -= rowH;

        // Fila 2: Dirección, Ciudad, Estado, Teléfono, Correo
        float c5 = w / 5;
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 5; i++) {
            cs.moveTo(x + c5 * i, y - rowH);
            cs.lineTo(x + c5 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 10, "DIRECCIÓN:", "ADDRESS", 6f);
        valor(cs, x + 4, y - 22, direccion, 6.5f);
        etiquetaBilingue(cs, x + c5 + 4, y - 10, "CIUDAD:", "CITY", 6f);
        etiquetaBilingue(cs, x + c5 * 2 + 4, y - 10, "ESTADO:", "STATE", 6f);
        etiquetaBilingue(cs, x + c5 * 3 + 4, y - 10, "TELÉFONO:", "PHONE", 6f);
        valor(cs, x + c5 * 3 + 4, y - 22, telefono, 6.5f);
        etiquetaBilingue(cs, x + c5 * 4 + 4, y - 10, "CORREO:", "E-MAIL", 6f);
        valor(cs, x + c5 * 4 + 4, y - 22, correo, 6f);
        y -= rowH;

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

        // Tipo de aeronave (checkboxes ALA FIJA / ALA ROTATIVA / OTRO)
        float rowH = 20f;
        float c4 = w / 4;
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 4; i++) {
            cs.moveTo(x + c4 * i, y - rowH);
            cs.lineTo(x + c4 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 12, "TIPO DE AERONAVE:", "AIRCRAFT TYPE", 6.5f);
        // NOTA: El sistema aún no distingue Ala Fija / Ala Rotativa (el campo `tipoAeronave`
        // del catálogo ModeloAeronave es provisional: Comercial/Carga/Privado). Hasta que
        // exista el catálogo oficial, los checkboxes se dejan sin marcar para llenado manual.
        boolean alaFija = false;
        boolean alaRotativa = false;
        checkbox(cs, x + c4 + 6, y - 13, alaFija, "ALA FIJA", "FIXED WING");
        checkbox(cs, x + c4 * 2 + 6, y - 13, alaRotativa, "ALA ROTATIVA", "ROTARY WING");
        etiquetaBilingue(cs, x + c4 * 3 + 6, y - 12, "OTRO / OTHER:", null, 6.5f);
        y -= rowH;

        // Por defecto se asume ala fija para el layout de unidades mayores (motores).
        // Cuando exista el dato real, este bloque debe reflejar el tipo correcto.
        boolean rotativa = alaRotativa;

        // Matrícula / Marca / Modelo
        rect(cs, x, y - rowH, w, rowH);
        float c3 = w / 3;
        for (int i = 1; i < 3; i++) {
            cs.moveTo(x + c3 * i, y - rowH);
            cs.lineTo(x + c3 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 9, "Matrícula / Registration:", null, 6.5f);
        valor(cs, x + 4, y - 18, matricula, 8f);
        etiquetaBilingue(cs, x + c3 + 4, y - 9, "Marca / Manufacturer:", null, 6.5f);
        valor(cs, x + c3 + 4, y - 18, marca, 8f);
        etiquetaBilingue(cs, x + c3 * 2 + 4, y - 9, "Modelo / Model:", null, 6.5f);
        valor(cs, x + c3 * 2 + 4, y - 18, modelo, 8f);
        y -= rowH;

        // Número de serie / Horas totales / Ciclos totales
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 3; i++) {
            cs.moveTo(x + c3 * i, y - rowH);
            cs.lineTo(x + c3 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 9, "Número de Serie / Serial number:", null, 6.5f);
        valor(cs, x + 4, y - 18, nsAeronave, 8f);
        etiquetaBilingue(cs, x + c3 + 4, y - 9, "Horas Totales / Total Hours:", null, 6.5f);
        valor(cs, x + c3 + 4, y - 18, ot.getHorasTotales() != null ? ot.getHorasTotales().toString() : "", 8f);
        etiquetaBilingue(cs, x + c3 * 2 + 4, y - 9, "Ciclos Totales / Total Cycles:", null, 6.5f);
        valor(cs, x + c3 * 2 + 4, y - 18, ot.getCiclosTotales() != null ? ot.getCiclosTotales().toString() : "", 8f);
        y -= rowH;

        // Unidades mayores (motores / APU) — encabezado
        rect(cs, x, y - rowH, w, rowH);
        float c5 = w / 5;
        for (int i = 1; i < 5; i++) {
            cs.moveTo(x + c5 * i, y - rowH);
            cs.lineTo(x + c5 * i, y);
            cs.stroke();
        }
        boolean rotativaLayout = rotativa;
        etiquetaBilingue(cs, x + 4, y - 12, rotativaLayout ? "Ala rotativa" : "Ala fija", rotativaLayout ? "Rotary Wing" : "Fixed Wing", 6.5f);
        etiquetaBilingue(cs, x + c5 + 4, y - 12, "Motor 1", "Engine 1 (LH)", 6.5f);
        etiquetaBilingue(cs, x + c5 * 2 + 4, y - 12, "Motor 2", "Engine 2 (RH)", 6.5f);
        etiquetaBilingue(cs, x + c5 * 3 + 4, y - 12, rotativa ? "Rotor principal" : "Motor 3", rotativa ? "Main rotor" : "Engine 3", 6.5f);
        etiquetaBilingue(cs, x + c5 * 4 + 4, y - 12, rotativa ? "Rotor de cola" : "UPA/Hélice", rotativa ? "Tail rotor" : "APU / Propeller", 6.5f);
        y -= rowH;

        // Tiempos de las unidades mayores
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 5; i++) {
            cs.moveTo(x + c5 * i, y - rowH);
            cs.lineTo(x + c5 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 12, "Horas / Ciclos", null, 6f);
        valor(cs, x + c5 + 4, y - 13, fmtHorasCiclos(ot.getTiempoMotor1(), ot.getCicloMotor1()), 6.5f);
        valor(cs, x + c5 * 2 + 4, y - 13, fmtHorasCiclos(ot.getTiempoMotor2(), ot.getCicloMotor2()), 6.5f);
        valor(cs, x + c5 * 3 + 4, y - 13, fmtHorasCiclos(ot.getTiempoMotor3(), ot.getCicloMotor3()), 6.5f);
        valor(cs, x + c5 * 4 + 4, y - 13, fmtHorasCiclos(ot.getTiempoAPU(), ot.getCicloAPU()), 6.5f);
        y -= rowH;

        return y;
    }

    private float dibujarInfoComponente(PDPageContentStream cs, float x, float y, float w, NuevaOT ot) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w, "INFORMACIÓN DE COMPONENTE", "COMPONENT INFORMATION");

        float rowH = 22f;
        float c3 = w / 3;

        // Fila 1: Descripción / No. parte / No. serie
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 3; i++) {
            cs.moveTo(x + c3 * i, y - rowH);
            cs.lineTo(x + c3 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 9, "Descripción / Description:", null, 6.5f);
        valor(cs, x + 4, y - 18, ot.getComponenteDescripcion(), 7.5f);
        etiquetaBilingue(cs, x + c3 + 4, y - 9, "Número de parte / Part number:", null, 6.5f);
        valor(cs, x + c3 + 4, y - 18, ot.getComponenteNumeroParte(), 7.5f);
        etiquetaBilingue(cs, x + c3 * 2 + 4, y - 9, "Número de Serie / Serial number:", null, 6.5f);
        valor(cs, x + c3 * 2 + 4, y - 18, ot.getComponenteNumeroSerie(), 7.5f);
        y -= rowH;

        // Fila 2: Cantidad / Horas / Ciclos
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 3; i++) {
            cs.moveTo(x + c3 * i, y - rowH);
            cs.lineTo(x + c3 * i, y);
            cs.stroke();
        }
        etiquetaBilingue(cs, x + 4, y - 9, "Cantidad / Quantity:", null, 6.5f);
        valor(cs, x + 4, y - 18, ot.getComponenteCantidad() != null ? ot.getComponenteCantidad().toString() : "", 7.5f);
        etiquetaBilingue(cs, x + c3 + 4, y - 9, "Horas Totales / Total Hours:", null, 6.5f);
        valor(cs, x + c3 + 4, y - 18, ot.getComponenteHoras() != null ? ot.getComponenteHoras().toString() : "", 7.5f);
        etiquetaBilingue(cs, x + c3 * 2 + 4, y - 9, "Ciclos Totales / Total Cycles:", null, 6.5f);
        valor(cs, x + c3 * 2 + 4, y - 18, ot.getComponenteCiclos() != null ? ot.getComponenteCiclos().toString() : "", 7.5f);
        y -= rowH;

        // Fila 3: Aeronave asociada / Horas-Ciclos remoción
        rect(cs, x, y - rowH, w, rowH);
        float c2 = w / 2;
        cs.moveTo(x + c2, y - rowH);
        cs.lineTo(x + c2, y);
        cs.stroke();
        etiquetaBilingue(cs, x + 4, y - 9, "Aeronave asociada / Associated aircraft:", null, 6.5f);
        valor(cs, x + 4, y - 18, ot.getComponenteAeronaveAsociada(), 7.5f);
        etiquetaBilingue(cs, x + c2 + 4, y - 9, "Horas / Ciclos Totales a remoción:", null, 6.5f);
        valor(cs, x + c2 + 4, y - 18, ot.getComponenteHorasCiclosRemocion(), 7.5f);
        y -= rowH;

        return y;
    }

    private float dibujarTablaTareas(PDPageContentStream cs, float x, float y, float w,
                                     List<OTTareaMantenimiento> tareas) throws IOException {
        // Encabezado de tabla: ITEM | TAREA | DESCRIPCIÓN
        float headH = 18f;
        float colItem = 40f;
        float colTarea = 120f;
        float colDesc = w - colItem - colTarea;

        cs.setNonStrokingColor(AZUL_MEDIO);
        cs.addRect(x, y - headH, w, headH);
        cs.fill();
        cs.setNonStrokingColor(Color.WHITE);
        textoCentrado(cs, fontBold(), 7.5f, Color.WHITE, x, colItem, y - 8, "ÍTEM");
        textoCentrado(cs, fontItalic(), 6f, Color.WHITE, x, colItem, y - 15, "ITEM");
        textoCentrado(cs, fontBold(), 7.5f, Color.WHITE, x + colItem, colTarea, y - 8, "TAREA");
        textoCentrado(cs, fontItalic(), 6f, Color.WHITE, x + colItem, colTarea, y - 15, "TASK");
        textoCentrado(cs, fontBold(), 7.5f, Color.WHITE, x + colItem + colTarea, colDesc, y - 8, "DESCRIPCIÓN");
        textoCentrado(cs, fontItalic(), 6f, Color.WHITE, x + colItem + colTarea, colDesc, y - 15, "Description");
        cs.setNonStrokingColor(Color.BLACK);
        y -= headH;

        // Filas (mínimo 5, o el número de tareas si es mayor)
        int totalFilas = Math.max(5, tareas != null ? tareas.size() : 0);
        float rowH = 18f;
        for (int i = 0; i < totalFilas; i++) {
            rect(cs, x, y - rowH, colItem, rowH);
            rect(cs, x + colItem, y - rowH, colTarea, rowH);
            rect(cs, x + colItem + colTarea, y - rowH, colDesc, rowH);

            textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, x, colItem, y - 12, String.format("%02d", i + 1));

            if (tareas != null && i < tareas.size()) {
                OTTareaMantenimiento t = tareas.get(i);
                valor(cs, x + colItem + 4, y - 12, t.getCodigo(), 7.5f);
                // Descripción recortada a una línea
                List<String> lineas = partirLineas(t.getDescripcion(), fontRegular(), 7f, colDesc - 8);
                if (!lineas.isEmpty()) {
                    valor(cs, x + colItem + colTarea + 4, y - 12, lineas.get(0), 7f);
                }
            }
            y -= rowH;
        }

        return y;
    }

    private float dibujarBloqueComentario(PDPageContentStream cs, float x, float y, float w,
                                          String tituloEs, String tituloEn, String contenido) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w, tituloEs, tituloEn);

        float boxH = 40f;
        rect(cs, x, y - boxH, w, boxH);

        if (contenido != null && !contenido.isBlank()) {
            List<String> lineas = partirLineas(contenido, fontRegular(), 7.5f, w - 12);
            float ty = y - 12;
            for (String linea : lineas) {
                if (ty < y - boxH + 6) break; // no desbordar
                valor(cs, x + 6, ty, linea, 7.5f);
                ty -= 10;
            }
        }

        return y - boxH;
    }

    private void dibujarFirmaResponsable(PDPageContentStream cs, float x, float y, float w,
                                         PDType1Font reg, PDType1Font ita) throws IOException {
        // Declaración de autorización
        String decl = "DECLARACIÓN DE AUTORIZACIÓN: Quien firma la presente Orden de Trabajo se reconoce con la facultad "
                + "necesaria para autorizar la ejecución de los servicios de mantenimiento programado / no programado y/o la "
                + "reparación de componente descrita. La presente OT se emite conforme a CO AV 145.01/24 párrafos 15.4, 15.5 y 15.6.";
        List<String> lineas = partirLineas(decl, reg, 6.5f, w - 12);
        float declH = lineas.size() * 8 + 10;

        cs.setNonStrokingColor(AZUL_CLARO);
        cs.addRect(x, y - declH, w, declH);
        cs.fill();
        rect(cs, x, y - declH, w, declH);
        cs.setNonStrokingColor(Color.BLACK);

        float ty = y - 10;
        for (String linea : lineas) {
            cs.beginText();
            cs.setFont(reg, 6.5f);
            cs.newLineAtOffset(x + 6, ty);
            cs.showText(linea);
            cs.endText();
            ty -= 8;
        }
        y -= declH;

        // Bloques de firma: Responsable de taller
        float rowH = 20f;
        String[][] campos = {
                {"RESPONSABLE DE TALLER", "WORKSHOP REPRESENTATIVE"},
                {"FIRMA / SIGNATURE:", null},
                {"NOMBRE / NAME:", null},
                {"FECHA / DATE:", null}
        };
        for (String[] campo : campos) {
            rect(cs, x, y - rowH, w, rowH);
            etiquetaBilingue(cs, x + 6, y - 13, campo[0], campo[1], 7f);
            y -= rowH;
        }
    }

    // =====================================================================
    // P-03: HOJA DE SERVICIO AG-145-04
    // =====================================================================

    @Override
    public byte[] generarHojaServicio(Integer idOT, Integer idTareaOT) {
        NuevaOT ot = nuevaOTRepository.findById(idOT)
                .orElseThrow(() -> new RuntimeException("La Orden de Trabajo solicitada no existe."));

        List<OTTareaMantenimiento> tareas = ot.getTareasMantenimiento();
        if (tareas == null || tareas.isEmpty()) {
            throw new RuntimeException("La Orden de Trabajo no tiene tareas de mantenimiento registradas.");
        }

        // Localizar la tarea y su número de ítem dentro de la OT
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
            throw new RuntimeException("La Orden de Trabajo no tiene tareas de mantenimiento registradas.");
        }

        List<Integer> items = new java.util.ArrayList<>();
        for (int i = 0; i < tareas.size(); i++) {
            items.add(i + 1);
        }
        return construirHojasServicio(ot, tareas, items);
    }

    /**
     * Construye el documento con una Hoja de Servicio por cada tarea recibida.
     * `items` contiene el número de ítem de cada tarea dentro de la OT.
     */
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

                    // Encabezado: el recuadro de control muestra la OT a la que pertenece
                    String fechaCumpl = tarea.getFechaCumplimiento() != null
                            ? tarea.getFechaCumplimiento().format(FMT_FECHA)
                            : "";
                    y = dibujarEncabezado(doc, cs, pageWidth, y,
                            "HOJA DE SERVICIO", "JOB CARD",
                            "ORDEN DE TRABAJO", noControl, fechaCumpl);

                    y -= 8;

                    // I. Datos generales
                    y = dibujarDatosGenerales(cs, contentX, y, contentW, ot, tarea, item);
                    y -= 6;

                    // II. Descripción de la tarea
                    y = dibujarDescripcionTarea(cs, contentX, y, contentW, tarea);
                    y -= 6;

                    // III. Tipo de tarea + P/N, S/N + RII
                    y = dibujarTipoTarea(cs, contentX, y, contentW, tarea);
                    y -= 6;

                    // IV. Acción correctiva
                    y = dibujarAccionCorrectiva(cs, contentX, y, contentW, tarea);
                    y -= 6;

                    // V. Parte, componente, equipo y/o material asociado
                    y = dibujarPartesAsociadas(cs, contentX, y, contentW, tarea);
                    y -= 6;

                    // VI. Certificación y firmas
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
        float headH = 22f;
        float rowH = 20f;
        float c4 = w / 4;

        // Encabezados con fondo azul
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
            textoCentrado(cs, fontBold(), 7f, Color.WHITE, x + c4 * i, c4, y - 9, cols[i][0]);
            textoCentrado(cs, fontItalic(), 6f, Color.WHITE, x + c4 * i, c4, y - 17, cols[i][1]);
        }
        y -= headH;

        // Valores
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 4; i++) {
            cs.moveTo(x + c4 * i, y - rowH);
            cs.lineTo(x + c4 * i, y);
            cs.stroke();
        }
        String operador = ot.getCliente() != null ? ot.getCliente().getCompania() : "";
        String matricula = ot.getMatricula() != null ? ot.getMatricula().getMatricula() : "";
        String hh = tarea.getHorasTotales() != null ? tarea.getHorasTotales().toString() : "";

        textoCentrado(cs, fontRegular(), 8f, Color.BLACK, x, c4, y - 13, operador);
        textoCentrado(cs, fontRegular(), 8f, Color.BLACK, x + c4, c4, y - 13, matricula);
        textoCentrado(cs, fontRegular(), 8f, Color.BLACK, x + c4 * 2, c4, y - 13, hh);
        textoCentrado(cs, fontRegular(), 8f, Color.BLACK, x + c4 * 3, c4, y - 13, String.format("%02d", item));
        y -= rowH;

        return y;
    }

    private float dibujarDescripcionTarea(PDPageContentStream cs, float x, float y, float w,
                                          OTTareaMantenimiento tarea) throws IOException {
        float headH = 22f;
        float colTarea = w * 0.25f;
        float colDesc = w * 0.50f;
        float colInterv = w - colTarea - colDesc;

        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x, y - headH, w, headH);
        cs.fill();
        textoCentrado(cs, fontBold(), 7f, Color.WHITE, x, colTarea, y - 9, "TAREA");
        textoCentrado(cs, fontItalic(), 6f, Color.WHITE, x, colTarea, y - 17, "TASK");
        textoCentrado(cs, fontBold(), 7f, Color.WHITE, x + colTarea, colDesc, y - 9, "DESCRIPCIÓN");
        textoCentrado(cs, fontItalic(), 6f, Color.WHITE, x + colTarea, colDesc, y - 17, "DESCRIPTION");
        textoCentrado(cs, fontBold(), 7f, Color.WHITE, x + colTarea + colDesc, colInterv, y - 9, "INTERVALO");
        textoCentrado(cs, fontItalic(), 6f, Color.WHITE, x + colTarea + colDesc, colInterv, y - 17, "INTERVAL");
        cs.setNonStrokingColor(Color.BLACK);
        y -= headH;

        // Fila de valores: la descripción puede necesitar varias líneas
        List<String> lineasDesc = partirLineas(tarea.getDescripcion(), fontRegular(), 7.5f, colDesc - 8);
        float rowH = Math.max(24f, lineasDesc.size() * 10f + 10f);

        rect(cs, x, y - rowH, w, rowH);
        cs.moveTo(x + colTarea, y - rowH);
        cs.lineTo(x + colTarea, y);
        cs.stroke();
        cs.moveTo(x + colTarea + colDesc, y - rowH);
        cs.lineTo(x + colTarea + colDesc, y);
        cs.stroke();

        valor(cs, x + 5, y - 14, tarea.getCodigo(), 8f);
        float ty = y - 13;
        for (String linea : lineasDesc) {
            valor(cs, x + colTarea + 5, ty, linea, 7.5f);
            ty -= 10;
        }
        valor(cs, x + colTarea + colDesc + 5, y - 14, tarea.getIntervalo(), 7.5f);
        y -= rowH;

        return y;
    }

    private float dibujarTipoTarea(PDPageContentStream cs, float x, float y, float w,
                                   OTTareaMantenimiento tarea) throws IOException {
        float headH = 22f;
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
            textoCentrado(cs, fontBold(), i == 3 ? 6f : 7f, Color.WHITE, x + c4 * i, c4, y - 9, cols[i][0]);
            textoCentrado(cs, fontItalic(), 5.5f, Color.WHITE, x + c4 * i, c4, y - 17, cols[i][1]);
        }
        cs.setNonStrokingColor(Color.BLACK);
        y -= headH;

        float rowH = 26f;
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 4; i++) {
            cs.moveTo(x + c4 * i, y - rowH);
            cs.lineTo(x + c4 * i, y);
            cs.stroke();
        }

        textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, x, c4, y - 16, tarea.getTipoTarea());
        textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, x + c4, c4, y - 16, tarea.getNumeroParte());
        textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, x + c4 * 2, c4, y - 16, tarea.getNumeroSerie());

        // Checkboxes SI / NO para RII
        boolean rii = "SI".equalsIgnoreCase(tarea.getRequiereRII());
        boolean noRii = "NO".equalsIgnoreCase(tarea.getRequiereRII());
        float baseX = x + c4 * 3;
        textoCentrado(cs, fontBold(), 6.5f, Color.BLACK, baseX, c4 / 2, y - 9, "SI (YES)");
        textoCentrado(cs, fontBold(), 6.5f, Color.BLACK, baseX + c4 / 2, c4 / 2, y - 9, "NO");
        checkbox(cs, baseX + c4 / 4 - 4, y - 22, rii, null, null);
        checkbox(cs, baseX + c4 * 3 / 4 - 4, y - 22, noRii, null, null);
        y -= rowH;

        return y;
    }

    private float dibujarAccionCorrectiva(PDPageContentStream cs, float x, float y, float w,
                                          OTTareaMantenimiento tarea) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w, "ACCIÓN CORRECTIVA", "CORRECTIVE ACTION");

        float boxH = 62f;
        rect(cs, x, y - boxH, w, boxH);

        List<String> lineas = partirLineas(tarea.getAccionCorrectiva(), fontRegular(), 7.5f, w - 14);
        float ty = y - 12;
        for (String linea : lineas) {
            if (ty < y - boxH + 8) break;
            valor(cs, x + 7, ty, linea, 7.5f);
            ty -= 10;
        }

        return y - boxH;
    }

    private float dibujarPartesAsociadas(PDPageContentStream cs, float x, float y, float w,
                                         OTTareaMantenimiento tarea) throws IOException {
        y = dibujarBarraSeccion(cs, x, y, w,
                "PARTE, COMPONENTE, EQUIPO Y/O MATERIAL ASOCIADO",
                "ASSOCIATED PARTS, COMPONENTS, EQUIPMENT AND/OR MATERIALS");

        // Encabezado de la tabla de 6 columnas
        float headH = 24f;
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
            textoCentrado(cs, fontBold(), 5.5f, Color.WHITE, cx, anchos[i], y - 10, cols[i][0]);
            textoCentrado(cs, fontItalic(), 5f, Color.WHITE, cx, anchos[i], y - 18, cols[i][1]);
            cx += anchos[i];
        }
        cs.setNonStrokingColor(Color.BLACK);
        y -= headH;

        // Filas: la primera lleva el texto libre capturado en `parteAsociada`
        int filas = 5;
        float rowH = 16f;
        for (int f = 0; f < filas; f++) {
            cx = x;
            for (int i = 0; i < 6; i++) {
                rect(cs, cx, y - rowH, anchos[i], rowH);
                cx += anchos[i];
            }
            if (f == 0 && tarea.getParteAsociada() != null && !tarea.getParteAsociada().isBlank()) {
                List<String> l = partirLineas(tarea.getParteAsociada(), fontRegular(), 6.5f, anchos[0] - 6);
                if (!l.isEmpty()) {
                    valor(cs, x + 4, y - 11, l.get(0), 6.5f);
                }
            }
            y -= rowH;
        }

        return y;
    }

    private void dibujarCertificacionFirmas(PDPageContentStream cs, float x, float y, float w,
                                            OTTareaMantenimiento tarea) throws IOException {
        PDType1Font reg = fontRegular();
        PDType1Font ita = fontItalic();

        // Texto de certificación
        String certEs = "Certificamos que las prácticas, métodos, procedimientos y material utilizado en las tareas asignadas "
                + "se encuentran en total conformidad con las normativas/certificaciones vigentes y aplicables de mantenimiento, "
                + "seguridad y calidad en la industria de la aviación. Además de que la herramienta/equipo asociado que requiere "
                + "calibración está vigente.";
        List<String> lineasEs = partirLineas(certEs, reg, 6.5f, w - 14);
        float certH = lineasEs.size() * 8f + 12f;

        cs.setNonStrokingColor(AZUL_CLARO);
        cs.addRect(x, y - certH, w, certH);
        cs.fill();
        rect(cs, x, y - certH, w, certH);
        cs.setNonStrokingColor(Color.BLACK);

        float ty = y - 11;
        for (String linea : lineasEs) {
            cs.beginText();
            cs.setFont(reg, 6.5f);
            cs.newLineAtOffset(x + 7, ty);
            cs.showText(linea);
            cs.endText();
            ty -= 8;
        }
        y -= certH;

        // Encabezados de firmas
        float headH = 22f;
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
            textoCentrado(cs, fontBold(), 7f, Color.WHITE, x + c3 * i, c3, y - 9, cols[i][0]);
            textoCentrado(cs, fontItalic(), 6f, Color.WHITE, x + c3 * i, c3, y - 17, cols[i][1]);
        }
        cs.setNonStrokingColor(Color.BLACK);
        y -= headH;

        // Valores de firmas
        float rowH = 30f;
        rect(cs, x, y - rowH, w, rowH);
        for (int i = 1; i < 3; i++) {
            cs.moveTo(x + c3 * i, y - rowH);
            cs.lineTo(x + c3 * i, y);
            cs.stroke();
        }
        textoCentrado(cs, fontRegular(), 8f, Color.BLACK, x, c3, y - 20, tarea.getEfectuadoPor());
        textoCentrado(cs, fontRegular(), 8f, Color.BLACK, x + c3, c3, y - 20, tarea.getInspeccionadoPor());
        String fechaCumpl = tarea.getFechaCumplimiento() != null ? tarea.getFechaCumplimiento().format(FMT_FECHA) : "";
        textoCentrado(cs, fontRegular(), 8f, Color.BLACK, x + c3 * 2, c3, y - 20, fechaCumpl);
    }

    // =====================================================================
    // P-04: DISCREPANCIAS AG-145-12
    // =====================================================================

    @Override
    public byte[] generarDiscrepancia(Integer idOT, Integer idOTDiscrepancia) {
        NuevaOT ot = nuevaOTRepository.findById(idOT)
                .orElseThrow(() -> new RuntimeException("La Orden de Trabajo solicitada no existe."));

        List<OTDiscrepancia> discrepancias = ot.getDiscrepancias();
        if (discrepancias == null || discrepancias.isEmpty()) {
            throw new RuntimeException("La Orden de Trabajo no tiene discrepancias registradas.");
        }

        int indice = -1;
        for (int i = 0; i < discrepancias.size(); i++) {
            Integer id = discrepancias.get(i).getIdOTDiscrepancia();
            if (id != null && id.equals(idOTDiscrepancia)) {
                indice = i;
                break;
            }
        }
        if (indice < 0) {
            throw new RuntimeException("La discrepancia indicada no pertenece a esta Orden de Trabajo.");
        }

        // Conserva el consecutivo AGD que le corresponde dentro de la OT
        return construirDiscrepancias(ot, List.of(discrepancias.get(indice)), List.of(indice + 1));
    }

    @Override
    public byte[] generarDiscrepanciasTodas(Integer idOT) {
        NuevaOT ot = nuevaOTRepository.findById(idOT)
                .orElseThrow(() -> new RuntimeException("La Orden de Trabajo solicitada no existe."));

        List<OTDiscrepancia> discrepancias = ot.getDiscrepancias();
        if (discrepancias == null || discrepancias.isEmpty()) {
            throw new RuntimeException("La Orden de Trabajo no tiene discrepancias registradas.");
        }

        List<Integer> consecutivos = new java.util.ArrayList<>();
        for (int i = 0; i < discrepancias.size(); i++) {
            consecutivos.add(i + 1);
        }
        return construirDiscrepancias(ot, discrepancias, consecutivos);
    }

    /**
     * Construye el documento AG-145-12 con dos discrepancias por hoja.
     * `consecutivos` contiene el número AGD de cada discrepancia dentro de la OT.
     */
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

                    y -= 10;

                    int desde = hoja * porHoja;
                    int hasta = Math.min(desde + porHoja, discrepancias.size());
                    for (int i = desde; i < hasta; i++) {
                        y = dibujarBloqueDiscrepancia(cs, contentX, y, contentW,
                                discrepancias.get(i), consecutivos.get(i));
                        y -= 10;
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

    /**
     * Dibuja un bloque completo de discrepancia (encabezado AGD, descripción,
     * acción correctiva con firmas laterales y tabla de partes asociadas).
     */
    private float dibujarBloqueDiscrepancia(PDPageContentStream cs, float x, float y, float w,
                                            OTDiscrepancia d, int consecutivo) throws IOException {
        // ---- Encabezado del bloque ----
        float headH = 34f;
        float colAgd = w * 0.32f;
        float colTipo = w * 0.36f;
        float colHH = w * 0.13f;
        float colFecha = w - colAgd - colTipo - colHH;

        // Celda AGD-XX con fondo turquesa
        cs.setNonStrokingColor(AZUL_MEDIO);
        cs.addRect(x, y - headH, colAgd, headH);
        cs.fill();
        textoCentrado(cs, fontBold(), 9f, Color.WHITE, x, colAgd, y - 15,
                "DISCREPANCIA AGD-" + String.format("%02d", consecutivo));
        textoCentrado(cs, fontItalic(), 7f, Color.WHITE, x, colAgd, y - 25, "Discrepancy");

        // Encabezados azules de tipo / HH / fecha autorizada
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x + colAgd, y - 16f, colTipo + colHH + colFecha, 16f);
        cs.fill();
        textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x + colAgd, colTipo, y - 7, "TIPO DE DISCREPANCIA");
        textoCentrado(cs, fontItalic(), 5.5f, Color.WHITE, x + colAgd, colTipo, y - 14, "DISCREPANCY TYPE");
        textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x + colAgd + colTipo, colHH, y - 7, "H.H. EST.");
        textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x + colAgd + colTipo + colHH, colFecha, y - 7, "FECHA AUTORIZADA");
        textoCentrado(cs, fontItalic(), 5.5f, Color.WHITE, x + colAgd + colTipo + colHH, colFecha, y - 14, "AUTHORIZED DATE");
        cs.setNonStrokingColor(Color.BLACK);

        // Fila de valores del encabezado
        float subY = y - 16f;
        float subH = headH - 16f;
        rect(cs, x + colAgd, subY - subH, colTipo, subH);
        rect(cs, x + colAgd + colTipo, subY - subH, colHH, subH);
        rect(cs, x + colAgd + colTipo + colHH, subY - subH, colFecha, subH);

        boolean porOperador = "OPERADOR".equalsIgnoreCase(d.getTipoDiscrepancia());
        boolean enServicio = "SERVICIO".equalsIgnoreCase(d.getTipoDiscrepancia());
        // Etiquetas a 6pt: a 7.5pt "Reportada por el operador" se desborda sobre la
        // segunda casilla, porque la columna solo ofrece ~96pt por opción.
        float mitadTipo = colTipo / 2f;
        checkbox(cs, x + colAgd + 4, subY - subH + 5, porOperador,
                "Reportada por el operador", "Reported by Operator", 6f);
        checkbox(cs, x + colAgd + mitadTipo + 4, subY - subH + 5, enServicio,
                "Generada en servicio", "Generated in Service", 6f);

        String hh = d.getHhEstimadas() != null ? d.getHhEstimadas().toString() : "";
        textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, x + colAgd + colTipo, colHH, subY - subH + 6, hh);
        String fechaAut = d.getFechaAutorizada() != null ? d.getFechaAutorizada().format(FMT_FECHA) : "";
        textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, x + colAgd + colTipo + colHH, colFecha, subY - subH + 6, fechaAut);

        y -= headH;

        // ---- Descripción de la discrepancia ----
        float descH = 34f;
        rect(cs, x, y - descH, w, descH);
        List<String> lineasDesc = partirLineas(d.getDescripcion(), fontRegular(), 7.5f, w - 12);
        float ty = y - 12;
        for (String linea : lineasDesc) {
            if (ty < y - descH + 6) break;
            valor(cs, x + 6, ty, linea, 7.5f);
            ty -= 10;
        }
        y -= descH;

        // ---- Barra ACCIÓN CORRECTIVA ----
        y = dibujarBarraSeccion(cs, x, y, w, "ACCIÓN CORRECTIVA", "CORRECTIVE ACTION");

        // ---- Acción correctiva (izquierda) + firmas (derecha) ----
        float colFirmas = w * 0.30f;
        float colAccion = w - colFirmas;
        float bloqueH = 66f;

        rect(cs, x, y - bloqueH, colAccion, bloqueH);
        List<String> lineasAcc = partirLineas(d.getAccionCorrectiva(), fontRegular(), 7.5f, colAccion - 12);
        ty = y - 12;
        for (String linea : lineasAcc) {
            if (ty < y - bloqueH + 6) break;
            valor(cs, x + 6, ty, linea, 7.5f);
            ty -= 10;
        }

        // Tres pares etiqueta/valor apilados a la derecha
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
            // Etiqueta con fondo azul
            cs.setNonStrokingColor(AZUL_OSCURO);
            cs.addRect(fx, py - etiquetaH, colFirmas, etiquetaH);
            cs.fill();
            textoCentrado(cs, fontBold(), 6f, Color.WHITE, fx, colFirmas, py - etiquetaH + 6, firmas[i][0]);
            cs.setNonStrokingColor(Color.BLACK);
            // Valor
            rect(cs, fx, py - parH, colFirmas, parH - etiquetaH);
            textoCentrado(cs, fontRegular(), 7f, Color.BLACK, fx, colFirmas, py - parH + 5, firmas[i][2]);
        }
        y -= bloqueH;

        // ---- Tabla: DESCRIPCIÓN + PARTE, COMPONENTE, EQUIPO Y/O MATERIAL ASOCIADO ----
        float colDescripcion = w * 0.24f;
        float restante = w - colDescripcion;
        float headTablaH = 26f;

        // Encabezado combinado
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x, y - 14f, w, 14f);
        cs.fill();
        textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x, colDescripcion, y - 9, "DESCRIPCIÓN");
        textoCentrado(cs, fontBold(), 6.5f, Color.WHITE, x + colDescripcion, restante, y - 9,
                "PARTE, COMPONENTE, EQUIPO Y/O MATERIAL ASOCIADO");
        cs.setNonStrokingColor(Color.BLACK);
        y -= 14f;

        // Subencabezados de las 5 columnas de partes
        float[] anchos = {restante * 0.10f, restante * 0.16f, restante * 0.20f, restante * 0.20f, 0};
        anchos[4] = restante - (anchos[0] + anchos[1] + anchos[2] + anchos[3]);
        String[][] cols = {
                {"CANT.", "QTY."},
                {"CONDICIÓN", "CONDITION"},
                {"No. PARTE INSTALADO", "INSTALLED P/N"},
                {"No. SERIE INSTALADO", "INSTALLED S/N"},
                {"No. PARTE / SERIE REMOVIDO", "REMOVED P/N - S/N"}
        };
        float subHeadH = headTablaH - 14f + 12f;
        cs.setNonStrokingColor(AZUL_MEDIO);
        cs.addRect(x + colDescripcion, y - subHeadH, restante, subHeadH);
        cs.fill();
        float cx = x + colDescripcion;
        for (int i = 0; i < 5; i++) {
            textoCentrado(cs, fontBold(), 5f, Color.WHITE, cx, anchos[i], y - 9, cols[i][0]);
            textoCentrado(cs, fontItalic(), 4.5f, Color.WHITE, cx, anchos[i], y - 16, cols[i][1]);
            cx += anchos[i];
        }
        cs.setNonStrokingColor(Color.BLACK);
        // Celda de descripción (columna izquierda, ocupa el alto del subencabezado + filas)
        y -= subHeadH;

        // Filas de partes
        int filas = 3;
        float rowH = 14f;
        float alturaFilas = filas * rowH;
        rect(cs, x, y - alturaFilas, colDescripcion, alturaFilas);
        if (d.getParteAsociada() != null && !d.getParteAsociada().isBlank()) {
            List<String> l = partirLineas(d.getParteAsociada(), fontRegular(), 6.5f, colDescripcion - 8);
            float dy = y - 10;
            for (String linea : l) {
                if (dy < y - alturaFilas + 4) break;
                valor(cs, x + 4, dy, linea, 6.5f);
                dy -= 9;
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

    // ---------------------------------------------------------------------
    // Utilidades locales
    // ---------------------------------------------------------------------

    private String fmtHorasCiclos(java.math.BigDecimal horas, Integer ciclos) {
        String h = horas != null ? horas.toString() : "-";
        String c = ciclos != null ? ciclos.toString() : "-";
        return h + " / " + c;
    }
}