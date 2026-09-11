package com.aerotaller.modules.ot.service;

import com.aerotaller.modelos.Aeronave;
import com.aerotaller.modelos.Cliente;
import com.aerotaller.modelos.NuevaOT;
import com.aerotaller.modelos.OTDiscrepancia;
import com.aerotaller.modelos.OTTareaMantenimiento;
import com.aerotaller.modules.aeronave.repository.AeronaveRepository;
import com.aerotaller.modules.catalogo.repository.ModeloAeronaveRepository;
import com.aerotaller.modules.ot.dto.*;
import com.aerotaller.modules.catalogo.repository.ClienteRepository;
import com.aerotaller.modules.ot.repository.NuevaOTRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
public class OTServiceImpl implements OTService {

    private final NuevaOTRepository nuevaOTRepository;
    private final AeronaveRepository aeronaveRepository;
    private final ClienteRepository clienteRepository;
    private final ModeloAeronaveRepository modeloAeronaveRepository;

    public OTServiceImpl(NuevaOTRepository nuevaOTRepository,
                         AeronaveRepository aeronaveRepository,
                         ClienteRepository clienteRepository,
                         ModeloAeronaveRepository modeloAeronaveRepository) {
        this.nuevaOTRepository = nuevaOTRepository;
        this.aeronaveRepository = aeronaveRepository;
        this.clienteRepository = clienteRepository;
        this.modeloAeronaveRepository = modeloAeronaveRepository;
    }

    @Override
    public List<AeronaveComboResponse> obtenerMatriculas() {
        return aeronaveRepository.findAllByOrderByMatriculaAsc()
                .stream()
                .map(aeronave -> new AeronaveComboResponse(
                        aeronave.getIdAeronave(),
                        aeronave.getMatricula()
                ))
                .toList();
    }

    @Override
    public SiguienteNoOTResponse obtenerSiguienteNoOT() {
        return new SiguienteNoOTResponse(generarSiguienteNoOT());
    }

    @Override
    @Transactional
    public CrearOTResponse crearOT(CrearOTRequest request) {
        validarRequest(request);

        Aeronave aeronave = aeronaveRepository.findById(request.getIdAeronave().longValue())
                .orElseThrow(() -> new RuntimeException("La matrícula seleccionada no existe."));

        Cliente cliente = null;
        if (request.getIdCliente() != null) {
            cliente = clienteRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new RuntimeException("El cliente seleccionado no existe."));
        }

        NuevaOT nuevaOT = new NuevaOT();
        nuevaOT.setNoOT(generarSiguienteNoOT());
        if (request.getNoOT() != null && !request.getNoOT().isBlank()) {
            nuevaOT.setNoOT(request.getNoOT().trim());
        } else {
            nuevaOT.setNoOT(generarSiguienteNoOT());
        }
        nuevaOT.setMatricula(aeronave);
        nuevaOT.setCliente(cliente);
        nuevaOT.setFechaCreacion(LocalDateTime.now());
        nuevaOT.setFechaApertura(request.getFechaApertura() != null ? request.getFechaApertura() : LocalDateTime.now());
        nuevaOT.setFechaEntrega(request.getFechaEntrega());
        nuevaOT.setFechaCierre(request.getFechaCierre());
        nuevaOT.setEstado("Abierta");

