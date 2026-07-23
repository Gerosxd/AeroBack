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
 * P-01: Helper compartido para las plantillas oficiales AG-145-XX.
 * Dibuja los elementos comunes: encabezado institucional, pie de página,
 * celdas bilingües (ES negrita / EN cursiva) y checkboxes.
 *
 * Usado por: Carátula OT (AG-145-03), Hoja de Servicio (AG-145-04),
 * Discrepancias (AG-145-12) y Salida de Almacén (AG-145-22).
 */
public final class AGFormatoPdfHelper {

    private AGFormatoPdfHelper() {
    }

    // Paleta institucional
    public static final Color AZUL_OSCURO = new Color(12, 74, 110);    // Encabezados de sección
    public static final Color AZUL_MEDIO = new Color(12, 132, 173);    // Acentos / títulos
    public static final Color AZUL_CLARO = new Color(224, 242, 254);   // Fondos suaves
    public static final Color ROJO_CONTROL = new Color(200, 30, 30);   // Número de control XX-XXX
    public static final Color GRIS_LINEA = new Color(120, 120, 120);

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
     * Encabezado institucional completo:
     * [LOGO] | AG AVIATION SUPPLIERS S.A. de C.V. + subtítulo del formato | recuadro No. de control + fecha
     *
     * @param tituloFormato   ej. "ORDEN DE TRABAJO"
     * @param tituloIngles    ej. "WORK ORDER"
     * @param etiquetaControl ej. "NÚMERO DE CONTROL" u "ORDEN DE TRABAJO"
     * @param noControl       ej. "AG/OT/26-007"
     * @param fecha           fecha ya formateada (dd/MM/yyyy) o null
     * @return la coordenada Y donde termina el encabezado (para continuar dibujando debajo)
     */
    public static float dibujarEncabezado(PDDocument doc, PDPageContentStream cs, float pageWidth, float topY,
                                          String tituloFormato, String tituloIngles,
                                          String etiquetaControl, String noControl, String fecha) throws IOException {
        PDType1Font bold = fontBold();
        PDType1Font reg = fontRegular();
        PDType1Font ita = fontItalic();

        float headerHeight = 78f;
        float y = topY;

        // Marco del encabezado
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(0.8f);
        cs.addRect(PAGE_MARGIN, y - headerHeight, pageWidth - 2 * PAGE_MARGIN, headerHeight);
        cs.stroke();

        // 1. LOGO (izquierda)
        float logoBoxW = 120f;
        try (InputStream is = new ClassPathResource("reports/assets/logo_ag.png").getInputStream()) {
            PDImageXObject logo = PDImageXObject.createFromByteArray(doc, is.readAllBytes(), "logo_ag");
            float logoW = 95f;
            float logoH = logoW * ((float) logo.getHeight() / logo.getWidth());
            cs.drawImage(logo, PAGE_MARGIN + (logoBoxW - logoW) / 2, y - headerHeight / 2 - logoH / 2, logoW, logoH);
        } catch (Exception ignored) {
            // Si el logo no está disponible, continuar sin él
        }
        // Línea divisoria tras el logo
        cs.moveTo(PAGE_MARGIN + logoBoxW, y - headerHeight);
        cs.lineTo(PAGE_MARGIN + logoBoxW, y);
        cs.stroke();

        // 2. TÍTULOS (centro)
        float controlBoxW = 150f;
        float centerX = PAGE_MARGIN + logoBoxW;
        float centerW = pageWidth - 2 * PAGE_MARGIN - logoBoxW - controlBoxW;

        textoCentrado(cs, bold, 9.5f, AZUL_MEDIO, centerX, centerW, y - 16,
                "AG AVIATION SUPPLIERS S.A. de C.V.");
        textoCentrado(cs, bold, 9.5f, AZUL_MEDIO, centerX, centerW, y - 28,
                "TALLER AERONÁUTICO AUTORIZADO A.F.A.C No. 505");
        textoCentrado(cs, bold, 12f, Color.BLACK, centerX, centerW, y - 52, tituloFormato);
        textoCentrado(cs, ita, 10f, Color.BLACK, centerX, centerW, y - 65, tituloIngles);

        // Línea divisoria antes del recuadro de control
        float controlX = pageWidth - PAGE_MARGIN - controlBoxW;
        cs.moveTo(controlX, y - headerHeight);
        cs.lineTo(controlX, y);
        cs.stroke();

        // 3. RECUADRO DE CONTROL (derecha): etiqueta con fondo azul + número + fecha
        float bandH = 16f;
        // Banda superior
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(controlX, y - bandH, controlBoxW, bandH);
        cs.fill();
        textoCentrado(cs, bold, 7.5f, Color.WHITE, controlX, controlBoxW, y - 11, etiquetaControl);

        // Número de control
        textoCentrado(cs, bold, 12f, ROJO_CONTROL, controlX, controlBoxW, y - bandH - 16,
                noControl != null ? noControl : "AG/OT/__-___");

        // Banda fecha
        float fechaBandY = y - bandH - 26;
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(controlX, fechaBandY - bandH, controlBoxW, bandH);
        cs.fill();
        textoCentrado(cs, bold, 7.5f, Color.WHITE, controlX, controlBoxW, fechaBandY - 11,
                "FECHA:  " + (fecha != null ? fecha : ""));
        textoCentrado(cs, ita, 6.5f, Color.WHITE, controlX, controlBoxW, fechaBandY - bandH + 2, "DATE");

        // Línea horizontal interna (separa banda de control del número)
        cs.setStrokingColor(Color.BLACK);
        cs.moveTo(controlX, y - bandH - 26 - bandH);
        cs.lineTo(pageWidth - PAGE_MARGIN, y - bandH - 26 - bandH);

        cs.setNonStrokingColor(Color.BLACK);
        return y - headerHeight - 8;
    }

