package com.posbarlacteo.PosBarLacteo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.posbarlacteo.PosBarLacteo.model.Usuario;
import com.posbarlacteo.PosBarLacteo.repository.UsuarioRepository;



@RestController
@CrossOrigin(origins = {
    "http://posbarlacteo-manuel-2026.s3-website-us-east-1.amazonaws.com", // Producción AWS
    "http://localhost:5173",
    "http://34.203.91.138",
    "https://ordpos.duckdns.org",
                                                 // PC Local
    "http://192.168.100.85:5173"                                         // Tu Celular
})
@RequestMapping("/api/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<Usuario> listarUsuarios(@RequestParam(defaultValue = "1") Long empresaId) {
        return usuarioRepository.findByEmpresaId(empresaId);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario loginRequest) {
        // Buscamos al usuario por su nombre de usuario
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsuario(loginRequest.getUsuario());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Verificamos la contraseña (aquí podrías usar BCrypt más adelante)
            if (usuario.getContrasena().equals(loginRequest.getContrasena())) {
                // Si es correcto, devolvemos el usuario (sin la contraseña por seguridad si prefieres)
                usuario.setContrasena(null); 
                return ResponseEntity.ok(usuario);
            }
        }
        
        // Si no existe o la clave falla, enviamos 401 (No autorizado)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body("Usuario o contraseña incorrectos");
    }
    
}
