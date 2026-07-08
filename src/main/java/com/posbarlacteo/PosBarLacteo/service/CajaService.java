package com.posbarlacteo.PosBarLacteo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.posbarlacteo.PosBarLacteo.dto.MovimientoCajaDTO;
import com.posbarlacteo.PosBarLacteo.dto.ResumenCajaDTO;
import com.posbarlacteo.PosBarLacteo.model.Empresa;
import com.posbarlacteo.PosBarLacteo.model.TurnoCaja;
import com.posbarlacteo.PosBarLacteo.model.Usuario;
import com.posbarlacteo.PosBarLacteo.repository.EmpresaRepository;
import com.posbarlacteo.PosBarLacteo.repository.TurnoCajaRepository;
import com.posbarlacteo.PosBarLacteo.repository.UsuarioRepository;

@Service
public class CajaService {

    @Autowired
    private TurnoCajaRepository turnoCajaRepository;

    // ✨ Movimos los repositorios ADENTRO de la clase
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    public boolean tieneCajaAbierta(Long cajeroId) {
        return turnoCajaRepository.findByCajeroIdAndEstado(cajeroId, "ABIERTA").isPresent();
    }

    // ✨ Cambiamos Double a BigDecimal
    public void abrirCaja(Long usuarioId, Long empresaId, BigDecimal montoInicial) {
        // 1. Validar que el usuario no tenga ya una caja abierta
        if (tieneCajaAbierta(usuarioId)) {
            throw new RuntimeException("El usuario ya tiene una caja abierta");
        }

        // 2. Buscar al usuario y a la empresa
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
        Empresa empresa = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        // 3. Crear el turno y asignarle todo
        TurnoCaja turno = new TurnoCaja();
        
        // ✨ Ajustado a los nombres reales de tu entidad TurnoCaja:
        turno.setCajeroId(usuarioId); // ⚠️ Si te da error aquí, cámbialo por: turno.setCajero(usuario);
        turno.setEmpresa(empresa); 
        turno.setFechaApertura(LocalDateTime.now());
        turno.setMontoApertura(montoInicial); // ✨ Usamos montoApertura (basado en tu método de resumen)
        turno.setEstado("ABIERTA"); // ✨ Corregido a "ABIERTA" (femenino) para que coincida con tus otros métodos

        // 4. Guardar
        turnoCajaRepository.save(turno);
    }

    public void cerrarCaja(Long cajeroId) {
        TurnoCaja turnoAbierto = turnoCajaRepository.findByCajeroIdAndEstado(cajeroId, "ABIERTA")
                .orElseThrow(() -> new RuntimeException("No hay ninguna caja abierta para este usuario."));

        turnoAbierto.setEstado("CERRADA");
        turnoAbierto.setFechaCierre(LocalDateTime.now());

        turnoCajaRepository.save(turnoAbierto);
    }

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
    }

    public ResumenCajaDTO obtenerResumen(Long cajeroId) {
        TurnoCaja turnoAbierto = turnoCajaRepository.findByCajeroIdAndEstado(cajeroId, "ABIERTA")
                .orElseThrow(() -> new RuntimeException("No hay ninguna caja abierta."));

        BigDecimal fondo = turnoAbierto.getMontoApertura() != null ? turnoAbierto.getMontoApertura() : BigDecimal.ZERO;
        BigDecimal vEfectivo = turnoAbierto.getVentasEfectivo() != null ? turnoAbierto.getVentasEfectivo() : BigDecimal.ZERO;
        BigDecimal vTarjeta = turnoAbierto.getVentasTarjeta() != null ? turnoAbierto.getVentasTarjeta() : BigDecimal.ZERO;
        BigDecimal ingresos = turnoAbierto.getIngresosExtra() != null ? turnoAbierto.getIngresosExtra() : BigDecimal.ZERO;
        BigDecimal retiros = turnoAbierto.getRetiros() != null ? turnoAbierto.getRetiros() : BigDecimal.ZERO;

        BigDecimal totalEnCaja = fondo.add(vEfectivo).add(ingresos).subtract(retiros);

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