package com.posbarlacteo.PosBarLacteo.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.posbarlacteo.PosBarLacteo.model.Cliente;
import com.posbarlacteo.PosBarLacteo.repository.ClienteRepository;
import com.posbarlacteo.PosBarLacteo.service.CajaService;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = {
    "http://posbarlacteo-manuel-2026.s3-website-us-east-1.amazonaws.com",
    "http://localhost:5173",
    "http://34.203.91.138",
    "https://ordpos.duckdns.org",
    "http://192.168.100.85:5173"
})
public class ClienteController {

    private final ClienteRepository clienteRepository;

    public ClienteController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @PostMapping
    public ResponseEntity<?> crearCliente(@RequestBody Cliente cliente) {
        try {
            if (cliente.getNombre() == null || cliente.getNombre().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "El nombre es obligatorio"));
            }
            if (cliente.getRut() == null || cliente.getRut().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "El RUT es obligatorio"));
            }

            Cliente nuevoCliente = clienteRepository.save(cliente);
            return ResponseEntity.ok(nuevoCliente);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error al crear cliente: " + e.getMessage()));
        }
    }
    // 📋 OBTENER TODOS LOS CLIENTES (Para el reporte de Excel y listados)
    @GetMapping
    public ResponseEntity<?> obtenerTodosLosClientes(@RequestParam(required = false) Long empresaId) {
        try {
            // Utilizamos findAll() para obtener la lista completa de clientes de la base de datos
            List<Cliente> clientes = clienteRepository.findAll();
            
            // Nota: Si en el futuro tu ClienteRepository tiene un método findByEmpresaId(empresaId),
            // podrías usarlo aquí para filtrar por empresa. Por ahora findAll() funcionará perfecto.
            
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error al obtener clientes: " + e.getMessage()));
        }
    }

    // 🔍 BUSCAR CLIENTE POR RUT O ID
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarCliente(@RequestParam String termino) {
        try {
            // Intentar buscar por ID si el término es numérico
            if (termino.matches("\\d+")) {
                Long id = Long.parseLong(termino);
                Optional<Cliente> clienteOpt = clienteRepository.findById(id);
                if (clienteOpt.isPresent()) {
                    return ResponseEntity.ok(clienteOpt.get());
                }
            }

            // Buscar por RUT exacto
            Optional<Cliente> clienteOpt = clienteRepository.findByRut(termino);
            if (clienteOpt.isPresent()) {
                return ResponseEntity.ok(clienteOpt.get());
            }

            return ResponseEntity.status(404).body(Map.of("message", "Cliente no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error en la búsqueda: " + e.getMessage()));
        }
    }

    // 💰 ABONAR A LA DEUDA DEL CLIENTE
    @Autowired
    private CajaService cajaService;
    @PostMapping("/{id}/abonar")
    public ResponseEntity<?> abonarDeuda(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            Optional<Cliente> clienteOpt = clienteRepository.findById(id);
            if (clienteOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "Cliente no encontrado"));
            }

            Double monto = Double.parseDouble(payload.get("monto").toString());
            Long usuarioId = Long.parseLong(payload.get("usuarioId").toString());

            if (monto <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "El monto del abono debe ser mayor a 0"));
            }

            Cliente cliente = clienteOpt.get();

            if (monto > cliente.getDeudaActual()) {
                return ResponseEntity.badRequest().body(Map.of("message", "El monto del abono no puede ser mayor a la deuda actual"));
            }

            // 1. Descontar la deuda del cliente
            cliente.setDeudaActual(cliente.getDeudaActual() - monto);
            Cliente clienteActualizado = clienteRepository.save(cliente);

            // 2. ✨ REGISTRAR EL INGRESO EN LA CAJA ACTIVA
            cajaService.registrarAbonoCredito(usuarioId, BigDecimal.valueOf(monto));

            return ResponseEntity.ok(clienteActualizado);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error al procesar el abono: " + e.getMessage()));
        }
    }
}