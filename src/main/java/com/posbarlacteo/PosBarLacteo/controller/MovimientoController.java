package com.posbarlacteo.PosBarLacteo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posbarlacteo.PosBarLacteo.model.Venta;
import com.posbarlacteo.PosBarLacteo.repository.VentaRepository;

@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = "http://localhost:5173") // Esto elimina el error de CORS
public class MovimientoController {

    @Autowired
    private VentaRepository ventaRepository;

    @GetMapping
    public List<Venta> obtenerHistorial() {
        // Retorna todas las ventas guardadas en la BD
        return ventaRepository.findAll();
    }
}