        nuevaOT.setHorasTotales(request.getHorasTotales());
        nuevaOT.setCiclosTotales(request.getCiclosTotales());
        nuevaOT.setTiempoMotor1(request.getTiempoMotor1());
        nuevaOT.setCicloMotor1(request.getCicloMotor1());
        nuevaOT.setTiempoMotor2(request.getTiempoMotor2());
        nuevaOT.setCicloMotor2(request.getCicloMotor2());
        nuevaOT.setTiempoMotor3(request.getTiempoMotor3());
        nuevaOT.setCicloMotor3(request.getCicloMotor3());
        nuevaOT.setTiempoAPU(request.getTiempoAPU());
        nuevaOT.setCicloAPU(request.getCicloAPU());
        nuevaOT.setComentarioCliente(request.getComentarioCliente());
        // P-00: plantillas de impresión
        nuevaOT.setTipoMantenimiento(request.getTipoMantenimiento());
        nuevaOT.setModalidadMantenimiento(request.getModalidadMantenimiento());
        nuevaOT.setComentarioTaller(request.getComentarioTaller());
        nuevaOT.setComponenteDescripcion(request.getComponenteDescripcion());
        nuevaOT.setComponenteNumeroParte(request.getComponenteNumeroParte());
        nuevaOT.setComponenteNumeroSerie(request.getComponenteNumeroSerie());
        nuevaOT.setComponenteCantidad(request.getComponenteCantidad());
        nuevaOT.setComponenteHoras(request.getComponenteHoras());
        nuevaOT.setComponenteCiclos(request.getComponenteCiclos());
        nuevaOT.setComponenteAeronaveAsociada(request.getComponenteAeronaveAsociada());
        nuevaOT.setComponenteHorasCiclosRemocion(request.getComponenteHorasCiclosRemocion());

        if (request.getTareasMantenimiento() != null) {
            for (OTTareaRequest tareaRequest : request.getTareasMantenimiento()) {
                nuevaOT.agregarTarea(crearTareaDesdeRequest(tareaRequest));
            }
        }

        if (request.getDiscrepancias() != null) {
            for (OTDiscrepanciaRequest discrepanciaRequest : request.getDiscrepancias()) {
                nuevaOT.agregarDiscrepancia(crearDiscrepanciaDesdeRequest(discrepanciaRequest));
            }
        }

        NuevaOT guardada = nuevaOTRepository.save(nuevaOT);

