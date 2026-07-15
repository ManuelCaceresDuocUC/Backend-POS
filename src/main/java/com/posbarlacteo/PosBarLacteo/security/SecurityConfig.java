package com.posbarlacteo.PosBarLacteo.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. Endpoints públicos de acceso y registro (✨ SOLUCIÓN)
                .requestMatchers(
                    "/api/usuarios/login",
                    "/api/auth/registrar-empresa", // Agrega la ruta exacta de tu backend
                    "/auth/registrar-empresa",     // Por si no usa el prefijo /api
                    "/api/auth/**"                 // O puedes permitir todo el controlador de auth
                ).permitAll() 
                
                // 2. Rutas EXCLUSIVAS para Administradores
                .requestMatchers(
                    "/api/caja/historial", 
                    "/api/admin/**", 
                    "/api/reportes/**", 
                    "/api/inventario/**"
                ).hasRole("ADMIN")
                
                // 3. Operativa del POS
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://posbarlacteo-manuel-2026.s3-website-us-east-1.amazonaws.com",
            "http://localhost:5173",
            "http://34.203.91.138",
            "https://ordpos.duckdns.org",
            "http://192.168.100.85:5173"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}