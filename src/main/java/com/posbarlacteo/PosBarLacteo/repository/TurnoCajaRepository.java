package com.posbarlacteo.PosBarLacteo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.posbarlacteo.PosBarLacteo.model.TurnoCaja;

@Repository
public interface TurnoCajaRepository extends JpaRepository<TurnoCaja, Long> {
    
    // Necesitamos este método para encontrar la caja abierta de un usuario específico
    Optional<TurnoCaja> findByCajeroIdAndEstado(Long cajeroId, String estado);
}