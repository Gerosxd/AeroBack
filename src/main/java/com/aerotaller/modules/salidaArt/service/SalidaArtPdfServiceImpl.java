package com.aerotaller.modules.salidaArt.service;

import com.aerotaller.modelos.Articulo;
import com.aerotaller.modelos.DetalleSalidaArt;
import com.aerotaller.modelos.SalidaArt;
import com.aerotaller.modules.catalogo.repository.CondicionRepository;
import com.aerotaller.modules.ot.service.AGFormatoPdfHelper;
import com.aerotaller.modules.salidaArt.dto.SalidaArtExportRequestDto;
import com.aerotaller.modules.salidaArt.repository.SalidaArtRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.aerotaller.modules.ot.service.AGFormatoPdfHelper.*;

/**
 * P-05: Generación del formato oficial AG-145-22 (Salida de Almacén).
 * Reescrito sobre el helper compartido AGFormatoPdfHelper para mantener
 * consistencia con el resto de formatos institucionales AG-145-XX.
 */
@Service
public class SalidaArtPdfServiceImpl implements SalidaArtPdfService {

    private final SalidaArtRepository repository;
    private final CondicionRepository condicionRepository;

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public SalidaArtPdfServiceImpl(SalidaArtRepository repository,
                                   CondicionRepository condicionRepository) {
        this.repository = repository;
        this.condicionRepository = condicionRepository;
    }

    @Override
    public byte[] generarPdfSalida(Integer idSalida, SalidaArtExportRequestDto exportDto) {
        SalidaArt salida = repository.findById(idSalida)
                .orElseThrow(() -> new RuntimeException("Salida no encontrada."));

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float pageWidth = PDRectangle.A4.getWidth();
            float pageHeight = PDRectangle.A4.getHeight();
            float x = PAGE_MARGIN;
            float w = pageWidth - 2 * PAGE_MARGIN;

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = pageHeight - PAGE_MARGIN;

                // 1. Encabezado propio del formato (logo + razón social + No. de salida)
                y = dibujarEncabezadoSalida(doc, cs, pageWidth, y, salida);
                y -= 10;

                // 2. Destinatario y dirección
                y = dibujarDestinatario(cs, x, y, w, salida);
                y -= 10;

                // 3. Fecha y Referencia
                y = dibujarFechaReferencia(cs, x, y, w, salida);
                y -= 12;

                // 4. Tabla de partidas
                y = dibujarTablaPartidas(cs, x, y, w, salida.getDetalles());
                y -= 10;

                // 5. Nota de condiciones
                y = dibujarNotaCondiciones(cs, x, y, w);
                y -= 10;

                // 6. Preguntas de verificación SI/NO
                y = dibujarPreguntasVerificacion(cs, x, y, w);
                y -= 14;

                // 7. Bloques de firma
                y = dibujarFirmas(cs, x, y, w, salida, exportDto);
                y -= 10;

                // 8. Aviso de confidencialidad
                dibujarAvisoConfidencialidad(cs, x, y, w);

                dibujarPie(cs, pageWidth, "AG-145/22", 1, 1);
            }

            doc.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF de Salida: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------
    // Secciones del formato
    // ---------------------------------------------------------------------

