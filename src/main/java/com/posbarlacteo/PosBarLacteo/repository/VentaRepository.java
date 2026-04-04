package com.posbarlacteo.PosBarLacteo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.posbarlacteo.PosBarLacteo.model.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    
}
