package com.posbarlacteo.PosBarLacteo.repository;
import com.posbarlacteo.PosBarLacteo.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    // Puedes agregar métodos personalizados si lo requieres, ej: buscar por RUT
    java.util.Optional<Empresa> findByRutEmpresa(String rutEmpresa);
}