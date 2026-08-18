package com.posbarlacteo.PosBarLacteo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.posbarlacteo.PosBarLacteo.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByRut(String rut);
}