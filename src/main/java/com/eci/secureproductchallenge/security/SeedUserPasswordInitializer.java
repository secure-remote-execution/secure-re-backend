package com.eci.secureproductchallenge.security;

import com.eci.secureproductchallenge.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * La migración V2 siembra admin/viewer con hashes de contraseñas que quedaron
 * versionadas en el repo (F-01). V2 no se puede editar sin romper el checksum de
 * Flyway, así que al arrancar se reemplazan esos hashes por los de las
 * contraseñas que llegan por variable de entorno: los hashes de V2 quedan
 * correspondiendo a contraseñas retiradas.
 */
@Component
public class SeedUserPasswordInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedUserPasswordInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> seedPasswords;

    public SeedUserPasswordInitializer(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder,
                                       @Value("${app.seed.admin-password}") String adminPassword,
                                       @Value("${app.seed.viewer-password}") String viewerPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedPasswords = Map.of("admin", requireNonBlank("LAB_ADMIN_PASSWORD", adminPassword),
                                    "viewer", requireNonBlank("LAB_VIEWER_PASSWORD", viewerPassword));
    }

    @Override
    public void run(ApplicationArguments args) {
        seedPasswords.forEach((username, password) ->
                userRepository.findByUsername(username).ifPresent(user -> {
                    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                        user.setPasswordHash(passwordEncoder.encode(password));
                        userRepository.save(user);
                        log.info("Contraseña del usuario semilla '{}' rotada desde variable de entorno", username);
                    }
                }));
    }

    private static String requireNonBlank(String envName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(envName + " es obligatoria y no puede estar vacía");
        }
        return value;
    }
}
