package com.posbarlacteo.PosBarLacteo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posbarlacteo.PosBarLacteo.model.Receta;
import com.posbarlacteo.PosBarLacteo.repository.RecetaRepository;

@RestController
@RequestMapping("/api/recetas")
@CrossOrigin(origins = "http://localhost:5173") 
public class RecetaController {

    @Autowired
    private RecetaRepository recetaRepository;

    @GetMapping
    public List<Receta> obtenerTodas() {
        return recetaRepository.findAll();
    }

    @PostMapping
    public Receta guardar(@RequestBody Receta receta) {
        return recetaRepository.save(receta);
    }
    
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        recetaRepository.deleteById(id);
    }
}