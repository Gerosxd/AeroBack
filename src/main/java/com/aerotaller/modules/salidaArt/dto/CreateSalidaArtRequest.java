package com.aerotaller.modules.salidaArt.dto;

import com.aerotaller.modules.detalleSalidaArt.dto.CreateDetalleSalidaArtRequest;

import java.util.List;

public class CreateSalidaArtRequest {
    // Frontend sends YYYY-MM-DD; backend enforces "fecha del dia" in the service.
    private String fecha;
    private String referencia;
    private String noSalida;
    private String destinatario;
    private String direccionDestinatario;
    private String encargadoAlmacen;
    private String traslada;
    private String recibe;

    private List<CreateDetalleSalidaArtRequest> detalles;

    public String getNoSalida() {
        return noSalida;
    }

    public void setNoSalida(String noSalida) {
        this.noSalida = noSalida;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
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

    public List<CreateDetalleSalidaArtRequest> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<CreateDetalleSalidaArtRequest> detalles) {
        this.detalles = detalles;
    }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}