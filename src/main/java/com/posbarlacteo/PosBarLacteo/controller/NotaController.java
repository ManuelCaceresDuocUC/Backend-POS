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

import com.posbarlacteo.PosBarLacteo.model.Nota;
import com.posbarlacteo.PosBarLacteo.repository.NotaRepository;

@RestController
@RequestMapping("/api/notas")
@CrossOrigin(origins = "*") // Ajusta tus orígenes como en tus otros controladores
public class NotaController {

    @Autowired
    private NotaRepository notaRepository;

    @GetMapping
    public List<Nota> obtenerNotas() {
        return notaRepository.findAllByOrderByFechaHoraDesc();
    }

    @PostMapping
    public Nota crearNota(@RequestBody Nota nota) {
        return notaRepository.save(nota);
    }

    @DeleteMapping("/{id}")
    public void eliminarNota(@PathVariable Long id) {
        notaRepository.deleteById(id);
    }
}