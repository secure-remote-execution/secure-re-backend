package com.eci.secureproductchallenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Secure Product Challenge - Backend (Laboratorio 3, FDSI)
 *
 * IMPORTANTE: por diseño del Laboratorio 3, esta aplicación NO tiene
 * autenticación, autorización ni HTTPS. Eso corresponde al Laboratorio 4.
 * Toda la información expuesta (dispositivos, IPs, responsables) es FICTICIA.
 */
@SpringBootApplication
public class SecureProductChallengeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureProductChallengeApplication.class, args);
    }
}
