package com.posbarlacteo.PosBarLacteo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.posbarlacteo.PosBarLacteo.dto.RegistroEmpresaDTO;
import com.posbarlacteo.PosBarLacteo.model.Empresa;
import com.posbarlacteo.PosBarLacteo.model.Usuario;
import com.posbarlacteo.PosBarLacteo.repository.EmpresaRepository;
import com.posbarlacteo.PosBarLacteo.repository.UsuarioRepository;

@RestController
@CrossOrigin(origins = {
    "http://posbarlacteo-manuel-2026.s3-website-us-east-1.amazonaws.com",
    "http://localhost:5173",
    "http://34.203.91.138",
    "https://ordpos.duckdns.org",
    "http://192.168.100.85:5173"
})
@RequestMapping("/api/auth")
public class RegistroController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/registrar-empresa")
    @Transactional // Hace rollback de toda la operación si falla un solo guardado
    public ResponseEntity<?> registrarEmpresaCompleta(@RequestBody RegistroEmpresaDTO data) {
        try {
            // 1. Validar que la Empresa no exista ya por su RUT
            if (empresaRepository.findByRutEmpresa(data.getEmpresa().getRut_empresa()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ya existe una empresa registrada con el RUT: " + data.getEmpresa().getRut_empresa());
            }

            // 2. Validar que el usuario admin no esté ocupado en el sistema
            if (usuarioRepository.findByUsuario(data.getAdmin().getUsuario()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El nombre de usuario del administrador ya está en uso.");
            }

            // 3. Crear y guardar la Entidad Empresa
            Empresa nuevaEmpresa = new Empresa();
            nuevaEmpresa.setRutEmpresa(data.getEmpresa().getRut_empresa());
            nuevaEmpresa.setRazonSocial(data.getEmpresa().getRazon_social());
            nuevaEmpresa.setGiro(data.getEmpresa().getGiro());
            
            // Prevención de valores nulos para columnas con nullable = false
            nuevaEmpresa.setDireccion(data.getEmpresa().getDireccion() != null ? data.getEmpresa().getDireccion() : "Sin dirección");
            nuevaEmpresa.setComuna(data.getEmpresa().getComuna() != null ? data.getEmpresa().getComuna() : "Sin comuna");
            nuevaEmpresa.setActivo(true);
            
            // No seteamos fechaRegistro aquí por la restricción insertable = false de tu modelo
            nuevaEmpresa = empresaRepository.save(nuevaEmpresa);

            // 4. Crear y guardar la Cuenta Administrador
            Usuario usuarioAdmin = new Usuario();
            usuarioAdmin.setUsuario(data.getAdmin().getUsuario());
            usuarioAdmin.setContrasena(data.getAdmin().getContrasena());
            usuarioAdmin.setRol("admin");
            usuarioAdmin.setEmpresa(nuevaEmpresa);
            
            usuarioRepository.save(usuarioAdmin);

            // 5. Crear Cuentas de Empleados extras (si el usuario agregó en el form)
            if (data.getEmpleados() != null && !data.getEmpleados().isEmpty()) {
                for (RegistroEmpresaDTO.UsuarioDTO empDto : data.getEmpleados()) {
                    if (usuarioRepository.findByUsuario(empDto.getUsuario()).isPresent()) {
                        throw new RuntimeException("El usuario colaborador '" + empDto.getUsuario() + "' ya existe en el sistema.");
                    }
                    
                    Usuario empleado = new Usuario();
                    empleado.setUsuario(empDto.getUsuario());
                    empleado.setContrasena(empDto.getContrasena());
                    empleado.setRol(empDto.getRol() != null ? empDto.getRol() : "vendedor");
                    empleado.setEmpresa(nuevaEmpresa);
                    
                    usuarioRepository.save(empleado);
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Empresa y usuarios registrados exitosamente.");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno en el servidor al procesar el registro.");
        }
    }
}