package com.posbarlacteo.PosBarLacteo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.posbarlacteo.PosBarLacteo.model.Cliente;
import com.posbarlacteo.PosBarLacteo.model.Empresa; 
import com.posbarlacteo.PosBarLacteo.model.ItemVenta;
import com.posbarlacteo.PosBarLacteo.model.Producto;
import com.posbarlacteo.PosBarLacteo.model.Receta;
import com.posbarlacteo.PosBarLacteo.model.TurnoCaja;
import com.posbarlacteo.PosBarLacteo.model.Usuario;
import com.posbarlacteo.PosBarLacteo.model.Venta;
import com.posbarlacteo.PosBarLacteo.model.VentaDetalle;
import com.posbarlacteo.PosBarLacteo.repository.ClienteRepository;
import com.posbarlacteo.PosBarLacteo.repository.EmpresaRepository; 
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

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Transactional
    public Venta procesarVentaCompleta(List<ItemVenta> items, Double montoTotal, String metodoPago, Long usuarioId, Long empresaId, Long clienteId) {
        
        Venta venta = new Venta();
        venta.setTotal(montoTotal);
        venta.setMetodoPago(metodoPago);

        if (empresaId != null) {
            Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con el ID: " + empresaId));
            venta.setEmpresa(empresa);
        } else {
            throw new RuntimeException("El ID de la empresa es obligatorio para registrar la venta");
        }

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

            if (ingredientes.isEmpty()) {
                // 🟢 VALIDACIÓN ELIMINADA: Ya no bloquea si el stock es menor a la cantidad a vender
                /*
                if (producto.getStock() < item.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getDescripcion() + 
                                               ". Tienes " + producto.getStock() + " y quieres vender " + item.getCantidad());
                }
                */
                // Descuenta directamente (puede quedar en negativo)
                producto.setStock(producto.getStock() - item.getCantidad());
                productoRepository.save(producto);
            } else {
                for (Receta receta : ingredientes) {
                    Producto insumo = receta.getInsumo();
                    Double gastoTotal = receta.getCantidadUsada() * item.getCantidad();

                    // 🟢 VALIDACIÓN ELIMINADA: Ya no bloquea si falta insumo
                    /*
                    if (insumo.getStock() < gastoTotal) {
                        throw new RuntimeException("¡Falta insumo! No hay suficiente '" + insumo.getDescripcion() + 
                                                   "' para preparar '" + producto.getDescripcion() + "'.");
                    }
                    */
                    // Descuenta directamente el insumo (puede quedar en negativo)
                    insumo.setStock(insumo.getStock() - gastoTotal);
                    productoRepository.save(insumo);
                }
            }

            VentaDetalle detalle = new VentaDetalle();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setVenta(venta);
            detalles.add(detalle);
        }
        
        if (clienteId != null) {
            Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            venta.setCliente(cliente);
            
            if ("CREDITO".equalsIgnoreCase(metodoPago)) {
                cliente.setDeudaActual(cliente.getDeudaActual() + montoTotal);
                clienteRepository.save(cliente); 
            }
        } else if ("CREDITO".equalsIgnoreCase(metodoPago)) {
            throw new RuntimeException("Para vender con crédito, debe seleccionar un cliente.");
        }
        
        venta.setDetalles(detalles);
        ventaRepository.save(venta);

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
            
        } else if ("CREDITO".equalsIgnoreCase(metodoPago)) {
            // ✨ NUEVO: Suma las ventas a crédito al turno
            BigDecimal creditoActual = turnoActivo.getVentasCredito() != null 
                                       ? turnoActivo.getVentasCredito() 
                                       : BigDecimal.ZERO;
            turnoActivo.setVentasCredito(creditoActual.add(montoEnBigDecimal));
        }

        turnoCajaRepository.save(turnoActivo);
        
        return venta;
    }
}