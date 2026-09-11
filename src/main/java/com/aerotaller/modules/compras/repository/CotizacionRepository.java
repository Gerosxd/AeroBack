package com.aerotaller.modules.compras.repository;

import com.aerotaller.modelos.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Integer> {
    @Query("SELECT c FROM Cotizacion c WHERE c.solicitudCotizacion.requisicion.idRequisicion = :idReq")
    List<Cotizacion> findByRequisicionId(@Param("idReq") Integer idRequisicion);
}