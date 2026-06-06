package com.posbarlacteo.PosBarLacteo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.posbarlacteo.PosBarLacteo.model.TurnoCaja;
import com.posbarlacteo.PosBarLacteo.repository.TurnoCajaRepository;

@Service
public class CajaService {

    @Autowired
    private TurnoCajaRepository turnoCajaRepository;

    public boolean tieneCajaAbierta(Long cajeroId) {
        return turnoCajaRepository.findByCajeroIdAndEstado(cajeroId, "ABIERTA").isPresent();
    }

    public void abrirCaja(Long cajeroId, BigDecimal montoInicial) {
        if (tieneCajaAbierta(cajeroId)) {
            throw new RuntimeException("El cajero ya tiene una caja abierta.");
        }

        TurnoCaja nuevoTurno = new TurnoCaja();
        nuevoTurno.setCajeroId(cajeroId);
        nuevoTurno.setMontoApertura(montoInicial);
        nuevoTurno.setEstado("ABIERTA");
        nuevoTurno.setFechaApertura(LocalDateTime.now());

        turnoCajaRepository.save(nuevoTurno);
    }

    public void cerrarCaja(Long cajeroId) {
        TurnoCaja turnoAbierto = turnoCajaRepository.findByCajeroIdAndEstado(cajeroId, "ABIERTA")
                .orElseThrow(() -> new RuntimeException("No hay ninguna caja abierta para este usuario."));

        turnoAbierto.setEstado("CERRADA");
        turnoAbierto.setFechaCierre(LocalDateTime.now());

        turnoCajaRepository.save(turnoAbierto);
    }
}