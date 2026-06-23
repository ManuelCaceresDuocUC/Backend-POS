package com.posbarlacteo.PosBarLacteo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.posbarlacteo.PosBarLacteo.dto.MovimientoCajaDTO;
import com.posbarlacteo.PosBarLacteo.dto.ResumenCajaDTO;
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
        
        // Es buena práctica inicializar los acumuladores en 0 al abrir la caja
        nuevoTurno.setIngresosExtra(BigDecimal.ZERO);
        nuevoTurno.setRetiros(BigDecimal.ZERO);
        nuevoTurno.setVentasEfectivo(BigDecimal.ZERO);
        nuevoTurno.setVentasTarjeta(BigDecimal.ZERO);

        turnoCajaRepository.save(nuevoTurno);
    }

    public void cerrarCaja(Long cajeroId) {
        TurnoCaja turnoAbierto = turnoCajaRepository.findByCajeroIdAndEstado(cajeroId, "ABIERTA")
                .orElseThrow(() -> new RuntimeException("No hay ninguna caja abierta para este usuario."));

        turnoAbierto.setEstado("CERRADA");
        turnoAbierto.setFechaCierre(LocalDateTime.now());

        turnoCajaRepository.save(turnoAbierto);
    }

    // ✨ NUEVO: Método para registrar ingresos o retiros
    public void registrarMovimiento(Long cajeroId, MovimientoCajaDTO movimiento) {
        TurnoCaja turnoAbierto = turnoCajaRepository.findByCajeroIdAndEstado(cajeroId, "ABIERTA")
                .orElseThrow(() -> new RuntimeException("No hay ninguna caja abierta para registrar movimientos."));

        BigDecimal monto = BigDecimal.valueOf(movimiento.getMonto());

        if ("ingreso".equalsIgnoreCase(movimiento.getTipo())) {
            BigDecimal actual = turnoAbierto.getIngresosExtra() != null ? turnoAbierto.getIngresosExtra() : BigDecimal.ZERO;
            turnoAbierto.setIngresosExtra(actual.add(monto));
        } else if ("retiro".equalsIgnoreCase(movimiento.getTipo())) {
            BigDecimal actual = turnoAbierto.getRetiros() != null ? turnoAbierto.getRetiros() : BigDecimal.ZERO;
            turnoAbierto.setRetiros(actual.add(monto));
        } else {
            throw new RuntimeException("Tipo de movimiento inválido. Debe ser 'ingreso' o 'retiro'.");
        }

        turnoCajaRepository.save(turnoAbierto);
        
        // Nota: Si en el futuro creas una tabla/entidad "HistorialMovimientos", 
        // aquí es donde harías el repository.save() de ese registro individual para auditoría.
    }

    // ✨ NUEVO: Método para compilar y enviar el resumen al frontend
    public ResumenCajaDTO obtenerResumen(Long cajeroId) {
        TurnoCaja turnoAbierto = turnoCajaRepository.findByCajeroIdAndEstado(cajeroId, "ABIERTA")
                .orElseThrow(() -> new RuntimeException("No hay ninguna caja abierta."));

        // Obtener valores manejando posibles nulos de la base de datos
        BigDecimal fondo = turnoAbierto.getMontoApertura() != null ? turnoAbierto.getMontoApertura() : BigDecimal.ZERO;
        BigDecimal vEfectivo = turnoAbierto.getVentasEfectivo() != null ? turnoAbierto.getVentasEfectivo() : BigDecimal.ZERO;
        BigDecimal vTarjeta = turnoAbierto.getVentasTarjeta() != null ? turnoAbierto.getVentasTarjeta() : BigDecimal.ZERO;
        BigDecimal ingresos = turnoAbierto.getIngresosExtra() != null ? turnoAbierto.getIngresosExtra() : BigDecimal.ZERO;
        BigDecimal retiros = turnoAbierto.getRetiros() != null ? turnoAbierto.getRetiros() : BigDecimal.ZERO;

        // Calcular el total esperado en la caja (Fondo + Ventas Efectivo + Ingresos Extra - Retiros)
        BigDecimal totalEnCaja = fondo.add(vEfectivo).add(ingresos).subtract(retiros);

        // Retornamos el DTO con los valores convertidos a Double para Jackson/React
        return new ResumenCajaDTO(
            fondo.doubleValue(),
            vEfectivo.doubleValue(),
            vTarjeta.doubleValue(),
            ingresos.doubleValue(),
            retiros.doubleValue(),
            totalEnCaja.doubleValue()
        );
    }
}