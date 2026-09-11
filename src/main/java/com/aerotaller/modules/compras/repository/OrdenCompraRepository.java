package com.aerotaller.modules.compras.repository;

import com.aerotaller.modelos.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Integer> {
    Optional<OrdenCompra> findByFolio(String folio);
    boolean existsByFolio(String folio);
}