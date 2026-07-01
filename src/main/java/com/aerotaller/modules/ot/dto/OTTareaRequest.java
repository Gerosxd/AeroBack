package com.aerotaller.modules.ot.dto;

import java.math.BigDecimal;

public class OTTareaRequest {

    private String codigo;
    private String descripcion;
    private String tecnicos;
    private BigDecimal horasTotales;
    private String tipoTarea;
    private String tipoServicio;
    private String intervalo;
    private String requiereRII;
    private String parteAsociada;

    public OTTareaRequest() {
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTecnicos() {
        return tecnicos;
    }

    public void setTecnicos(String tecnicos) {
        this.tecnicos = tecnicos;
    }

    public BigDecimal getHorasTotales() {
        return horasTotales;
    }

    public void setHorasTotales(BigDecimal horasTotales) {
        this.horasTotales = horasTotales;
    }

    public String getTipoTarea() {
        return tipoTarea;
    }

    public void setTipoTarea(String tipoTarea) {
        this.tipoTarea = tipoTarea;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getIntervalo() {
        return intervalo;
    }

    public void setIntervalo(String intervalo) {
        this.intervalo = intervalo;
    }

    public String getRequiereRII() {
        return requiereRII;
    }

    public void setRequiereRII(String requiereRII) {
        this.requiereRII = requiereRII;
    }

    public String getParteAsociada() {
        return parteAsociada;
    }

    public void setParteAsociada(String parteAsociada) {
        this.parteAsociada = parteAsociada;
    }
}