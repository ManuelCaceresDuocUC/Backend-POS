package com.posbarlacteo.PosBarLacteo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posbarlacteo.PosBarLacteo.dto.PagoRequest;
import com.posbarlacteo.PosBarLacteo.service.GetnetService;
import com.posbarlacteo.PosBarLacteo.service.VentaService;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "http://localhost:5173")
public class PagoController {

    @Autowired
    private GetnetService getnetService;

    @Autowired
    private VentaService ventaService;

    @PostMapping("/efectivo")
    public ResponseEntity<?> procesarEfectivo(@RequestBody PagoRequest request) {
        try {
            ventaService.procesarVentaCompleta(request.getItems(), (double) request.getMonto(), "EFECTIVO");
            return ResponseEntity.ok(Map.of("status", "success", "message", "Venta en efectivo registrada"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/cobrar")
    public ResponseEntity<?> procesarPago(@RequestBody PagoRequest request) {
        try {
            if (request.getItems() == null || request.getItems().isEmpty()) {
                throw new Exception("El carrito está vacío");
            }

            getnetService.conectar();
            getnetService.iniciarCobro(request.getMonto()); 
            
            ventaService.procesarVentaCompleta(request.getItems(), (double) request.getMonto(), "TARJETA");
            
            return ResponseEntity.ok(Map.of("status", "success", "message", "Venta procesada con éxito"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
}

// --- Clases de apoyo (Asegúrate de que estén fuera de la clase PagoController o en archivos propios) ---
