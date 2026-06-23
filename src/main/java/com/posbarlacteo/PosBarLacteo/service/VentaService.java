package com.posbarlacteo.PosBarLacteo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.posbarlacteo.PosBarLacteo.model.ItemVenta;
import com.posbarlacteo.PosBarLacteo.model.Producto;
import com.posbarlacteo.PosBarLacteo.model.Receta;
import com.posbarlacteo.PosBarLacteo.model.TurnoCaja;
import com.posbarlacteo.PosBarLacteo.model.Usuario;
import com.posbarlacteo.PosBarLacteo.model.Venta;
import com.posbarlacteo.PosBarLacteo.model.VentaDetalle; // ✨ NUEVO IMPORT
import com.posbarlacteo.PosBarLacteo.repository.ProductoRepository;
import com.posbarlacteo.PosBarLacteo.repository.RecetaRepository;
import com.posbarlacteo.PosBarLacteo.repository.TurnoCajaRepository;
import com.posbarlacteo.PosBarLacteo.repository.UsuarioRepository;
import com.posbarlacteo.PosBarLacteo.repository.VentaRepository; // ✨ NUEVO IMPORT

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

    // ✨ NUEVO: Inyectamos el repositorio de usuarios
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    // ✨ ACTUALIZADO: Agregamos el 4to parámetro (Long usuarioId)
    public void procesarVentaCompleta(List<ItemVenta> items, Double montoTotal, String metodoPago, Long usuarioId) {
        
        Venta venta = new Venta();
        venta.setTotal(montoTotal);
        venta.setMetodoPago(metodoPago);

        // ✨ NUEVO: Asignar el usuario real a la venta
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

            // ✨ 1. VALIDACIÓN DEL PRODUCTO PRINCIPAL
            // Si el producto no tiene receta (es un producto directo como una bebida)
            // o si también controlas el stock del producto principal preparado:
            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getDescripcion() + 
                                           ". Tienes " + producto.getStock() + " y quieres vender " + item.getCantidad());
            }

            // Descontamos stock del producto principal
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            // ✨ 2. VALIDACIÓN DE INSUMOS (RECETA)
            if (!ingredientes.isEmpty()) {
                for (Receta receta : ingredientes) {
                    Producto insumo = receta.getInsumo();
                    Double gastoTotal = receta.getCantidadUsada() * item.getCantidad();

                    // Comprobamos si hay suficiente insumo para preparar la cantidad solicitada
                    if (insumo.getStock() < gastoTotal) {
                        throw new RuntimeException("¡Falta insumo! No hay suficiente '" + insumo.getDescripcion() + 
                                                   "' para preparar '" + producto.getDescripcion() + "'. " +
                                                   "Necesitas " + gastoTotal + " pero solo quedan " + insumo.getStock());
                    }

                    // Si pasa la validación, descontamos el insumo
                    insumo.setStock(insumo.getStock() - gastoTotal);
                    productoRepository.save(insumo);
                }
            }

            // Crear el registro de detalle
            VentaDetalle detalle = new VentaDetalle();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalles.add(detalle);
        }

        venta.setDetalles(detalles);
        ventaRepository.save(venta);

        // ✨ ACTUALIZADO: Usamos el usuarioId real en vez del 1L estático
        TurnoCaja turnoActivo = turnoCajaRepository.findByCajeroIdAndEstado(usuarioId, "ABIERTA")
            .orElseThrow(() -> new RuntimeException("No se puede procesar la venta: No hay un turno de caja abierto para este cajero."));

        // Convertimos el Double a BigDecimal porque así lo definiste en el modelo TurnoCaja
        BigDecimal montoEnBigDecimal = BigDecimal.valueOf(montoTotal);

        if ("EFECTIVO".equalsIgnoreCase(metodoPago)) {
            // Validamos que no sea null. Si es null, asumimos que es 0.
            BigDecimal efectivoActual = turnoActivo.getVentasEfectivo() != null 
                                        ? turnoActivo.getVentasEfectivo() 
                                        : BigDecimal.ZERO;
                                        
            turnoActivo.setVentasEfectivo(efectivoActual.add(montoEnBigDecimal));
            
        } else if ("TARJETA".equalsIgnoreCase(metodoPago)) {
            // Validamos que no sea null. Si es null, asumimos que es 0.
            BigDecimal tarjetaActual = turnoActivo.getVentasTarjeta() != null 
                                       ? turnoActivo.getVentasTarjeta() 
                                       : BigDecimal.ZERO;
                                       
            turnoActivo.setVentasTarjeta(tarjetaActual.add(montoEnBigDecimal));
        }

        turnoCajaRepository.save(turnoActivo);
    }
}