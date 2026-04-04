package com.posbarlacteo.PosBarLacteo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.posbarlacteo.PosBarLacteo.model.ItemVenta;
import com.posbarlacteo.PosBarLacteo.model.Producto;
import com.posbarlacteo.PosBarLacteo.model.Receta;
import com.posbarlacteo.PosBarLacteo.model.Venta;
import com.posbarlacteo.PosBarLacteo.model.VentaDetalle;
import com.posbarlacteo.PosBarLacteo.repository.ProductoRepository;
import com.posbarlacteo.PosBarLacteo.repository.RecetaRepository;
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

    @Transactional
    public void procesarVentaCompleta(List<ItemVenta> items, Double montoTotal, String metodoPago) {
        Venta venta = new Venta();
        venta.setTotal(montoTotal);
        venta.setMetodoPago(metodoPago);
        
        List<VentaDetalle> detalles = new ArrayList<>();

        for (ItemVenta item : items) {
            Producto producto = productoRepository.findById(item.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

            // Lógica de Stock (Simples y con Receta)
            List<Receta> ingredientes = recetaRepository.findByProductoPrincipalId(producto.getId());

            // Descontamos stock del producto principal siempre
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            // Si tiene receta, descontamos los insumos
            if (!ingredientes.isEmpty()) {
                for (Receta receta : ingredientes) {
                    Producto insumo = receta.getInsumo();
                    Double gastoTotal = receta.getCantidadUsada() * item.getCantidad();
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
    }
}