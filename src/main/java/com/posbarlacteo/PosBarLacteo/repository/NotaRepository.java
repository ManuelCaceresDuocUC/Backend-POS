package com.posbarlacteo.PosBarLacteo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.posbarlacteo.PosBarLacteo.model.Nota;

public interface NotaRepository extends JpaRepository<Nota, Long> {
    // Para mostrar las más recientes primero
    List<Nota> findAllByOrderByFechaHoraDesc(); 
}