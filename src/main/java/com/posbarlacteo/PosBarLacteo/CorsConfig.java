package com.posbarlacteo.PosBarLacteo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todos los controladores (rutas)
                .allowedOriginPatterns("*") // Permite que cualquier URL (como tu S3) se conecte
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Permite todos los métodos HTTP
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}