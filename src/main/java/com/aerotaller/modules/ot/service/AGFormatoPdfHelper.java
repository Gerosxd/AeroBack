package com.aerotaller.modules.ot.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper compartido para plantillas oficiales AG-145-XX.
 * Dibuja encabezado institucional, pie de página, celdas bilingües y checkboxes.
 */
public final class AGFormatoPdfHelper {

    private AGFormatoPdfHelper() {
    }

    // PALETA INSTITUCIONAL OFICIAL (AFAC / Taller 505)
    public static final Color AZUL_OSCURO     = Color.decode("#0B769E"); // Encabezados principales
    public static final Color AZUL_MARINO     = Color.decode("#153D63"); // Fondos oscuros / firmas
    public static final Color AZUL_CLARO      = Color.decode("#DAE9F7"); // Celdas y fondos suaves
    public static final Color AZUL_MEDIO      = Color.decode("#A5C9EB"); // Subencabezados
    public static final Color CIAN_DISCREP    = Color.decode("#33CCCC"); // Encabezado Discrepancia AGD-01
    public static final Color ROJO_CONTROL    = new Color(200, 30, 30);  // Folio AG/OT/XX-XXX
    public static final Color GRIS_LINEA      = new Color(100, 100, 100);

    public static final float PAGE_MARGIN = 30f;

    public static PDType1Font fontBold() {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    }