    /**
     * Pie de página oficial:
     * FORMA AG-145/XX REV: 01  ·  Hoja X de Y  ·  dirección del hangar
     */
    public static void dibujarPie(PDPageContentStream cs, float pageWidth, String forma,
                                  int hoja, int totalHojas) throws IOException {
        PDType1Font bold = fontBold();
        float y = 42f;

        cs.beginText();
        cs.setFont(bold, 8f);
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.newLineAtOffset(PAGE_MARGIN, y);
        cs.showText("FORMA " + forma + " REV: 01");
        cs.endText();

        String hojaTxt = "Hoja " + hoja + " de " + totalHojas;
        float w = bold.getStringWidth(hojaTxt) / 1000 * 8f;
        cs.beginText();
        cs.setFont(bold, 8f);
        cs.newLineAtOffset(pageWidth - PAGE_MARGIN - w, y);
        cs.showText(hojaTxt);
        cs.endText();

        cs.beginText();
        cs.setFont(bold, 6.5f);
        cs.newLineAtOffset(PAGE_MARGIN, y - 12);
        cs.showText("HANGAR No. 23, AEROPUERTO NACIONAL DE CELAYA, CARRETERA LIBRE CELAYA A SALAMANCA KM. 6.5, C.P. 38000, CELAYA, GUANAJUATO.");
        cs.endText();
        cs.setNonStrokingColor(Color.BLACK);
    }

    /**
     * Barra de título de sección con fondo azul oscuro y subtítulo en inglés.
     * ej. "INFORMACIÓN DEL CLIENTE / OPERADOR" / "CLIENT / OPERATOR INFORMATION"
     *
     * @return la Y debajo de la barra
     */
    public static float dibujarBarraSeccion(PDPageContentStream cs, float x, float y, float width,
                                            String tituloEs, String tituloEn) throws IOException {
        float h = tituloEn != null ? 24f : 15f;
        cs.setNonStrokingColor(AZUL_OSCURO);
        cs.addRect(x, y - h, width, h);
        cs.fill();
        textoCentrado(cs, fontBold(), 8.5f, Color.WHITE, x, width, y - 11, tituloEs);
        if (tituloEn != null) {
            textoCentrado(cs, fontItalic(), 7.5f, Color.WHITE, x, width, y - 21, tituloEn);
        }
        cs.setNonStrokingColor(Color.BLACK);
        return y - h;
    }

