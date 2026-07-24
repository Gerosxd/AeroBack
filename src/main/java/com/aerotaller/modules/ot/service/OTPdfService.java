package com.aerotaller.modules.ot.service;

public interface OTPdfService {

    /**
     * P-02: Genera la carátula oficial AG-145-03 (Orden de Trabajo) para una OT.
     *
     * @param idOT identificador de la orden de trabajo
     * @return contenido del PDF en bytes
     */
    byte[] generarCaratula(Integer idOT);

    /**
     * P-03: Genera la Hoja de Servicio oficial AG-145-04 para una tarea específica de la OT.
     *
     * @param idOT       identificador de la orden de trabajo
     * @param idTareaOT  identificador de la tarea dentro de la OT
     * @return contenido del PDF en bytes
     */
    byte[] generarHojaServicio(Integer idOT, Integer idTareaOT);

    /**
     * P-03: Genera un PDF con la Hoja de Servicio de todas las tareas de la OT
     * (una hoja por tarea, en el orden en que aparecen en la orden).
     *
     * @param idOT identificador de la orden de trabajo
     * @return contenido del PDF en bytes
     */
    byte[] generarHojasServicioTodas(Integer idOT);

    /**
     * P-04: Genera el formato oficial AG-145-12 (Discrepancias) para una discrepancia específica.
     *
     * @param idOT              identificador de la orden de trabajo
     * @param idOTDiscrepancia  identificador de la discrepancia dentro de la OT
     * @return contenido del PDF en bytes
     */
    byte[] generarDiscrepancia(Integer idOT, Integer idOTDiscrepancia);

    /**
     * P-04: Genera el formato AG-145-12 con todas las discrepancias de la OT.
     * Se imprimen dos discrepancias por hoja (AGD-01/AGD-02, AGD-03/AGD-04, ...).
     *
     * @param idOT identificador de la orden de trabajo
     * @return contenido del PDF en bytes
     */
    byte[] generarDiscrepanciasTodas(Integer idOT);
}