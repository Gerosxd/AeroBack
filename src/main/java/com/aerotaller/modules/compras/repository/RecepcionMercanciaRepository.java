package com.aerotaller.modules.compras.repository;

import com.aerotaller.modelos.RecepcionMercancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecepcionMercanciaRepository extends JpaRepository<RecepcionMercancia, Integer> {
    List<RecepcionMercancia> findByOrdenCompraIdOrdenCompra(Integer idOrdenCompra);
}