package com.posbarlacteo.PosBarLacteo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.posbarlacteo.PosBarLacteo.model.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
}
