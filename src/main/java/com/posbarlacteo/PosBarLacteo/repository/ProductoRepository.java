package com.posbarlacteo.PosBarLacteo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.posbarlacteo.PosBarLacteo.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // Este método es mágico: Spring genera la consulta SQL 
    // "SELECT * FROM pos_productos WHERE codigo_barras = ?" automáticamente.
    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.receta r LEFT JOIN FETCH r.insumo WHERE p.activo = true")
    Page<Producto> findByActivoTrue(Pageable pageable);
    Optional<Producto> findByCodigoBarras(String codigoBarras);
    Page<Producto> findByActivoTrueAndCategoriaId(Long categoriaId, Pageable pageable);
    @Query("SELECT COALESCE(SUM(p.stock * p.precio), 0.0) FROM Producto p WHERE p.activo = true")
    Double calcularValorTotalInventario();
}