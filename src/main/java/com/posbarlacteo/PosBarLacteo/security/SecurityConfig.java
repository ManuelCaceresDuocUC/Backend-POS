package com.posbarlacteo.PosBarLacteo.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // ✨ Importante para permitir OPTIONS
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
                // ✨ 1. PERMITIR PREFLIGHT (OPTIONS) DE CORS SIEMPRE
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ✨ 2. ENDPOINTS PÚBLICOS (Agrega todas las variantes posibles de tu ruta)
                .requestMatchers(
                    "/api/usuarios/login",
                    "/api/auth/**",
                    "/auth/**"
                ).permitAll() 
                
                // 3. Rutas de Administración
                .requestMatchers(
                    "/api/caja/historial", 
                    "/api/admin/**", 
                    "/api/reportes/**", 
                    "/api/inventario/**"
                ).hasRole("ADMIN")
                
                // 4. Operativa general del POS
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // ✨ 5. CAMBIO CLAVE: Usar OriginPatterns con comodines (*) 
        // Permite cualquier puerto en localhost, 127.0.0.1 y tu red local sin romper allowCredentials=true
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://192.168.*:*",
            "http://posbarlacteo-manuel-2026.s3-website-us-east-1.amazonaws.com",
            "http://34.203.91.138",
            "https://ordpos.duckdns.org"
        ));
        
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}