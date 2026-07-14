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
import com.posbarlacteo.PosBarLacteo.model.Empresa;
import com.posbarlacteo.PosBarLacteo.model.Producto;
import com.posbarlacteo.PosBarLacteo.model.Receta;
import com.posbarlacteo.PosBarLacteo.repository.EmpresaRepository;
import com.posbarlacteo.PosBarLacteo.repository.ProductoRepository;
import com.posbarlacteo.PosBarLacteo.repository.RecetaRepository;

import jakarta.transaction.Transactional;

@CrossOrigin(origins = {
    "http://posbarlacteo-manuel-2026.s3-website-us-east-1.amazonaws.com", // Producción AWS
    "http://localhost:5173",
    "http://34.203.91.138",
    "https://ordpos.duckdns.org",                                       // PC Local
    "http://192.168.100.85:5173"                                        // Tu Celular
})
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private RecetaRepository recetaRepository;

    // ✨ CORRECCIÓN 1: Inyectamos el EmpresaRepository que faltaba y causaba el error de compilación
    @Autowired
    private EmpresaRepository empresaRepository;

    @GetMapping
    public Page<Producto> obtenerTodos(
            @RequestParam(required = false) Long categoriaId, 
            @RequestParam(defaultValue = "1") Long empresaId, 
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "20") int size) {
        
        if (categoriaId != null) {
            return productoRepository.findByActivoTrueAndCategoriaIdAndEmpresaId(categoriaId, empresaId, PageRequest.of(page, size));
        }
        
        return productoRepository.findByActivoTrueAndEmpresaId(empresaId, PageRequest.of(page, size));
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
    @Transactional 
    public Producto descontarStock(@PathVariable Long id, @RequestParam Double cantidad) {
        return productoRepository.findById(id)
            .map(producto -> {
                List<Receta> ingredientes = recetaRepository.findByProductoPrincipalId(id);

                if (ingredientes.isEmpty()) {
                    if (producto.getStock() < cantidad) {
                        throw new RuntimeException("Stock insuficiente");
                    }
                    producto.setStock(producto.getStock() - cantidad);
                } else {
                    for (Receta item : ingredientes) {
                        Producto insumo = item.getInsumo();
                        Double cantidadAGastar = item.getCantidadUsada() * cantidad;

                        if (insumo.getStock() < cantidadAGastar) {
                            throw new RuntimeException("No hay suficiente " + insumo.getDescripcion());
                        }
                        
                        insumo.setStock(insumo.getStock() - cantidadAGastar);
                        productoRepository.save(insumo); 
                    }
                }
                
                return productoRepository.save(producto);
            })
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    @PatchMapping("/{id}")
    public Producto editarParcial(@PathVariable Long id, @RequestBody Producto productoActualizado) {
        return productoRepository.findById(id)
            .map(producto -> {
                if (productoActualizado.getDescripcion() != null) producto.setDescripcion(productoActualizado.getDescripcion());
                if (productoActualizado.getPrecio() != null) producto.setPrecio(productoActualizado.getPrecio());
                if (productoActualizado.getStock() != null) producto.setStock(productoActualizado.getStock());
                if (productoActualizado.getStockCritico() != null) producto.setStockCritico(productoActualizado.getStockCritico());
                
                return productoRepository.save(producto);
            })
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    // ✨ CORRECCIÓN 2: Asignación de empresa al guardar un producto simple o insumo para evitar error 500
    @PostMapping
    public Producto guardar(
            @RequestBody Producto producto,
            @RequestParam(defaultValue = "1") Long empresaId
    ) {
        // Asignamos la empresa si el objeto viene sin ella desde el frontend
        if (producto.getEmpresa() == null) {
            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
            producto.setEmpresa(empresa);
        }

        // Validación de código de barras por empresa
        if (producto.getCodigoBarras() != null && !producto.getCodigoBarras().isEmpty()) {
            Optional<Producto> existente = productoRepository.findByCodigoBarrasAndEmpresaId(producto.getCodigoBarras(), empresaId);
            if (existente.isPresent()) {
                throw new RuntimeException("Ya existe un producto con el código: " + producto.getCodigoBarras());
            }
        }
        return productoRepository.save(producto);
    }

    @PostMapping("/con-receta")
    @Transactional
    public Producto crearProductoConReceta(
            @RequestBody RecetaRequestDTO request,
            @RequestParam(defaultValue = "1") Long empresaId 
    ) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        Producto pPrincipal = request.getProductoPrincipal();
        pPrincipal.setEsInsumo(false);
        pPrincipal.setStock(0.0);
        pPrincipal.setEmpresa(empresa); 
        
        Producto nuevoProducto = productoRepository.save(pPrincipal);

        if (request.getIngredientes() != null && !request.getIngredientes().isEmpty()) {
            for (var item : request.getIngredientes()) {
                Receta vinculo = new Receta();
                vinculo.setProductoPrincipal(nuevoProducto); 
                Producto insumo = productoRepository.findById(item.getInsumoId())
                    .orElseThrow(() -> new RuntimeException("Insumo no existe"));
                vinculo.setInsumo(insumo);
                vinculo.setCantidadUsada(item.getCantidad());
                recetaRepository.save(vinculo);
            }
        }
        return nuevoProducto;
    }
}