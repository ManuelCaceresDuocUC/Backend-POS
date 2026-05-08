package com.posbarlacteo.PosBarLacteo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posbarlacteo.PosBarLacteo.dto.PagoRequest;
import com.posbarlacteo.PosBarLacteo.service.VentaService;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired
    private VentaService ventaService;

    @PostMapping("/efectivo")
    public ResponseEntity<?> procesarEfectivo(@RequestBody PagoRequest request) {
        try {
            ventaService.procesarVentaCompleta(request.getItems(), (double) request.getMonto(), "EFECTIVO");
            return ResponseEntity.ok(Map.of("status", "success", "message", "Venta en efectivo registrada en la nube"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/cobrar")
    public ResponseEntity<?> procesarPagoTarjeta(@RequestBody PagoRequest request) {
        try {
            if (request.getItems() == null || request.getItems().isEmpty()) {
                throw new Exception("El carrito está vacío");
            }

            // Ya NO llamamos a Getnet aquí. React ya se encargó de eso con el Agente Local.
            // Directamente guardamos la venta en la base de datos RDS.
            ventaService.procesarVentaCompleta(request.getItems(), (double) request.getMonto(), "TARJETA");
            
            return ResponseEntity.ok(Map.of("status", "success", "message", "Venta con tarjeta registrada en la nube"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
}