    /**
     * Encabezado AG-145-22: logo a la izquierda, razón social centrada y
     * recuadro "NO. DE SALIDA" a la derecha.
     */
    private float dibujarEncabezadoSalida(PDDocument doc, PDPageContentStream cs, float pageWidth,
                                          float topY, SalidaArt salida) throws IOException {
        float y = topY;
        float headerH = 72f;

        // Logo
        try (InputStream is = new ClassPathResource("reports/assets/logo_ag.png").getInputStream()) {
            PDImageXObject logo = PDImageXObject.createFromByteArray(doc, is.readAllBytes(), "logo_ag");
            float logoW = 110f;
            float logoH = logoW * ((float) logo.getHeight() / logo.getWidth());
            cs.drawImage(logo, PAGE_MARGIN + 10, y - headerH / 2 - logoH / 2, logoW, logoH);
        } catch (Exception ignored) {
            // Continuar sin logo si el asset no está disponible
        }

        // Razón social centrada
        float centerX = PAGE_MARGIN + 140f;
        float centerW = pageWidth - PAGE_MARGIN - 140f - 160f - PAGE_MARGIN;
        textoCentrado(cs, fontBold(), 10f, AZUL_MEDIO, centerX, centerW, y - 14, "AG AVIATION SUPPLIERS S.A DE C.V.");
        textoCentrado(cs, fontBold(), 10f, AZUL_MEDIO, centerX, centerW, y - 28, "TALLER AERONÁUTICO AUTORIZADO");
        textoCentrado(cs, fontBold(), 10f, AZUL_MEDIO, centerX, centerW, y - 42, "A.F.A.C NO. 505.");
        textoCentrado(cs, fontBold(), 11f, Color.BLACK, centerX, centerW, y - 62, "SALIDA DE ALMACÉN");

        // Recuadro NO. DE SALIDA
        float boxW = 150f;
        float boxX = pageWidth - PAGE_MARGIN - boxW;
        float bandH = 18f;
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(boxX, y - bandH, boxW, bandH);
        cs.fill();
        textoCentrado(cs, fontBold(), 8.5f, Color.WHITE, boxX, boxW, y - 12, "NO. DE SALIDA:");
        cs.setNonStrokingColor(Color.BLACK);
        rect(cs, boxX, y - bandH - 26f, boxW, 26f);
        textoCentrado(cs, fontBold(), 11f, Color.BLACK, boxX, boxW, y - bandH - 17f,
                salida.getNoSalida() != null ? salida.getNoSalida() : "");

        return y - headerH;
    }

    /**
     * Bloque de destinatario: etiqueta azul a la izquierda, valor en celda blanca.
     */
    private float dibujarDestinatario(PDPageContentStream cs, float x, float y, float w,
                                      SalidaArt salida) throws IOException {
        float rowH = 16f;
        float labelW = 165f;

        String[][] filas = {
                {"DESTINATARIO", salida.getDestinatario()},
                {"DIRECCIÓN DEL DESTINATARIO", salida.getDireccionDestinatario()}
        };

        for (String[] fila : filas) {
            cs.setNonStrokingColor(AZUL_OSCURO);
            cs.addRect(x, y - rowH, labelW, rowH);
            cs.fill();
            cs.setNonStrokingColor(Color.WHITE);
            cs.beginText();
            cs.setFont(fontBold(), 7f);
            cs.newLineAtOffset(x + 5, y - 11);
            cs.showText(sanear(fila[0]));
            cs.endText();
            cs.setNonStrokingColor(Color.BLACK);

            rect(cs, x + labelW, y - rowH, w - labelW, rowH);
            valor(cs, x + labelW + 5, y - 11, fila[1], 8f);
            y -= rowH;
        }

        return y;
    }

    /**
     * Bloque de fecha y referencia.
     */
    private float dibujarFechaReferencia(PDPageContentStream cs, float x, float y, float w,
                                         SalidaArt salida) throws IOException {
        float labelW = 100f;

        // Fecha
        float fechaH = 18f;
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x, y - fechaH, labelW, fechaH);
        cs.fill();
        textoCentrado(cs, fontBold(), 8f, Color.WHITE, x, labelW, y - 12, "FECHA:");
        cs.setNonStrokingColor(Color.BLACK);
        rect(cs, x + labelW, y - fechaH, 170f, fechaH);
        String fechaStr = salida.getFecha() != null ? salida.getFecha().format(FMT_FECHA) : "";
        textoCentrado(cs, fontRegular(), 8.5f, Color.BLACK, x + labelW, 170f, y - 12, fechaStr);
        y -= fechaH + 6;

