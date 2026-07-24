package com.aerotaller.modelos;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "OTTareaMantenimiento")
public class OTTareaMantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTareaOT")
    private Integer idTareaOT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OT", nullable = false)
    private NuevaOT ot;

    @Column(name = "Codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "Descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "Tecnicos", length = 150)
    private String tecnicos;

    @Column(name = "HorasTotales", precision = 10, scale = 2)
    private BigDecimal horasTotales;

    @Column(name = "TipoTarea", length = 100)
    private String tipoTarea;

    @Column(name = "TipoServicio", length = 150)
    private String tipoServicio;

    @Column(name = "Intervalo", length = 150)
    private String intervalo;

    @Column(name = "RequiereRII", length = 2)
    private String requiereRII;

    @Column(name = "ParteAsociada", length = 255)
    private String parteAsociada;

    // --- P-03: Campos para la Hoja de Servicio AG-145-04 ---
    @Column(name = "NumeroParte", length = 100)
    private String numeroParte;

    @Column(name = "NumeroSerie", length = 100)
    private String numeroSerie;

    @Column(name = "AccionCorrectiva", columnDefinition = "TEXT")
    private String accionCorrectiva;

    @Column(name = "EfectuadoPor", length = 150)
    private String efectuadoPor;

    @Column(name = "InspeccionadoPor", length = 150)
    private String inspeccionadoPor;

    @Column(name = "FechaCumplimiento")
    private java.time.LocalDate fechaCumplimiento;

    public OTTareaMantenimiento() {
    }

    public Integer getIdTareaOT() {
        return idTareaOT;
    }

    public void setIdTareaOT(Integer idTareaOT) {
        this.idTareaOT = idTareaOT;
    }

    public NuevaOT getOt() {
        return ot;
    }

    public void setOt(NuevaOT ot) {
        this.ot = ot;
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

    // --- P-03: Hoja de Servicio AG-145-04 ---
    public String getNumeroParte() { return numeroParte; }
    public void setNumeroParte(String numeroParte) { this.numeroParte = numeroParte; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public String getAccionCorrectiva() { return accionCorrectiva; }
    public void setAccionCorrectiva(String accionCorrectiva) { this.accionCorrectiva = accionCorrectiva; }

    public String getEfectuadoPor() { return efectuadoPor; }
    public void setEfectuadoPor(String efectuadoPor) { this.efectuadoPor = efectuadoPor; }

    public String getInspeccionadoPor() { return inspeccionadoPor; }
    public void setInspeccionadoPor(String inspeccionadoPor) { this.inspeccionadoPor = inspeccionadoPor; }

    public java.time.LocalDate getFechaCumplimiento() { return fechaCumplimiento; }
    public void setFechaCumplimiento(java.time.LocalDate fechaCumplimiento) { this.fechaCumplimiento = fechaCumplimiento; }
}