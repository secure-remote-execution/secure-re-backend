package com.eci.secureproductchallenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI secureProductChallengeOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Secure Product Challenge - API")
                .description("Inventario de dispositivos y simulación de scripts. "
                        + "Laboratorio 3 FDSI: sin autenticación, datos ficticios.")
                .version("v0.1 (Lab 3)"));
    }
}