        // Referencia
        float refH = 32f;
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x, y - refH, labelW, refH);
        cs.fill();
        textoCentrado(cs, fontBold(), 8f, Color.WHITE, x, labelW, y - 19, "REFERENCIA:");
        cs.setNonStrokingColor(Color.BLACK);
        rect(cs, x + labelW, y - refH, w - labelW, refH);
        if (salida.getReferencia() != null && !salida.getReferencia().isBlank()) {
            List<String> lineas = partirLineas(salida.getReferencia(), fontRegular(), 8f, w - labelW - 12);
            float ty = y - 13;
            for (String linea : lineas) {
                if (ty < y - refH + 6) break;
                valor(cs, x + labelW + 6, ty, linea, 8f);
                ty -= 10;
            }
        }
        y -= refH;

        return y;
    }

    /**
     * Tabla de partidas: NO | QTY | DESCRIPCIÓN | NÚMERO DE PARTE | NÚMERO DE SERIE | CONDICIÓN | OBSERVACIÓN
     */
    private float dibujarTablaPartidas(PDPageContentStream cs, float x, float y, float w,
                                       List<DetalleSalidaArt> detalles) throws IOException {
        float[] props = {0.05f, 0.06f, 0.28f, 0.15f, 0.15f, 0.11f, 0.20f};
        float[] anchos = new float[props.length];
        float suma = 0;
        for (int i = 0; i < props.length - 1; i++) {
            anchos[i] = w * props[i];
            suma += anchos[i];
        }
        anchos[props.length - 1] = w - suma;

        String[] headers = {"NO", "QTY", "DESCRIPCIÓN", "NÚMERO DE PARTE", "NÚMERO DE SERIE", "CONDICIÓN", "OBSERVACIÓN"};

        // Encabezado
        float headH = 20f;
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x, y - headH, w, headH);
        cs.fill();
        float cx = x;
        for (int i = 0; i < headers.length; i++) {
            textoCentrado(cs, fontBold(), 6f, Color.WHITE, cx, anchos[i], y - 12, headers[i]);
            cx += anchos[i];
        }
        cs.setNonStrokingColor(Color.BLACK);
        y -= headH;

        // Filas: al menos 8, o tantas como partidas existan
        int totalFilas = Math.max(8, detalles != null ? detalles.size() : 0);
        float rowH = 18f;

        for (int f = 0; f < totalFilas; f++) {
            cx = x;
            for (float ancho : anchos) {
                rect(cs, cx, y - rowH, ancho, rowH);
                cx += ancho;
            }

            if (detalles != null && f < detalles.size()) {
                DetalleSalidaArt d = detalles.get(f);
                Articulo a = d.getArticulo();

                float px = x;
                textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, px, anchos[0], y - 12, String.valueOf(f + 1));
                px += anchos[0];

                Integer cant = d.getCantidad() != null ? d.getCantidad() : d.getQty();
                textoCentrado(cs, fontRegular(), 7.5f, Color.BLACK, px, anchos[1], y - 12,
                        cant != null ? cant.toString() : "");
                px += anchos[1];

                // Descripción recortada al ancho de la celda
                String desc = (a != null && a.getDescripcion() != null) ? a.getDescripcion() : "";
                List<String> lDesc = partirLineas(desc, fontRegular(), 6.5f, anchos[2] - 6);
                if (!lDesc.isEmpty()) {
                    valor(cs, px + 3, y - 12, lDesc.get(0), 6.5f);
                }
                px += anchos[2];

                textoCentrado(cs, fontRegular(), 6.5f, Color.BLACK, px, anchos[3], y - 12,
                        a != null ? a.getCodigo() : "");
                px += anchos[3];

                textoCentrado(cs, fontRegular(), 6.5f, Color.BLACK, px, anchos[4], y - 12,
                        (a != null && a.getNoSerie() != null) ? a.getNoSerie() : "");
                px += anchos[4];

                textoCentrado(cs, fontRegular(), 6.5f, Color.BLACK, px, anchos[5], y - 12,
                        obtenerNombreCondicion(a));
                px += anchos[5];

                String obs = d.getObservaciones() != null ? d.getObservaciones() : "";
                List<String> lObs = partirLineas(obs, fontRegular(), 6.5f, anchos[6] - 6);
                if (!lObs.isEmpty()) {
                    valor(cs, px + 3, y - 12, lObs.get(0), 6.5f);
                }
            }
            y -= rowH;
        }

        return y;
    }

    /**
     * Nota que define las abreviaturas de condición aceptadas.
     */
    private float dibujarNotaCondiciones(PDPageContentStream cs, float x, float y, float w) throws IOException {
        String nota = "* NOTA: LA CONDICIÓN SE DEFINE POR: NUEVO DE FÁBRICA(NF), NEW SURPLUS (NS), OVERHAULED (OH), "
                + "REMOVIDO (AR), SERVICIABLE(SV), REPARABLE (RP), CALIBRADO (CA)";
        List<String> lineas = partirLineas(nota, fontBold(), 6f, w - 8);
        float ty = y - 8;
        for (String linea : lineas) {
            cs.beginText();
            cs.setFont(fontBold(), 6f);
            cs.setNonStrokingColor(Color.BLACK);
            cs.newLineAtOffset(x, ty);
            cs.showText(linea);
            cs.endText();
            ty -= 8;
        }
        return ty;
    }

    /**
     * Tres preguntas de verificación con casillas SI / NO a la derecha.
     */
    private float dibujarPreguntasVerificacion(PDPageContentStream cs, float x, float y, float w) throws IOException {
        // Nota introductoria
        cs.beginText();
        cs.setFont(fontBold(), 6f);
        cs.setNonStrokingColor(Color.BLACK);
        cs.newLineAtOffset(x, y - 8);
        cs.showText("* NOTA: COLOCAR EN LA CASILLA RÚBRICA DE QUIEN RECIBE.");
        cs.endText();
        y -= 16;

        float colSi = w - 90f;
        float colNo = w - 45f;

        // Encabezados SI / NO
        textoCentrado(cs, fontBold(), 7f, Color.BLACK, x + colSi, 40f, y - 8, "SI");
        textoCentrado(cs, fontBold(), 7f, Color.BLACK, x + colNo, 40f, y - 8, "NO");
        y -= 12;

        String[] preguntas = {
                "¿EL COMPONENTE(S)/EQUIPO(S)/MATERIAL(ES) PRESENTA DAÑOS FÍSICOS?",
                "¿EL NÚMERO DE PARTE Y EL NÚMERO DE SERIE DEL COMPONENTE(S)/EQUIPO(S)/MATERIAL(ES) COINCIDEN CON LO REQUERIDO?",
                "¿LA DOCUMENTACIÓN ANEXA DEL COMPONENTE(S)/EQUIPO(S)/MATERIAL(ES) (CERTIFICACIÓN DEL MATERIAL, CERTIFICADO DE "
                        + "CONFORMIDAD, CERTIFICADO DE CALIBRACIÓN, SERVICIO DE MANTENIMIENTO REALIZADO, ETC) ES CORRECTA Y SE ENCUENTRA FIRMADA?"
        };

        float anchoTexto = colSi - 10f;
        for (String pregunta : preguntas) {
            List<String> lineas = partirLineas(pregunta, fontRegular(), 6f, anchoTexto);
            float alto = Math.max(14f, lineas.size() * 7.5f + 4f);

            float ty = y - 7;
            for (String linea : lineas) {
                cs.beginText();
                cs.setFont(fontRegular(), 6f);
                cs.setNonStrokingColor(Color.BLACK);
                cs.newLineAtOffset(x, ty);
                cs.showText(linea);
                cs.endText();
                ty -= 7.5f;
            }

            // Casillas SI / NO centradas verticalmente en el bloque
            float boxY = y - alto / 2 - 4f;
            rect(cs, x + colSi + 14f, boxY, 12f, 10f);
            rect(cs, x + colNo + 14f, boxY, 12f, 10f);

            y -= alto;
        }

        return y;
    }

    /**
     * Tres bloques de firma: Responsable de Almacén, Traslada y Recibe.
     */
    private float dibujarFirmas(PDPageContentStream cs, float x, float y, float w,
                                SalidaArt salida, SalidaArtExportRequestDto dto) throws IOException {
        float colW = w / 3f;

        String[][] bloques = {
                {"RESPONSABLE DE ALMACÉN:",
                        valorPreferido(dto != null ? dto.getEncargadoAlmacen() : null, salida.getEncargadoAlmacen()),
                        dto != null ? dto.getFechaEncargado() : null},
                {"TRASLADA:",
                        valorPreferido(dto != null ? dto.getTraslada() : null, salida.getTraslada()),
                        dto != null ? dto.getFechaTraslada() : null},
                {"RECIBE:",
                        valorPreferido(dto != null ? dto.getRecibe() : null, salida.getRecibe()),
                        dto != null ? dto.getFechaRecibe() : null}
        };

        // Títulos
        for (int i = 0; i < 3; i++) {
            textoCentrado(cs, fontBold(), 8f, Color.BLACK, x + colW * i, colW, y - 10, bloques[i][0]);
        }
        y -= 22;

        // Nombre/firma y fecha con líneas de escritura
        for (int i = 0; i < 3; i++) {
            float bx = x + colW * i;

            cs.beginText();
            cs.setFont(fontBold(), 6.5f);
            cs.setNonStrokingColor(Color.BLACK);
            cs.newLineAtOffset(bx, y);
            cs.showText("NOMBRE/FIRMA:");
            cs.endText();
            cs.setStrokingColor(Color.BLACK);
            cs.setLineWidth(0.6f);
            cs.moveTo(bx + 62f, y - 2);
            cs.lineTo(bx + colW - 12f, y - 2);
            cs.stroke();
            valor(cs, bx + 66f, y + 1, bloques[i][1], 7f);

            cs.beginText();
            cs.setFont(fontBold(), 6.5f);
            cs.newLineAtOffset(bx + 14f, y - 16);
            cs.showText("FECHA:");
            cs.endText();
            cs.moveTo(bx + 48f, y - 18);
            cs.lineTo(bx + colW - 12f, y - 18);
            cs.stroke();
            valor(cs, bx + 52f, y - 15, bloques[i][2], 7f);
        }

        return y - 30;
    }

    /**
     * Aviso de confidencialidad al pie del cuerpo del documento.
     */
    private void dibujarAvisoConfidencialidad(PDPageContentStream cs, float x, float y, float w) throws IOException {
        String aviso = "El contenido del presente documento es información confidencial propiedad de AG Aviation Suppliers S.A. de C.V. "
                + "La reproducción total o parcial, así como la transferencia de contenido sin previa autorización es sancionada por las "
                + "leyes nacionales e internacionales.";
        List<String> lineas = partirLineas(aviso, fontRegular(), 5.5f, w - 20);
        float ty = y - 8;
        for (String linea : lineas) {
            textoCentrado(cs, fontRegular(), 5.5f, GRIS_LINEA, x, w, ty, linea);
            ty -= 7;
        }
    }

    // ---------------------------------------------------------------------
    // Utilidades locales
    // ---------------------------------------------------------------------

    /**
     * Prioriza el valor capturado al exportar; si viene vacío usa el guardado en la salida.
     */
    private String valorPreferido(String preferido, String respaldo) {
        if (preferido != null && !preferido.isBlank()) return preferido;
        return respaldo != null ? respaldo : "";
    }

    /**
     * Devuelve la abreviatura de la condición (lo que está entre paréntesis en el catálogo).
     */
    private String obtenerNombreCondicion(Articulo articulo) {
        if (articulo == null || articulo.getCondicion() == null) {
            return "";
        }
        String nombreCompleto = condicionRepository.findById(articulo.getCondicion())
                .map(c -> c.getNombre())
                .orElse("");
        if (nombreCompleto.contains("(") && nombreCompleto.contains(")")) {
            return nombreCompleto.substring(nombreCompleto.indexOf("(") + 1, nombreCompleto.indexOf(")"));
        }
        return nombreCompleto;
    }
}