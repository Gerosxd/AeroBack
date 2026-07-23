package com.aerotaller.modules.salidaArt.dto;

import java.time.LocalDate;
import java.util.List;

public class SalidaArtDetalleResponse {
    private Integer idSalida;
    private String noSalida;
    private LocalDate fecha;
    private String referencia;
    private String destinatario;
    private String direccionDestinatario;
    private String encargadoAlmacen;
    private String traslada;
    private String recibe;

    private List<ArticuloSalidaResponse> articulos;

    public Integer getIdSalida() {
        return idSalida;
    }

    public void setIdSalida(Integer idSalida) {
        this.idSalida = idSalida;
    }

    public String getNoSalida() {
        return noSalida;
    }

    public void setNoSalida(String noSalida) {
        this.noSalida = noSalida;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getDireccionDestinatario() {
        return direccionDestinatario;
    }

    public void setDireccionDestinatario(String direccionDestinatario) {
        this.direccionDestinatario = direccionDestinatario;
    }

    public String getEncargadoAlmacen() {
        return encargadoAlmacen;
    }

    public void setEncargadoAlmacen(String encargadoAlmacen) {
        this.encargadoAlmacen = encargadoAlmacen;
    }

    public String getTraslada() {
        return traslada;
    }

    public void setTraslada(String traslada) {
        this.traslada = traslada;
    }

    public String getRecibe() {
        return recibe;
    }

    public void setRecibe(String recibe) {
        this.recibe = recibe;
    }

    public List<ArticuloSalidaResponse> getArticulos() {
        return articulos;
    }

    public void setArticulos(List<ArticuloSalidaResponse> articulos) {
        this.articulos = articulos;
    }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}