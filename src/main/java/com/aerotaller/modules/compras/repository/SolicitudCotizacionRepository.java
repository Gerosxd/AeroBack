package com.aerotaller.modules.compras.repository;

import com.aerotaller.modelos.SolicitudCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SolicitudCotizacionRepository extends JpaRepository<SolicitudCotizacion, Integer> {
    List<SolicitudCotizacion> findByRequisicionIdRequisicion(Integer idRequisicion);
}