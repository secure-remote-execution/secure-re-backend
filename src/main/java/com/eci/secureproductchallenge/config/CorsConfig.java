package com.eci.secureproductchallenge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS para el origen del frontend en desarrollo local (Vite) y, opcionalmente,
 * el/los origen(es) reales donde corre Nginx en el servidor del laboratorio
 * (ej. https://<IP-del-servidor>), vía CORS_ALLOWED_ORIGINS (lista separada por comas).
 * Si Nginx sirve frontend y backend bajo el mismo origen, el navegador ni siquiera
 * aplica CORS ahí — esto solo importa cuando el origen del navegador difiere del backend.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsConfig(@Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins.split(",");
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
