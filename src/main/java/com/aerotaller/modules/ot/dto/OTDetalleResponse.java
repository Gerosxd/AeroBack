package com.aerotaller.modules.ot.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OTDetalleResponse {
    private Integer idOT;
    private String noOT;
    private String matricula;
    private String modeloAeronave;

    // DATOS DE CLIENTE DESACOPLADOS
    private String clienteCompania;
    private String clienteContacto;
    private String clienteDireccion;
    private String clienteCiudad;
    private String clienteEstadoRep;
    private String clienteTelefono;
    private String clienteCorreo;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaApertura;
    private LocalDate fechaEntrega;
    private LocalDate fechaCierre;
    private String estado;
    private BigDecimal horasTotales;
    private Integer ciclosTotales;
    private String comentarioCliente;

    // P-00: Campos plantillas de impresión
    private String tipoMantenimiento;
    private String modalidadMantenimiento;
    private String comentarioTaller;
    private String componenteDescripcion;
    private String componenteNumeroParte;
    private String componenteNumeroSerie;
    private Integer componenteCantidad;
    private BigDecimal componenteHoras;
    private Integer componenteCiclos;
    private String componenteAeronaveAsociada;
    private String componenteHorasCiclosRemocion;

    private List<OTTareaRequest> tareasMantenimiento;
    private List<OTDiscrepanciaRequest> discrepancias;

    public OTDetalleResponse() {}

    public Integer getIdOT() { return idOT; }
    public void setIdOT(Integer idOT) { this.idOT = idOT; }

    public String getNoOT() { return noOT; }
    public void setNoOT(String noOT) { this.noOT = noOT; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getModeloAeronave() { return modeloAeronave; }
    public void setModeloAeronave(String modeloAeronave) { this.modeloAeronave = modeloAeronave; }

    public String getClienteCompania() { return clienteCompania; }
    public void setClienteCompania(String clienteCompania) { this.clienteCompania = clienteCompania; }

    public String getClienteContacto() { return clienteContacto; }
    public void setClienteContacto(String clienteContacto) { this.clienteContacto = clienteContacto; }

    public String getClienteDireccion() { return clienteDireccion; }
    public void setClienteDireccion(String clienteDireccion) { this.clienteDireccion = clienteDireccion; }

    public String getClienteCiudad() { return clienteCiudad; }
    public void setClienteCiudad(String clienteCiudad) { this.clienteCiudad = clienteCiudad; }

    public String getClienteEstadoRep() { return clienteEstadoRep; }
    public void setClienteEstadoRep(String clienteEstadoRep) { this.clienteEstadoRep = clienteEstadoRep; }

    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }

    public String getClienteCorreo() { return clienteCorreo; }
    public void setClienteCorreo(String clienteCorreo) { this.clienteCorreo = clienteCorreo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }

    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public LocalDate getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDate fechaCierre) { this.fechaCierre = fechaCierre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getHorasTotales() { return horasTotales; }
    public void setHorasTotales(BigDecimal horasTotales) { this.horasTotales = horasTotales; }

    public Integer getCiclosTotales() { return ciclosTotales; }
    public void setCiclosTotales(Integer ciclosTotales) { this.ciclosTotales = ciclosTotales; }

    public String getComentarioCliente() { return comentarioCliente; }
    public void setComentarioCliente(String comentarioCliente) { this.comentarioCliente = comentarioCliente; }

    public List<OTTareaRequest> getTareasMantenimiento() { return tareasMantenimiento; }
    public void setTareasMantenimiento(List<OTTareaRequest> tareasMantenimiento) { this.tareasMantenimiento = tareasMantenimiento; }

    public List<OTDiscrepanciaRequest> getDiscrepancias() { return discrepancias; }
    public void setDiscrepancias(List<OTDiscrepanciaRequest> discrepancias) { this.discrepancias = discrepancias; }

    public String getTipoMantenimiento() { return tipoMantenimiento; }
    public void setTipoMantenimiento(String v) { this.tipoMantenimiento = v; }
    public String getModalidadMantenimiento() { return modalidadMantenimiento; }
    public void setModalidadMantenimiento(String v) { this.modalidadMantenimiento = v; }
    public String getComentarioTaller() { return comentarioTaller; }
    public void setComentarioTaller(String v) { this.comentarioTaller = v; }
    public String getComponenteDescripcion() { return componenteDescripcion; }
    public void setComponenteDescripcion(String v) { this.componenteDescripcion = v; }
    public String getComponenteNumeroParte() { return componenteNumeroParte; }
    public void setComponenteNumeroParte(String v) { this.componenteNumeroParte = v; }
    public String getComponenteNumeroSerie() { return componenteNumeroSerie; }
    public void setComponenteNumeroSerie(String v) { this.componenteNumeroSerie = v; }
    public Integer getComponenteCantidad() { return componenteCantidad; }
    public void setComponenteCantidad(Integer v) { this.componenteCantidad = v; }
    public BigDecimal getComponenteHoras() { return componenteHoras; }
    public void setComponenteHoras(BigDecimal v) { this.componenteHoras = v; }
    public Integer getComponenteCiclos() { return componenteCiclos; }
    public void setComponenteCiclos(Integer v) { this.componenteCiclos = v; }
    public String getComponenteAeronaveAsociada() { return componenteAeronaveAsociada; }
    public void setComponenteAeronaveAsociada(String v) { this.componenteAeronaveAsociada = v; }
    public String getComponenteHorasCiclosRemocion() { return componenteHorasCiclosRemocion; }
    public void setComponenteHorasCiclosRemocion(String v) { this.componenteHorasCiclosRemocion = v; }
}