package com.eci.secureproductchallenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación de la API (Swagger UI en /swagger-ui.html). Se deja
 * intencionalmente pública en este laboratorio, ya que el Red Team debe
 * poder reconocerla como parte del ejercicio.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI secureProductChallengeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Secure Product Challenge API")
                        .description("Laboratorio 3 - FDSI (ECI). Inventario ficticio de dispositivos de red "
                                + "y simulación de scripts. Sin autenticación (deliberado, ver Lab 4).")
                        .version("v1.0 - Lab 3")
                        .contact(new Contact().name("Equipo FDSI - ECI")));
    }
}
