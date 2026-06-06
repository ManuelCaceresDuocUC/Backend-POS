package com.posbarlacteo.PosBarLacteo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") 
                .allowedOrigins("https://sistema.kuyval.cl") // <-- Tu frontend exacto (Sin barra al final)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") 
                .allowedHeaders("*")
                .allowCredentials(true); // <-- Cambiado a true para permitir el flujo de login
    }
}