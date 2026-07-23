package com.aerotaller.modules.ot.dto;

public class OTDiscrepanciaRequest
{

    private String codigo;
    private String descripcion;
    private String estatus;
    private String acciones;
    private String tipoDiscrepancia;
    private java.math.BigDecimal hhEstimadas;
    private String aeronavegable;
    private String fechaAutorizada;
    private String accionCorrectiva;
    private String fechaLiberacion;
    private String efectuadoPor;
    private String inspeccionadoPor;
    private String parteAsociada;

    public OTDiscrepanciaRequest()
    {
    }

    public String getCodigo()
    {
        return codigo;
    }

    public void setCodigo(String codigo)
    {
        this.codigo = codigo;
    }

    public String getDescripcion()
    {
        return descripcion;
    }

    public void setDescripcion(String descripcion)
    {
        this.descripcion = descripcion;
    }

    public String getEstatus()
    {
        return estatus;
    }

    public void setEstatus(String estatus)
    {
        this.estatus = estatus;
    }

    public String getAcciones()
    {
        return acciones;
    }

    public void setAcciones(String acciones)
    {
        this.acciones = acciones;
    }

    public String getAeronavegable() { return aeronavegable; }
    public void setAeronavegable(String aeronavegable) { this.aeronavegable = aeronavegable; }

    public String getFechaAutorizada() { return fechaAutorizada; }
    public void setFechaAutorizada(String fechaAutorizada) { this.fechaAutorizada = fechaAutorizada; }

    public String getAccionCorrectiva() { return accionCorrectiva; }
    public void setAccionCorrectiva(String accionCorrectiva) { this.accionCorrectiva = accionCorrectiva; }

    public String getFechaLiberacion() { return fechaLiberacion; }
    public void setFechaLiberacion(String fechaLiberacion) { this.fechaLiberacion = fechaLiberacion; }

    public String getEfectuadoPor() { return efectuadoPor; }
    public void setEfectuadoPor(String efectuadoPor) { this.efectuadoPor = efectuadoPor; }

    public String getInspeccionadoPor() { return inspeccionadoPor; }
    public void setInspeccionadoPor(String inspeccionadoPor) { this.inspeccionadoPor = inspeccionadoPor; }

    public String getParteAsociada() { return parteAsociada; }
    public void setParteAsociada(String parteAsociada) { this.parteAsociada = parteAsociada; }

    public String getTipoDiscrepancia() { return tipoDiscrepancia; }
    public void setTipoDiscrepancia(String v) { this.tipoDiscrepancia = v; }
    public java.math.BigDecimal getHhEstimadas() { return hhEstimadas; }
    public void setHhEstimadas(java.math.BigDecimal v) { this.hhEstimadas = v; }
}