        return new CrearOTResponse(
                guardada.getIdOT(),
                guardada.getNoOT(),
                "OT creada correctamente."
        );
    }


    private OTTareaMantenimiento crearTareaDesdeRequest(OTTareaRequest tareaRequest) {
        validarTarea(tareaRequest);
        OTTareaMantenimiento tarea = new OTTareaMantenimiento();
        tarea.setCodigo(tareaRequest.getCodigo().trim());
        tarea.setDescripcion(tareaRequest.getDescripcion().trim());
        tarea.setTecnicos(tareaRequest.getTecnicos() != null ? tareaRequest.getTecnicos().trim() : null);
        tarea.setHorasTotales(tareaRequest.getHorasTotales());
        tarea.setTipoTarea(tareaRequest.getTipoTarea() != null ? tareaRequest.getTipoTarea().trim() : null);
        tarea.setTipoServicio(tareaRequest.getTipoServicio() != null ? tareaRequest.getTipoServicio().trim() : null);
        tarea.setIntervalo(tareaRequest.getIntervalo() != null ? tareaRequest.getIntervalo().trim() : null);
        tarea.setRequiereRII(tareaRequest.getRequiereRII() != null ? tareaRequest.getRequiereRII().trim() : null);
        tarea.setParteAsociada(tareaRequest.getParteAsociada() != null ? tareaRequest.getParteAsociada().trim() : null);
        // P-03: Hoja de Servicio
        tarea.setNumeroParte(tareaRequest.getNumeroParte());
        tarea.setNumeroSerie(tareaRequest.getNumeroSerie());
        tarea.setAccionCorrectiva(tareaRequest.getAccionCorrectiva());
        tarea.setEfectuadoPor(tareaRequest.getEfectuadoPor());
        tarea.setInspeccionadoPor(tareaRequest.getInspeccionadoPor());
        if (tareaRequest.getFechaCumplimiento() != null && !tareaRequest.getFechaCumplimiento().isEmpty()) {
            tarea.setFechaCumplimiento(java.time.LocalDate.parse(tareaRequest.getFechaCumplimiento()));
        }
        tarea.setNumeroParte(tareaRequest.getNumeroParte());
        tarea.setNumeroSerie(tareaRequest.getNumeroSerie());
        tarea.setEfectuadoPor(tareaRequest.getEfectuadoPor());
        tarea.setInspeccionadoPor(tareaRequest.getInspeccionadoPor());
        if (tareaRequest.getFechaCumplimiento() != null && !tareaRequest.getFechaCumplimiento().isEmpty()) {
            tarea.setFechaCumplimiento(java.time.LocalDate.parse(tareaRequest.getFechaCumplimiento()));
        }
        return tarea;
    }


    private OTDiscrepancia crearDiscrepanciaDesdeRequest(OTDiscrepanciaRequest discrepanciaRequest) {
        validarDiscrepancia(discrepanciaRequest);
        OTDiscrepancia discrepancia = new OTDiscrepancia();
        discrepancia.setCodigo(discrepanciaRequest.getCodigo().trim());
        discrepancia.setDescripcion(discrepanciaRequest.getDescripcion().trim());
        discrepancia.setEstatus(discrepanciaRequest.getEstatus() != null ? discrepanciaRequest.getEstatus().trim() : null);
        discrepancia.setAcciones(discrepanciaRequest.getAcciones() != null ? discrepanciaRequest.getAcciones().trim() : null);
        discrepancia.setTipoDiscrepancia(discrepanciaRequest.getTipoDiscrepancia());
        discrepancia.setHhEstimadas(discrepanciaRequest.getHhEstimadas());
        discrepancia.setAeronavegable(discrepanciaRequest.getAeronavegable() != null ? discrepanciaRequest.getAeronavegable().trim() : null);
        discrepancia.setAccionCorrectiva(discrepanciaRequest.getAccionCorrectiva() != null ? discrepanciaRequest.getAccionCorrectiva().trim() : null);
        discrepancia.setEfectuadoPor(discrepanciaRequest.getEfectuadoPor() != null ? discrepanciaRequest.getEfectuadoPor().trim() : null);
        discrepancia.setInspeccionadoPor(discrepanciaRequest.getInspeccionadoPor() != null ? discrepanciaRequest.getInspeccionadoPor().trim() : null);
        discrepancia.setParteAsociada(discrepanciaRequest.getParteAsociada() != null ? discrepanciaRequest.getParteAsociada().trim() : null);
        if (discrepanciaRequest.getFechaAutorizada() != null && !discrepanciaRequest.getFechaAutorizada().isEmpty()) {
            discrepancia.setFechaAutorizada(java.time.LocalDate.parse(discrepanciaRequest.getFechaAutorizada()));
        }
        if (discrepanciaRequest.getFechaLiberacion() != null && !discrepanciaRequest.getFechaLiberacion().isEmpty()) {
            discrepancia.setFechaLiberacion(java.time.LocalDate.parse(discrepanciaRequest.getFechaLiberacion()));
        }
        return discrepancia;
    }

    private void validarRequest(CrearOTRequest request) {
        if (request == null) {
            throw new RuntimeException("La solicitud está vacía.");
        }
        if (request.getIdAeronave() == null) {
            throw new RuntimeException("La matrícula es obligatoria.");
        }
        validarNumeroNoNegativo(request.getHorasTotales(), "Horas totales");
        validarEnteroNoNegativo(request.getCiclosTotales(), "Ciclos totales");
        validarNumeroNoNegativo(request.getTiempoMotor1(), "Tiempo motor 1");
        validarEnteroNoNegativo(request.getCicloMotor1(), "Ciclo motor 1");
        validarNumeroNoNegativo(request.getTiempoMotor2(), "Tiempo motor 2");
        validarEnteroNoNegativo(request.getCicloMotor2(), "Ciclo motor 2");
        validarNumeroNoNegativo(request.getTiempoMotor3(), "Tiempo motor 3");
        validarEnteroNoNegativo(request.getCicloMotor3(), "Ciclo motor 1");
        validarNumeroNoNegativo(request.getTiempoAPU(), "Tiempo APU");
        validarEnteroNoNegativo(request.getCicloAPU(), "Ciclo APU");
    }

    private void validarTarea(OTTareaRequest tarea) {
        if (tarea == null) {
            throw new RuntimeException("Se recibió una tarea vacía.");
        }
        if (tarea.getCodigo() == null || tarea.getCodigo().trim().isEmpty()) {
            throw new RuntimeException("Cada tarea debe tener código.");
        }
        if (tarea.getDescripcion() == null || tarea.getDescripcion().trim().isEmpty()) {
            throw new RuntimeException("Cada tarea debe tener descripción.");
        }
        validarNumeroNoNegativo(tarea.getHorasTotales(), "Horas totales de la tarea");
    }

    private void validarDiscrepancia(OTDiscrepanciaRequest discrepancia) {
        if (discrepancia == null) {
            throw new RuntimeException("Se recibió una discrepancia vacía.");
        }
        if (discrepancia.getCodigo() == null || discrepancia.getCodigo().trim().isEmpty()) {
            throw new RuntimeException("Cada discrepancia debe tener código.");
        }
        if (discrepancia.getDescripcion() == null || discrepancia.getDescripcion().trim().isEmpty()) {
            throw new RuntimeException("Cada discrepancia debe tener descripción.");
        }
    }

    private void validarNumeroNoNegativo(java.math.BigDecimal valor, String campo) {
        if (valor != null && valor.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new RuntimeException(campo + " no puede ser negativo.");
        }
    }

    private void validarEnteroNoNegativo(Integer valor, String campo) {
        if (valor != null && valor < 0) {
            throw new RuntimeException(campo + " no puede ser negativo.");
        }
    }

    private String generarSiguienteNoOT() {
        int anioCompleto = Year.now().getValue(); // Nota: Se mantiene 'anio' por compatibilidad con tu base del código
        int anioCorto = anioCompleto % 100;

        Optional<NuevaOT> ultimaOTOpt = nuevaOTRepository.findTopByOrderByIdOTDesc();
        int siguienteConsecutivo = 1;

        if (ultimaOTOpt.isPresent() && ultimaOTOpt.get().getNoOT() != null) {
            String ultimoNoOT = ultimaOTOpt.get().getNoOT();
            try {
                String[] partes = ultimoNoOT.split("-");
                if (partes.length == 2) {
                    siguienteConsecutivo = Integer.parseInt(partes[1]) + 1; // SOLUCIÓN: Eliminado reasignación innecesaria / Unnecessary boxing
                }
            } catch (Exception ignored) {
                // Se mantiene comportamiento original en fallo
            }
        }

        return "AG/OT/" + anioCorto + "-" + String.format("%03d", siguienteConsecutivo);
    }

    @Override
    public List<OTListadoResponse> listarOTs() {
        return nuevaOTRepository.findAllByOrderByFechaCreacionDescIdOTDesc()
                .stream()
                .map(ot -> new OTListadoResponse(
                        ot.getIdOT(),
                        ot.getNoOT(),
                        ot.getMatricula() != null ? ot.getMatricula().getMatricula() : null,
                        ot.getCliente() != null ? ot.getCliente().getCompania() : "Sin cliente",
                        ot.getFechaCreacion(),
                        ot.getFechaEntrega(),
                        ot.getFechaCierre(),
                        ot.getFechaCierre() != null ? "Cerrada" : "Abierta" // Ajuste dinámico de estado si es necesario
                ))
                .toList();
    }

    @Override
    @Transactional
    public OTDetalleResponse obtenerPorId(Integer idOT) {
        NuevaOT ot = nuevaOTRepository.findById(idOT)
                .orElseThrow(() -> new RuntimeException("La Orden de Trabajo solicitada no existe."));

        OTDetalleResponse response = new OTDetalleResponse();
        response.setIdOT(ot.getIdOT());
        response.setNoOT(ot.getNoOT());
        response.setFechaCreacion(ot.getFechaCreacion());
        response.setFechaApertura(ot.getFechaApertura());
        response.setFechaEntrega(ot.getFechaEntrega());
        response.setFechaCierre(ot.getFechaCierre());
        response.setEstado(ot.getEstado());
        response.setHorasTotales(ot.getHorasTotales());
        response.setCiclosTotales(ot.getCiclosTotales());
        response.setComentarioCliente(ot.getComentarioCliente());
        // P-00: plantillas de impresión
        response.setTipoMantenimiento(ot.getTipoMantenimiento());
        response.setModalidadMantenimiento(ot.getModalidadMantenimiento());
        response.setComentarioTaller(ot.getComentarioTaller());
        response.setComponenteDescripcion(ot.getComponenteDescripcion());
        response.setComponenteNumeroParte(ot.getComponenteNumeroParte());
        response.setComponenteNumeroSerie(ot.getComponenteNumeroSerie());
        response.setComponenteCantidad(ot.getComponenteCantidad());
        response.setComponenteHoras(ot.getComponenteHoras());
        response.setComponenteCiclos(ot.getComponenteCiclos());
        response.setComponenteAeronaveAsociada(ot.getComponenteAeronaveAsociada());
        response.setComponenteHorasCiclosRemocion(ot.getComponenteHorasCiclosRemocion());

        if (ot.getMatricula() != null) {
            Aeronave aeronave = ot.getMatricula();
            response.setMatricula(aeronave.getMatricula());

            // SOLUCIÓN AL ERROR CRÍTICO: Tu repositorio espera un Integer, no un Long.
            // Usamos Integer de manera directa.
            Integer idModelo = aeronave.getModeloAeronave();

            modeloAeronaveRepository.findById(idModelo)
                    .ifPresentOrElse(
                            modeloEntity -> response.setModeloAeronave(modeloEntity.getMarca() + " " + modeloEntity.getModelo()),
                            () -> response.setModeloAeronave("ID Modelo: " + idModelo)
                    );
        }

        if (ot.getCliente() != null) {
            Cliente cl = ot.getCliente();
            response.setClienteCompania(cl.getCompania());
            response.setClienteContacto(cl.getContacto());
            response.setClienteDireccion(cl.getDireccion());
            response.setClienteCiudad(cl.getCiudad() != null && !cl.getCiudad().isBlank() ? cl.getCiudad() : "—");
            response.setClienteEstadoRep(cl.getEstadoRep() != null && !cl.getEstadoRep().isBlank() ? cl.getEstadoRep() : "—");
            response.setClienteTelefono(cl.getTelefono());
            response.setClienteCorreo(cl.getCorreo() != null && !cl.getCorreo().isBlank() ? cl.getCorreo() : "ukn");
        } else {
            response.setClienteCompania("Sin cliente");
            response.setClienteContacto("—");
            response.setClienteDireccion("—");
            response.setClienteCiudad("—");
            response.setClienteEstadoRep("—");
            response.setClienteTelefono("—");
            response.setClienteCorreo("—");
        }

        if (ot.getTareasMantenimiento() != null) {
            response.setTareasMantenimiento(ot.getTareasMantenimiento().stream().map(t -> {
                OTTareaRequest dto = new OTTareaRequest();
                dto.setCodigo(t.getCodigo());
                dto.setDescripcion(t.getDescripcion());
                dto.setTecnicos(t.getTecnicos());
                dto.setHorasTotales(t.getHorasTotales());
                dto.setTipoTarea(t.getTipoTarea());
                dto.setTipoServicio(t.getTipoServicio());
                dto.setIntervalo(t.getIntervalo());
                dto.setRequiereRII(t.getRequiereRII());
                dto.setParteAsociada(t.getParteAsociada());
                dto.setNumeroParte(t.getNumeroParte());
                dto.setNumeroSerie(t.getNumeroSerie());
                dto.setAccionCorrectiva(t.getAccionCorrectiva());
                dto.setEfectuadoPor(t.getEfectuadoPor());
                dto.setInspeccionadoPor(t.getInspeccionadoPor());
                dto.setFechaCumplimiento(t.getFechaCumplimiento() != null ? t.getFechaCumplimiento().toString() : null);
                dto.setNumeroParte(t.getNumeroParte());
                dto.setNumeroSerie(t.getNumeroSerie());
                dto.setEfectuadoPor(t.getEfectuadoPor());
                dto.setInspeccionadoPor(t.getInspeccionadoPor());
                dto.setFechaCumplimiento(t.getFechaCumplimiento() != null ? t.getFechaCumplimiento().toString() : null);
                return dto;
            }).toList());
        }

        if (ot.getDiscrepancias() != null) {
            response.setDiscrepancias(ot.getDiscrepancias().stream().map(d -> {
                OTDiscrepanciaRequest dto = new OTDiscrepanciaRequest();
                dto.setCodigo(d.getCodigo());
                dto.setDescripcion(d.getDescripcion());
                dto.setEstatus(d.getEstatus());
                dto.setAcciones(d.getAcciones());
                dto.setTipoDiscrepancia(d.getTipoDiscrepancia());
                dto.setHhEstimadas(d.getHhEstimadas());
                dto.setAeronavegable(d.getAeronavegable());
                dto.setFechaAutorizada(d.getFechaAutorizada() != null ? d.getFechaAutorizada().toString() : null);
                dto.setAccionCorrectiva(d.getAccionCorrectiva());
                dto.setFechaLiberacion(d.getFechaLiberacion() != null ? d.getFechaLiberacion().toString() : null);
                dto.setEfectuadoPor(d.getEfectuadoPor());
                dto.setInspeccionadoPor(d.getInspeccionadoPor());
                dto.setParteAsociada(d.getParteAsociada());
                return dto;
            }).toList());
        }

        return response;
    }

    @Override
    @Transactional // Mantiene la consistencia en tu base de datos de MySQL
    public void actualizarOT(Integer id, CrearOTRequest request) {

        // Buscamos la Orden de Trabajo usando tu repositorio real
        NuevaOT ot = nuevaOTRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La Orden de Trabajo con ID " + id + " no existe."));

        // Como CrearOTRequest no tiene 'getEstado', dejamos el que ya tiene la BD
        ot.setEstado(ot.getEstado());

        // SOLUCIÓN: Si request.getHorasTotales() ya es un BigDecimal o un Double,
        // lo manejamos de forma segura para evitar el error de 'Cannot resolve method valueOf'
        if (request.getHorasTotales() != null) {
            // Si en tu DTO es de tipo Double, usa: BigDecimal.valueOf(request.getHorasTotales())
            // Si en tu DTO ya es de tipo BigDecimal, se asigna directo: ot.setHorasTotales(request.getHorasTotales())
            // Para asegurar que compile sin importar el DTO, usamos el constructor New:
            ot.setHorasTotales(new BigDecimal(request.getHorasTotales().toString()));
        } else {
            ot.setHorasTotales(null);
        }

        ot.setCiclosTotales(request.getCiclosTotales());
        ot.setFechaEntrega(request.getFechaEntrega());
        ot.setFechaCierre(request.getFechaCierre());
        ot.setComentarioCliente(request.getComentarioCliente());
        // P-00: plantillas de impresión
        ot.setTipoMantenimiento(request.getTipoMantenimiento());
        ot.setModalidadMantenimiento(request.getModalidadMantenimiento());
        ot.setComentarioTaller(request.getComentarioTaller());
        ot.setComponenteDescripcion(request.getComponenteDescripcion());
        ot.setComponenteNumeroParte(request.getComponenteNumeroParte());
        ot.setComponenteNumeroSerie(request.getComponenteNumeroSerie());
        ot.setComponenteCantidad(request.getComponenteCantidad());
        ot.setComponenteHoras(request.getComponenteHoras());
        ot.setComponenteCiclos(request.getComponenteCiclos());
        ot.setComponenteAeronaveAsociada(request.getComponenteAeronaveAsociada());
        ot.setComponenteHorasCiclosRemocion(request.getComponenteHorasCiclosRemocion());

        // Sincronizar Colección de Tareas de Mantenimiento (Clear & Refill)
        ot.getTareasMantenimiento().clear();
        if (request.getTareasMantenimiento() != null) {
            request.getTareasMantenimiento().forEach(tDto -> {
                OTTareaMantenimiento tarea = new OTTareaMantenimiento();
                tarea.setCodigo(tDto.getCodigo());
                tarea.setDescripcion(tDto.getDescripcion());
                tarea.setTecnicos(tDto.getTecnicos());

                // SOLUCIÓN: Conversión segura para las tareas hijas
                if (tDto.getHorasTotales() != null) {
                    tarea.setHorasTotales(new BigDecimal(tDto.getHorasTotales().toString()));
                }

                tarea.setTipoTarea(tDto.getTipoTarea());
                tarea.setTipoServicio(tDto.getTipoServicio());
                tarea.setIntervalo(tDto.getIntervalo());
                tarea.setRequiereRII(tDto.getRequiereRII());
                tarea.setParteAsociada(tDto.getParteAsociada());
                tarea.setNumeroParte(tDto.getNumeroParte());
                tarea.setNumeroSerie(tDto.getNumeroSerie());
                tarea.setAccionCorrectiva(tDto.getAccionCorrectiva());
                tarea.setEfectuadoPor(tDto.getEfectuadoPor());
                tarea.setInspeccionadoPor(tDto.getInspeccionadoPor());
                if (tDto.getFechaCumplimiento() != null && !tDto.getFechaCumplimiento().isEmpty()) {
                    tarea.setFechaCumplimiento(java.time.LocalDate.parse(tDto.getFechaCumplimiento()));
                }
                tarea.setNumeroParte(tDto.getNumeroParte());
                tarea.setNumeroSerie(tDto.getNumeroSerie());
                tarea.setEfectuadoPor(tDto.getEfectuadoPor());
                tarea.setInspeccionadoPor(tDto.getInspeccionadoPor());
                if (tDto.getFechaCumplimiento() != null && !tDto.getFechaCumplimiento().isEmpty()) {
                    tarea.setFechaCumplimiento(java.time.LocalDate.parse(tDto.getFechaCumplimiento()));
                }
                ot.agregarTarea(tarea); // Helper bidireccional de tu entidad
            });
        }

        // Sincronizar Colección de Discrepancias
        ot.getDiscrepancias().clear();
        if (request.getDiscrepancias() != null) {
            request.getDiscrepancias().forEach(dDto -> {
                OTDiscrepancia discrepancia = new OTDiscrepancia();
                discrepancia.setCodigo(dDto.getCodigo());
                discrepancia.setDescripcion(dDto.getDescripcion());
                discrepancia.setEstatus(dDto.getEstatus());
                discrepancia.setAcciones(dDto.getAcciones());
                discrepancia.setTipoDiscrepancia(dDto.getTipoDiscrepancia());
                discrepancia.setHhEstimadas(dDto.getHhEstimadas());
                discrepancia.setAeronavegable(dDto.getAeronavegable());
                discrepancia.setAccionCorrectiva(dDto.getAccionCorrectiva());
                discrepancia.setEfectuadoPor(dDto.getEfectuadoPor());
                discrepancia.setInspeccionadoPor(dDto.getInspeccionadoPor());
                discrepancia.setParteAsociada(dDto.getParteAsociada());
                if (dDto.getFechaAutorizada() != null && !dDto.getFechaAutorizada().isEmpty()) {
                    discrepancia.setFechaAutorizada(java.time.LocalDate.parse(dDto.getFechaAutorizada()));
                }
                if (dDto.getFechaLiberacion() != null && !dDto.getFechaLiberacion().isEmpty()) {
                    discrepancia.setFechaLiberacion(java.time.LocalDate.parse(dDto.getFechaLiberacion()));
                }
                ot.agregarDiscrepancia(discrepancia); // Helper bidireccional de tu entidad
            });
        }

        // Guardamostodo el árbol relacional en MySQL
        nuevaOTRepository.save(ot);
    }

}