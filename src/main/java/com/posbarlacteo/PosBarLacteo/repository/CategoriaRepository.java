package com.posbarlacteo.PosBarLacteo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.posbarlacteo.PosBarLacteo.model.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    
    // Método para traer solo las categorías que no han sido "eliminadas"
    List<Categoria> findByActivoTrue();
}