    public static PDType1Font fontRegular() {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    public static PDType1Font fontItalic() {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
    }

    /**
     * Encabezado institucional optimizado para evitar solapamiento de fechas y números de control.
     */
    public static float dibujarEncabezado(PDDocument doc, PDPageContentStream cs, float pageWidth, float topY,
                                          String tituloFormato, String tituloIngles,
                                          String etiquetaControl, String noControl, String fecha) throws IOException {
        PDType1Font bold = fontBold();
        PDType1Font ita = fontItalic();

        float headerHeight = 78f;
        float y = topY;

        // Marco exterior
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(0.8f);
        cs.addRect(PAGE_MARGIN, y - headerHeight, pageWidth - 2 * PAGE_MARGIN, headerHeight);
        cs.stroke();

        // 1. LOGO
        float logoBoxW = 115f;
        try (InputStream is = new ClassPathResource("reports/assets/logo_ag.png").getInputStream()) {
            PDImageXObject logo = PDImageXObject.createFromByteArray(doc, is.readAllBytes(), "logo_ag");
            float logoW = 90f;
            float logoH = logoW * ((float) logo.getHeight() / logo.getWidth());
            cs.drawImage(logo, PAGE_MARGIN + (logoBoxW - logoW) / 2, y - headerHeight / 2 - logoH / 2, logoW, logoH);
        } catch (Exception ignored) {
        }
        cs.moveTo(PAGE_MARGIN + logoBoxW, y - headerHeight);
        cs.lineTo(PAGE_MARGIN + logoBoxW, y);
        cs.stroke();

        // 2. TÍTULOS CENTRALES
        float controlBoxW = 150f;
        float centerX = PAGE_MARGIN + logoBoxW;
        float centerW = pageWidth - 2 * PAGE_MARGIN - logoBoxW - controlBoxW;

        textoCentrado(cs, bold, 9f, AZUL_OSCURO, centerX, centerW, y - 14, "AG AVIATION SUPPLIERS S.A. de C.V.");
        textoCentrado(cs, bold, 8.5f, AZUL_MARINO, centerX, centerW, y - 26, "TALLER AERONÁUTICO AUTORIZADO A.F.A.C No. 505");
        textoCentrado(cs, bold, 11f, Color.BLACK, centerX, centerW, y - 48, tituloFormato);
        if (tituloIngles != null && !tituloIngles.isBlank()) {
            textoCentrado(cs, ita, 9f, GRIS_LINEA, centerX, centerW, y - 62, tituloIngles);
        }

        // 3. RECUADRO DERECHO DE CONTROL Y FECHA
        float controlX = pageWidth - PAGE_MARGIN - controlBoxW;
        cs.moveTo(controlX, y - headerHeight);
        cs.lineTo(controlX, y);
        cs.stroke();

        // Banda superior (Título Control / Orden de Trabajo)
        float topBandH = 15f;
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(controlX, y - topBandH, controlBoxW, topBandH);
        cs.fill();
        textoCentrado(cs, bold, 7.5f, Color.WHITE, controlX, controlBoxW, y - 11, etiquetaControl);

        // Valor Número de Control / OT
        textoCentrado(cs, bold, 11.5f, ROJO_CONTROL, controlX, controlBoxW, y - topBandH - 16,
                noControl != null ? noControl : "AG/OT/__-___");

        // Banda divisoria de Fecha (con mayor separación para no encimarse)
        float fechaBandH = 14f;
        float fechaBandY = y - 46f;
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(controlX, fechaBandY - fechaBandH, controlBoxW, fechaBandH);
        cs.fill();

        // Texto Fecha en español e inglés alineados
        String textoFecha = "FECHA / DATE: " + (fecha != null ? fecha : "—");
        textoCentrado(cs, bold, 7.5f, Color.WHITE, controlX, controlBoxW, fechaBandY - 10.5f, textoFecha);

        // Sub-rectángulo inferior en blanco
        float subBoxH = (fechaBandY - fechaBandH) - (y - headerHeight);
        cs.setStrokingColor(Color.BLACK);
        cs.addRect(controlX, y - headerHeight, controlBoxW, subBoxH);
        cs.stroke();

        cs.setNonStrokingColor(Color.BLACK);
        return y - headerHeight - 6;
    }

    /**
     * Dibuja una celda estándar con borde, fondo opcional, etiqueta bilingüe y valor.
     */
    public static void celda(PDPageContentStream cs, float x, float y, float w, float h,
                             String es, String en, String val, Color colorFondo) throws IOException {
        if (colorFondo != null) {
            cs.setNonStrokingColor(colorFondo);
            cs.addRect(x, y - h, w, h);
            cs.fill();
            cs.setNonStrokingColor(Color.BLACK);
        }

        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(0.6f);
        cs.addRect(x, y - h, w, h);
        cs.stroke();

        // Etiquetas
        if (es != null) {
            etiquetaBilingue(cs, x + 3f, y - 8f, es, en, 6.5f);
        }

        // Valor
        if (val != null && !val.isBlank()) {
            valor(cs, x + 3f, y - h + 4.5f, val, 7.5f);
        }
    }

    public static void dibujarPie(PDPageContentStream cs, float pageWidth, String forma,
                                  int hoja, int totalHojas) throws IOException {
        PDType1Font bold = fontBold();
        float y = 32f;

        cs.beginText();
        cs.setFont(bold, 7.5f);
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.newLineAtOffset(PAGE_MARGIN, y);
        cs.showText("FORMA " + forma + " REV: 01");
        cs.endText();

        String hojaTxt = "Hoja " + hoja + " de " + totalHojas;
        float w = bold.getStringWidth(hojaTxt) / 1000 * 7.5f;
        cs.beginText();
        cs.setFont(bold, 7.5f);
        cs.newLineAtOffset(pageWidth - PAGE_MARGIN - w, y);
        cs.showText(hojaTxt);
        cs.endText();

        cs.beginText();
        cs.setFont(fontRegular(), 6f);
        cs.newLineAtOffset(PAGE_MARGIN, y - 10);
        cs.showText("HANGAR No. 23, AEROPUERTO NACIONAL DE CELAYA, CARRETERA LIBRE CELAYA A SALAMANCA KM. 6.5, C.P. 38000, CELAYA, GUANAJUATO.");
        cs.endText();
        cs.setNonStrokingColor(Color.BLACK);
    }

    public static float dibujarBarraSeccion(PDPageContentStream cs, float x, float y, float width,
                                            String tituloEs, String tituloEn) throws IOException {
        float h = tituloEn != null ? 20f : 14f;
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x, y - h, width, h);
        cs.fill();
        textoCentrado(cs, fontBold(), 8f, Color.WHITE, x, width, y - 9.5f, tituloEs);
        if (tituloEn != null) {
            textoCentrado(cs, fontItalic(), 6.5f, Color.WHITE, x, width, y - 17.5f, tituloEn);
        }
        cs.setNonStrokingColor(Color.BLACK);
        return y - h;
    }

