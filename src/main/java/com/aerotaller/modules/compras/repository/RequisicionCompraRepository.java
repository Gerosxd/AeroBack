package com.aerotaller.modules.compras.repository;

import com.aerotaller.modelos.RequisicionCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RequisicionCompraRepository extends JpaRepository<RequisicionCompra, Integer> {
    Optional<RequisicionCompra> findByFolio(String folio);
    boolean existsByFolio(String folio);
}