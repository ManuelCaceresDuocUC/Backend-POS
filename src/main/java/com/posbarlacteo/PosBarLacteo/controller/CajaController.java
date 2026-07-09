package com.posbarlacteo.PosBarLacteo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // ✨ NUEVO IMPORT
import org.springframework.web.bind.annotation.RestController;

import com.posbarlacteo.PosBarLacteo.dto.AperturaCajaDTO;
import com.posbarlacteo.PosBarLacteo.dto.EstadoCajaDTO;
import com.posbarlacteo.PosBarLacteo.dto.MovimientoCajaDTO; 
import com.posbarlacteo.PosBarLacteo.dto.ResumenCajaDTO;
import com.posbarlacteo.PosBarLacteo.service.CajaService;

@RestController
@RequestMapping("/api/caja")
@CrossOrigin(origins = {
    "http://posbarlacteo-manuel-2026.s3-website-us-east-1.amazonaws.com", 
    "http://localhost:5173",
    "http://34.203.91.138",
    "https://ordpos.duckdns.org",                                             
    "http://192.168.100.85:5173"                                         
})
public class CajaController {

    @Autowired
    private CajaService cajaService;

    @GetMapping("/estado")
    // ✨ Pedimos el usuarioId por la URL
    public ResponseEntity<EstadoCajaDTO> obtenerEstadoCaja(@RequestParam Long usuarioId) {
        boolean estaAbierta = cajaService.tieneCajaAbierta(usuarioId);
        return ResponseEntity.ok(new EstadoCajaDTO(estaAbierta));
    }

    @PostMapping("/abrir")
    // ✨ Ahora pedimos usuarioId Y empresaId por la URL
    public ResponseEntity<?> abrirCaja(
            @RequestParam Long usuarioId, 
            @RequestParam(defaultValue = "1") Long empresaId, // ✨ Agregamos empresaId
            @RequestBody AperturaCajaDTO aperturaDTO) {
        try {
            // ✨ Le pasamos el empresaId al servicio
            cajaService.abrirCaja(usuarioId, empresaId, aperturaDTO.getMontoInicial());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cerrar")
    public ResponseEntity<?> cerrarCaja(@RequestParam Long usuarioId) {
        try {
            cajaService.cerrarCaja(usuarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/movimiento")
    public ResponseEntity<?> registrarMovimiento(@RequestParam Long usuarioId, @RequestBody MovimientoCajaDTO movimientoDTO) {
        try {
            cajaService.registrarMovimiento(usuarioId, movimientoDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumen(@RequestParam Long usuarioId) {
        try {
            ResumenCajaDTO resumen = cajaService.obtenerResumen(usuarioId);
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}