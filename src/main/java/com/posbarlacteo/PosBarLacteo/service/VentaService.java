package com.posbarlacteo.PosBarLacteo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.posbarlacteo.PosBarLacteo.model.Empresa; // ✨ NUEVO IMPORT
import com.posbarlacteo.PosBarLacteo.model.ItemVenta;
import com.posbarlacteo.PosBarLacteo.model.Producto;
import com.posbarlacteo.PosBarLacteo.model.Receta;
import com.posbarlacteo.PosBarLacteo.model.TurnoCaja;
import com.posbarlacteo.PosBarLacteo.model.Usuario;
import com.posbarlacteo.PosBarLacteo.model.Venta;
import com.posbarlacteo.PosBarLacteo.model.VentaDetalle;
import com.posbarlacteo.PosBarLacteo.repository.EmpresaRepository; // ✨ NUEVO IMPORT
import com.posbarlacteo.PosBarLacteo.repository.ProductoRepository;
import com.posbarlacteo.PosBarLacteo.repository.RecetaRepository;
import com.posbarlacteo.PosBarLacteo.repository.TurnoCajaRepository;
import com.posbarlacteo.PosBarLacteo.repository.UsuarioRepository;
import com.posbarlacteo.PosBarLacteo.repository.VentaRepository;

import jakarta.transaction.Transactional;

@Service
public class VentaService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private RecetaRepository recetaRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private TurnoCajaRepository turnoCajaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ✨ NUEVO: Inyectamos el repositorio de Empresa
    @Autowired
    private EmpresaRepository empresaRepository;

    @Transactional
    // ✨ ACTUALIZADO: Agregamos el 5to parámetro (Long empresaId)
    public void procesarVentaCompleta(List<ItemVenta> items, Double montoTotal, String metodoPago, Long usuarioId, Long empresaId) {
        
        Venta venta = new Venta();
        venta.setTotal(montoTotal);
        venta.setMetodoPago(metodoPago);

        // ✨ NUEVO: Buscar y asignar la empresa a la venta
        if (empresaId != null) {
            Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con el ID: " + empresaId));
            venta.setEmpresa(empresa);
        } else {
            throw new RuntimeException("El ID de la empresa es obligatorio para registrar la venta");
        }

        // Asignar el usuario real a la venta
        if (usuarioId != null) {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario cajero no encontrado"));
            venta.setUsuario(usuario);
        } else {
            throw new RuntimeException("El ID del cajero es obligatorio para registrar la venta");
        }
        
        List<VentaDetalle> detalles = new ArrayList<>();

        for (ItemVenta item : items) {
            Producto producto = productoRepository.findById(item.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

            List<Receta> ingredientes = recetaRepository.findByProductoPrincipalId(producto.getId());

            // ✨ CORRECCIÓN AQUÍ: 
            // Solo validamos el stock directo si el producto NO tiene ingredientes.
            // Si tiene ingredientes, confiamos en la validación que ocurre más abajo.
            if (ingredientes.isEmpty()) {
                if (producto.getStock() < item.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getDescripcion() + 
                                            ". Tienes " + producto.getStock() + " y quieres vender " + item.getCantidad());
                }
                // Descontamos stock del producto normal
                producto.setStock(producto.getStock() - item.getCantidad());
                productoRepository.save(producto);
            } else {
                // Si es receta, NO descontamos el stock del producto principal (porque es virtual).
                // Solo validamos y descontamos los insumos (la lógica que ya tenías abajo).
                for (Receta receta : ingredientes) {
                    Producto insumo = receta.getInsumo();
                    Double gastoTotal = receta.getCantidadUsada() * item.getCantidad();

                    if (insumo.getStock() < gastoTotal) {
                        throw new RuntimeException("¡Falta insumo! No hay suficiente '" + insumo.getDescripcion() + 
                                                "' para preparar '" + producto.getDescripcion() + "'.");
                    }

                    insumo.setStock(insumo.getStock() - gastoTotal);
                    productoRepository.save(insumo);
                }
            }
        }

        venta.setDetalles(detalles);
        ventaRepository.save(venta);

        // Actualización de Turno de Caja
        TurnoCaja turnoActivo = turnoCajaRepository.findByCajeroIdAndEstado(usuarioId, "ABIERTA")
            .orElseThrow(() -> new RuntimeException("No se puede procesar la venta: No hay un turno de caja abierto para este cajero."));

        BigDecimal montoEnBigDecimal = BigDecimal.valueOf(montoTotal);

        if ("EFECTIVO".equalsIgnoreCase(metodoPago)) {
            BigDecimal efectivoActual = turnoActivo.getVentasEfectivo() != null 
                                        ? turnoActivo.getVentasEfectivo() 
                                        : BigDecimal.ZERO;
                                        
            turnoActivo.setVentasEfectivo(efectivoActual.add(montoEnBigDecimal));
            
        } else if ("TARJETA".equalsIgnoreCase(metodoPago)) {
            BigDecimal tarjetaActual = turnoActivo.getVentasTarjeta() != null 
                                       ? turnoActivo.getVentasTarjeta() 
                                       : BigDecimal.ZERO;
                                       
            turnoActivo.setVentasTarjeta(tarjetaActual.add(montoEnBigDecimal));
        }

        turnoCajaRepository.save(turnoActivo);
    }
}