    public static void etiquetaBilingue(PDPageContentStream cs, float x, float y,
                                        String es, String en, float size) throws IOException {
        cs.beginText();
        cs.setFont(fontBold(), size);
        cs.setNonStrokingColor(Color.BLACK);
        cs.newLineAtOffset(x, y);
        cs.showText(es);
        cs.endText();
        if (en != null) {
            cs.beginText();
            cs.setFont(fontItalic(), Math.max(5f, size - 1.2f));
            cs.setNonStrokingColor(GRIS_LINEA);
            cs.newLineAtOffset(x, y - size);
            cs.showText(en);
            cs.endText();
            cs.setNonStrokingColor(Color.BLACK);
        }
    }

    public static void valor(PDPageContentStream cs, float x, float y, String texto, float size) throws IOException {
        if (texto == null || texto.isBlank()) return;
        cs.beginText();
        cs.setFont(fontRegular(), size);
        cs.setNonStrokingColor(Color.BLACK);
        cs.newLineAtOffset(x, y);
        cs.showText(sanear(texto));
        cs.endText();
    }

    public static void checkbox(PDPageContentStream cs, float x, float y, boolean marcado,
                                String etiquetaEs, String etiquetaEn, float sizeEtiqueta) throws IOException {
        float s = 7.5f;
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(0.7f);
        cs.addRect(x, y, s, s);
        cs.stroke();
        if (marcado) {
            cs.beginText();
            cs.setFont(fontBold(), 7.5f);
            cs.newLineAtOffset(x + 1.2f, y + 1f);
            cs.showText("X");
            cs.endText();
        }
        if (etiquetaEs != null) {
            etiquetaBilingue(cs, x + s + 3.5f, y + 1.5f, etiquetaEs, etiquetaEn, sizeEtiqueta);
        }
    }

    public static void rect(PDPageContentStream cs, float x, float y, float w, float h) throws IOException {
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(0.6f);
        cs.addRect(x, y, w, h);
        cs.stroke();
    }

    public static void textoCentrado(PDPageContentStream cs, PDType1Font font, float size, Color color,
                                     float x, float width, float y, String texto) throws IOException {
        if (texto == null) return;
        String t = sanear(texto);
        float w = font.getStringWidth(t) / 1000 * size;
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x + (width - w) / 2, y);
        cs.showText(t);
        cs.endText();
        cs.setNonStrokingColor(Color.BLACK);
    }

    public static String sanear(String texto) {
        if (texto == null) return "";
        return texto.replace("\r", " ").replace("\n", " ").replace("\t", " ");
    }

    /**
     * Divide un texto largo en múltiples líneas para que no exceda el ancho máximo permitido.
     */
    public static java.util.List<String> partirLineas(String texto, PDType1Font font, float size, float maxWidth) throws IOException {
        java.util.List<String> lineas = new java.util.ArrayList<>();
        if (texto == null || texto.isBlank()) {
            return lineas;
        }
        String[] palabras = sanear(texto).split("\\s+");
        StringBuilder actual = new StringBuilder();
        for (String p : palabras) {
            String prueba = actual.isEmpty() ? p : actual + " " + p;
            if (font.getStringWidth(prueba) / 1000 * size <= maxWidth) {
                actual = new StringBuilder(prueba);
            } else {
                if (!actual.isEmpty()) {
                    lineas.add(actual.toString());
                }
                actual = new StringBuilder(p);
            }
        }
        if (!actual.isEmpty()) {
            lineas.add(actual.toString());
        }
        return lineas;
    }
}