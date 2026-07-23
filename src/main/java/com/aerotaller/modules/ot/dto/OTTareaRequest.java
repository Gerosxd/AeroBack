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
    // P-03: Hoja de Servicio AG-145-04
    private String numeroParte;
    private String numeroSerie;
    private String accionCorrectiva;
    private String efectuadoPor;
    private String inspeccionadoPor;
    private String fechaCumplimiento;

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

    public String getNumeroParte() { return numeroParte; }
    public void setNumeroParte(String v) { this.numeroParte = v; }
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String v) { this.numeroSerie = v; }
    public String getAccionCorrectiva() { return accionCorrectiva; }
    public void setAccionCorrectiva(String v) { this.accionCorrectiva = v; }
    public String getEfectuadoPor() { return efectuadoPor; }
    public void setEfectuadoPor(String v) { this.efectuadoPor = v; }
    public String getInspeccionadoPor() { return inspeccionadoPor; }
    public void setInspeccionadoPor(String v) { this.inspeccionadoPor = v; }
    public String getFechaCumplimiento() { return fechaCumplimiento; }
    public void setFechaCumplimiento(String v) { this.fechaCumplimiento = v; }
}