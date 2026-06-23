package com.posbarlacteo.PosBarLacteo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.posbarlacteo.PosBarLacteo.dto.RecetaRequestDTO;
import com.posbarlacteo.PosBarLacteo.model.Producto;
import com.posbarlacteo.PosBarLacteo.model.Receta;
import com.posbarlacteo.PosBarLacteo.repository.ProductoRepository;
import com.posbarlacteo.PosBarLacteo.repository.RecetaRepository;

import jakarta.transaction.Transactional;

@CrossOrigin(origins = {
    "http://posbarlacteo-manuel-2026.s3-website-us-east-1.amazonaws.com", // Producción AWS
    "http://localhost:5173",                                             // PC Local
    "http://192.168.100.85:5173"                                         // Tu Celular
})
@RestController
@RequestMapping("/api/productos")

public class ProductoController {
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private RecetaRepository recetaRepository;
    @GetMapping
    public Page<Producto> obtenerTodos(
            // ✨ NUEVO: Parámetro opcional para filtrar
            @RequestParam(required = false) Long categoriaId, 
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "20") int size) {
        
        // Si el frontend nos manda una categoría, usamos el nuevo filtro
        if (categoriaId != null) {
            return productoRepository.findByActivoTrueAndCategoriaId(categoriaId, PageRequest.of(page, size));
        }
        
        // Si no mandan categoría, devolvemos todo como antes
        return productoRepository.findByActivoTrue(PageRequest.of(page, size));
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        productoRepository.findById(id).ifPresent(p -> {
            p.setActivo(false);
            productoRepository.save(p);
        });
    }
    
    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto productoActualizado) {
        return productoRepository.findById(id)
            .map(producto -> {
                producto.setDescripcion(productoActualizado.getDescripcion());
                producto.setPrecio(productoActualizado.getPrecio());
                producto.setStock(productoActualizado.getStock());
                producto.setStockCritico(productoActualizado.getStockCritico());
                return productoRepository.save(producto);
            })
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }
    
    @PutMapping("/{id}/descontar")
    @Transactional // IMPORTANTE: Para que si algo falla, no descuente nada
    public Producto descontarStock(@PathVariable Long id, @RequestParam Double cantidad) {
        return productoRepository.findById(id)
            .map(producto -> {
                // 1. BUSCAMOS SI ESTE PRODUCTO TIENE UNA RECETA
                List<Receta> ingredientes = recetaRepository.findByProductoPrincipalId(id);

                if (ingredientes.isEmpty()) {
                    // CASO A: Es un producto simple (bebida, etc.)
                    if (producto.getStock() < cantidad) {
                        throw new RuntimeException("Stock insuficiente");
                    }
                    producto.setStock(producto.getStock() - cantidad);
                } else {
                    // CASO B: Es un producto compuesto (sándwich)
                    for (Receta item : ingredientes) {
                        Producto insumo = item.getInsumo();
                        Double cantidadAGastar = item.getCantidadUsada() * cantidad;

                        if (insumo.getStock() < cantidadAGastar) {
                            throw new RuntimeException("No hay suficiente " + insumo.getDescripcion());
                        }
                        
                        insumo.setStock(insumo.getStock() - cantidadAGastar);
                        productoRepository.save(insumo); // Descontamos la palta, el pan, etc.
                    }
                    // Nota: El "stock" del sándwich podrías dejarlo en 0 o no usarlo,
                    // ya que lo que importa es el stock de los insumos.
                }
                
                return productoRepository.save(producto);
            })
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }
    @PatchMapping("/{id}")
    public Producto editarParcial(@PathVariable Long id, @RequestBody Producto productoActualizado) {
        return productoRepository.findById(id)
            .map(producto -> {
                // Actualizamos solo los campos que vienen en el body
                if (productoActualizado.getDescripcion() != null) producto.setDescripcion(productoActualizado.getDescripcion());
                if (productoActualizado.getPrecio() != null) producto.setPrecio(productoActualizado.getPrecio());
                if (productoActualizado.getStock() != null) producto.setStock(productoActualizado.getStock());
                if (productoActualizado.getStockCritico() != null) producto.setStockCritico(productoActualizado.getStockCritico());
                
                return productoRepository.save(producto);
            })
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    @PostMapping
    public Producto guardar(@RequestBody Producto producto) {
        // Si el producto trae código de barras, verificamos que no esté duplicado
        if (producto.getCodigoBarras() != null && !producto.getCodigoBarras().isEmpty()) {
            Optional<Producto> existente = productoRepository.findByCodigoBarras(producto.getCodigoBarras());
            if (existente.isPresent()) {
                throw new RuntimeException("Ya existe un producto con el código: " + producto.getCodigoBarras());
            }
        }
        return productoRepository.save(producto);
    }
    @PostMapping("/con-receta")
    @Transactional
    public Producto crearProductoConReceta(@RequestBody RecetaRequestDTO request) {
        // 1. Guardamos el producto principal primero
        Producto pPrincipal = request.getProductoPrincipal();
        pPrincipal.setEsInsumo(false);
        pPrincipal.setStock(0.0); // Stock físico inicial es 0 para productos con receta
        
        // IMPORTANTE: 'nuevoProducto' ya tiene el ID generado por la DB
        Producto nuevoProducto = productoRepository.save(pPrincipal);

        // 2. Vinculamos los ingredientes
        if (request.getIngredientes() != null && !request.getIngredientes().isEmpty()) {
            for (var item : request.getIngredientes()) {
                Receta vinculo = new Receta();
                
                // ASIGNACIÓN CRÍTICA: Aquí conectamos el ID generado
                vinculo.setProductoPrincipal(nuevoProducto); 
                
                Producto insumo = productoRepository.findById(item.getInsumoId())
                    .orElseThrow(() -> new RuntimeException("Insumo ID " + item.getInsumoId() + " no existe"));
                
                vinculo.setInsumo(insumo);
                vinculo.setCantidadUsada(item.getCantidad());
                
                recetaRepository.save(vinculo);
            }
        }
        return nuevoProducto;
    }
}
