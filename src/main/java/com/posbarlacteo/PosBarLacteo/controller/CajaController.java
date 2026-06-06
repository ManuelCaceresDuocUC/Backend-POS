package com.posbarlacteo.PosBarLacteo.controller; // Ajusta el paquete según tu proyecto

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posbarlacteo.PosBarLacteo.dto.AperturaCajaDTO;
import com.posbarlacteo.PosBarLacteo.dto.EstadoCajaDTO;
import com.posbarlacteo.PosBarLacteo.service.CajaService;

@RestController
@RequestMapping("/api/caja")
// 🔥 Permitimos explícitamente que tu S3 se conecte aquí
@CrossOrigin(origins = "http://posbarlacteo-manuel-2026.s3-website-us-east-1.amazonaws.com")
public class CajaController {

    @Autowired
    private CajaService cajaService;

    @GetMapping("/estado")
    public ResponseEntity<EstadoCajaDTO> obtenerEstadoCaja() {
        Long cajeroId = 1L; // TODO: Cambiar por ID del usuario logueado en el futuro
        boolean estaAbierta = cajaService.tieneCajaAbierta(cajeroId);
        return ResponseEntity.ok(new EstadoCajaDTO(estaAbierta));
    }

    @PostMapping("/abrir")
    public ResponseEntity<?> abrirCaja(@RequestBody AperturaCajaDTO aperturaDTO) {
        Long cajeroId = 1L; 
        try {
            cajaService.abrirCaja(cajeroId, aperturaDTO.getMontoInicial());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cerrar")
    public ResponseEntity<?> cerrarCaja() {
        Long cajeroId = 1L; 
        try {
            cajaService.cerrarCaja(cajeroId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}