    /**
     * Etiqueta bilingüe dentro de una celda: español en negritas arriba, inglés en cursiva debajo.
     */
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
            cs.setFont(fontItalic(), size - 1.5f);
            cs.setNonStrokingColor(GRIS_LINEA);
            cs.newLineAtOffset(x, y - size + 1f);
            cs.showText(en);
            cs.endText();
            cs.setNonStrokingColor(Color.BLACK);
        }
    }

    /**
     * Valor simple en fuente regular.
     */
    public static void valor(PDPageContentStream cs, float x, float y, String texto, float size) throws IOException {
        if (texto == null || texto.isBlank()) return;
        cs.beginText();
        cs.setFont(fontRegular(), size);
        cs.setNonStrokingColor(Color.BLACK);
        cs.newLineAtOffset(x, y);
        cs.showText(sanear(texto));
        cs.endText();
    }

    /**
     * Checkbox cuadrado con marca (X) opcional y etiqueta bilingüe a la derecha.
     * Usa tamaño de fuente 7.5pt por defecto.
     */
    public static void checkbox(PDPageContentStream cs, float x, float y, boolean marcado,
                                String etiquetaEs, String etiquetaEn) throws IOException {
        checkbox(cs, x, y, marcado, etiquetaEs, etiquetaEn, 7.5f);
    }

    /**
     * Checkbox con tamaño de fuente configurable, para columnas estrechas
     * donde la etiqueta por defecto se desbordaría.
     */
    public static void checkbox(PDPageContentStream cs, float x, float y, boolean marcado,
                                String etiquetaEs, String etiquetaEn, float sizeEtiqueta) throws IOException {
        float s = 8f;
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(0.8f);
        cs.addRect(x, y, s, s);
        cs.stroke();
        if (marcado) {
            cs.beginText();
            cs.setFont(fontBold(), 8f);
            cs.newLineAtOffset(x + 1.5f, y + 1.2f);
            cs.showText("X");
            cs.endText();
        }
        if (etiquetaEs != null) {
            etiquetaBilingue(cs, x + s + 4, y + 2, etiquetaEs, etiquetaEn, sizeEtiqueta);
        }
    }

    /**
     * Rectángulo de contorno simple.
     */
    public static void rect(PDPageContentStream cs, float x, float y, float w, float h) throws IOException {
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(0.8f);
        cs.addRect(x, y, w, h);
        cs.stroke();
    }

    /**
     * Texto centrado horizontalmente dentro de un ancho dado.
     */
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

    /**
     * Divide un texto largo en líneas que caben en un ancho dado.
     */
    public static java.util.List<String> partirLineas(String texto, PDType1Font font, float size, float maxWidth) throws IOException {
        java.util.List<String> lineas = new java.util.ArrayList<>();
        if (texto == null || texto.isBlank()) return lineas;
        String[] palabras = sanear(texto).split("\\s+");
        StringBuilder actual = new StringBuilder();
        for (String p : palabras) {
            String prueba = actual.isEmpty() ? p : actual + " " + p;
            if (font.getStringWidth(prueba) / 1000 * size <= maxWidth) {
                actual = new StringBuilder(prueba);
            } else {
                if (!actual.isEmpty()) lineas.add(actual.toString());
                actual = new StringBuilder(p);
            }
        }
        if (!actual.isEmpty()) lineas.add(actual.toString());
        return lineas;
    }

    /**
     * Elimina caracteres no soportados por las fuentes Standard14 (saltos de línea, tabs).
     */
    public static String sanear(String texto) {
        if (texto == null) return "";
        return texto.replace("\r", " ").replace("\n", " ").replace("\t", " ");
    }
}