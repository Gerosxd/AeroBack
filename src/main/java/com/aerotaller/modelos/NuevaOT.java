package com.aerotaller.modelos;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "NuevaOT")
public class NuevaOT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOT")
    private Integer idOT;

    @Column(name = "NoOT", nullable = false, unique = true, length = 20)
    private String noOT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Matricula", nullable = false)
    private Aeronave matricula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Cliente")
    private Cliente cliente;

    @Column(name = "FechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FechaApertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "FechaEntrega")
    private LocalDate fechaEntrega;

    @Column(name = "FechaCierre")
    private LocalDate fechaCierre;

    @Column(name = "Estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "HorasTotales", precision = 10, scale = 2)
    private BigDecimal horasTotales;

    @Column(name = "CiclosTotales")
    private Integer ciclosTotales;

    @Column(name = "TiempoMotor1", precision = 10, scale = 2)
    private BigDecimal tiempoMotor1;

    @Column(name = "CicloMotor1")
    private Integer cicloMotor1;

    @Column(name = "TiempoMotor2", precision = 10, scale = 2)
    private BigDecimal tiempoMotor2;

    @Column(name = "CicloMotor2")
    private Integer cicloMotor2;

    @Column(name = "TiempoMotor3", precision = 10, scale = 2)
    private BigDecimal tiempoMotor3;

    @Column(name = "CicloMotor3")
    private Integer cicloMotor3;

    @Column(name = "TiempoAPU", precision = 10, scale = 2)
    private BigDecimal tiempoAPU;

    @Column(name = "CicloAPU")
    private Integer cicloAPU;

    @Column(name = "ComentarioCliente", columnDefinition = "TEXT")
    private String comentarioCliente;

    // --- P-00: Campos para plantillas de impresión (AG-145-03) ---
    @Column(name = "TipoMantenimiento", length = 15)
    private String tipoMantenimiento; // AERONAVE o COMPONENTE

    @Column(name = "ModalidadMantenimiento", length = 15)
    private String modalidadMantenimiento; // PROGRAMADO o NO_PROGRAMADO

    @Column(name = "ComentarioTaller", columnDefinition = "TEXT")
    private String comentarioTaller;

    @Column(name = "ComponenteDescripcion", length = 255)
    private String componenteDescripcion;

    @Column(name = "ComponenteNumeroParte", length = 100)
    private String componenteNumeroParte;

    @Column(name = "ComponenteNumeroSerie", length = 100)
    private String componenteNumeroSerie;

    @Column(name = "ComponenteCantidad")
    private Integer componenteCantidad;

    @Column(name = "ComponenteHoras", precision = 10, scale = 2)
    private BigDecimal componenteHoras;

    @Column(name = "ComponenteCiclos")
    private Integer componenteCiclos;

    @Column(name = "ComponenteAeronaveAsociada", length = 20)
    private String componenteAeronaveAsociada;

    @Column(name = "ComponenteHorasCiclosRemocion", length = 50)
    private String componenteHorasCiclosRemocion;

    @OneToMany(mappedBy = "ot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OTTareaMantenimiento> tareasMantenimiento = new ArrayList<>();

    @OneToMany(mappedBy = "ot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OTDiscrepancia> discrepancias = new ArrayList<>();

    public NuevaOT() {
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.fechaApertura == null) {
            this.fechaApertura = LocalDateTime.now();
        }
        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "Abierta";
        }
    }

    public Integer getIdOT() {
        return idOT;
    }

    public void setIdOT(Integer idOT) {
        this.idOT = idOT;
    }

    public String getNoOT() {
        return noOT;
    }

    public void setNoOT(String noOT) {
        this.noOT = noOT;
    }

    public Aeronave getMatricula() {
        return matricula;
    }

    public void setMatricula(Aeronave matricula) {
        this.matricula = matricula;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getHorasTotales() {
        return horasTotales;
    }

    public void setHorasTotales(BigDecimal horasTotales) {
        this.horasTotales = horasTotales;
    }

    public Integer getCiclosTotales() {
        return ciclosTotales;
    }

    public void setCiclosTotales(Integer ciclosTotales) {
        this.ciclosTotales = ciclosTotales;
    }

    public BigDecimal getTiempoMotor1() {
        return tiempoMotor1;
    }

    public void setTiempoMotor1(BigDecimal tiempoMotor1) {
        this.tiempoMotor1 = tiempoMotor1;
    }

    public Integer getCicloMotor1() {
        return cicloMotor1;
    }

    public void setCicloMotor1(Integer cicloMotor1) {
        this.cicloMotor1 = cicloMotor1;
    }

    public BigDecimal getTiempoMotor2() {
        return tiempoMotor2;
    }

    public void setTiempoMotor2(BigDecimal tiempoMotor2) {
        this.tiempoMotor2 = tiempoMotor2;
    }

    public Integer getCicloMotor2() {
        return cicloMotor2;
    }

    public void setCicloMotor2(Integer cicloMotor2) {
        this.cicloMotor2 = cicloMotor2;
    }

    public BigDecimal getTiempoMotor3() {
        return tiempoMotor3;
    }

    public void setTiempoMotor3(BigDecimal tiempoMotor3) {
        this.tiempoMotor3 = tiempoMotor3;
    }

    public Integer getCicloMotor3() {
        return cicloMotor3;
    }

    public void setCicloMotor3(Integer cicloMotor3) {
        this.cicloMotor3 = cicloMotor3;
    }

    public BigDecimal getTiempoAPU() {
        return tiempoAPU;
    }

    public void setTiempoAPU(BigDecimal tiempoAPU) {
        this.tiempoAPU = tiempoAPU;
    }

    public Integer getCicloAPU() {
        return cicloAPU;
    }

    public void setCicloAPU(Integer cicloAPU) {
        this.cicloAPU = cicloAPU;
    }

    public String getComentarioCliente() {
        return comentarioCliente;
    }

    public void setComentarioCliente(String comentarioCliente) {
        this.comentarioCliente = comentarioCliente;
    }

    public String getTipoMantenimiento() { return tipoMantenimiento; }
    public void setTipoMantenimiento(String tipoMantenimiento) { this.tipoMantenimiento = tipoMantenimiento; }

    public String getModalidadMantenimiento() { return modalidadMantenimiento; }
    public void setModalidadMantenimiento(String modalidadMantenimiento) { this.modalidadMantenimiento = modalidadMantenimiento; }

    public String getComentarioTaller() { return comentarioTaller; }
    public void setComentarioTaller(String comentarioTaller) { this.comentarioTaller = comentarioTaller; }

    public String getComponenteDescripcion() { return componenteDescripcion; }
    public void setComponenteDescripcion(String componenteDescripcion) { this.componenteDescripcion = componenteDescripcion; }

    public String getComponenteNumeroParte() { return componenteNumeroParte; }
    public void setComponenteNumeroParte(String componenteNumeroParte) { this.componenteNumeroParte = componenteNumeroParte; }

    public String getComponenteNumeroSerie() { return componenteNumeroSerie; }
    public void setComponenteNumeroSerie(String componenteNumeroSerie) { this.componenteNumeroSerie = componenteNumeroSerie; }

    public Integer getComponenteCantidad() { return componenteCantidad; }
    public void setComponenteCantidad(Integer componenteCantidad) { this.componenteCantidad = componenteCantidad; }

    public BigDecimal getComponenteHoras() { return componenteHoras; }
    public void setComponenteHoras(BigDecimal componenteHoras) { this.componenteHoras = componenteHoras; }

    public Integer getComponenteCiclos() { return componenteCiclos; }
    public void setComponenteCiclos(Integer componenteCiclos) { this.componenteCiclos = componenteCiclos; }

    public String getComponenteAeronaveAsociada() { return componenteAeronaveAsociada; }
    public void setComponenteAeronaveAsociada(String componenteAeronaveAsociada) { this.componenteAeronaveAsociada = componenteAeronaveAsociada; }

    public String getComponenteHorasCiclosRemocion() { return componenteHorasCiclosRemocion; }
    public void setComponenteHorasCiclosRemocion(String componenteHorasCiclosRemocion) { this.componenteHorasCiclosRemocion = componenteHorasCiclosRemocion; }

    public List<OTTareaMantenimiento> getTareasMantenimiento() {
        return tareasMantenimiento;
    }

    public void setTareasMantenimiento(List<OTTareaMantenimiento> tareasMantenimiento) {
        this.tareasMantenimiento = tareasMantenimiento;
    }

    public List<OTDiscrepancia> getDiscrepancias() {
        return discrepancias;
    }

    public void setDiscrepancias(List<OTDiscrepancia> discrepancias) {
        this.discrepancias = discrepancias;
    }

    public void agregarTarea(OTTareaMantenimiento tarea) {
        tarea.setOt(this);
        this.tareasMantenimiento.add(tarea);
    }

    public void agregarDiscrepancia(OTDiscrepancia discrepancia) {
        discrepancia.setOt(this);
        this.discrepancias.add(discrepancia);
    }
}