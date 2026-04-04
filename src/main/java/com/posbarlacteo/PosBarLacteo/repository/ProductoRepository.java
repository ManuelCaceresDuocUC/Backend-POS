package com.posbarlacteo.PosBarLacteo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.posbarlacteo.PosBarLacteo.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // Este método es mágico: Spring genera la consulta SQL 
    // "SELECT * FROM pos_productos WHERE codigo_barras = ?" automáticamente.
    Optional<Producto> findByCodigoBarras(String codigoBarras);
}