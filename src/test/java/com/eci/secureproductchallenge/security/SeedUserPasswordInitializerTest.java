package com.eci.secureproductchallenge.security;

import com.eci.secureproductchallenge.model.Role;
import com.eci.secureproductchallenge.model.User;
import com.eci.secureproductchallenge.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SeedUserPasswordInitializerTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final UserRepository repository = mock(UserRepository.class);

    @Test
    void reemplazaElHashSembradoPorV2PorElDeLaVariableDeEntorno() {
        User admin = user("admin", encoder.encode("contraseña-versionada-en-V2"));
        User viewer = user("viewer", encoder.encode("otra-versionada-en-V2"));
        when(repository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(repository.findByUsername("viewer")).thenReturn(Optional.of(viewer));

        new SeedUserPasswordInitializer(repository, encoder, "admin-nueva", "viewer-nueva").run(null);

        assertThat(encoder.matches("admin-nueva", admin.getPasswordHash())).isTrue();
        assertThat(encoder.matches("contraseña-versionada-en-V2", admin.getPasswordHash())).isFalse();
        assertThat(encoder.matches("viewer-nueva", viewer.getPasswordHash())).isTrue();
        verify(repository, times(2)).save(any(User.class));
    }

    @Test
    void noReescribeSiLaContraseñaYaCoincide() {
        User admin = user("admin", encoder.encode("admin-nueva"));
        when(repository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(repository.findByUsername("viewer")).thenReturn(Optional.empty());

        new SeedUserPasswordInitializer(repository, encoder, "admin-nueva", "viewer-nueva").run(null);

        verify(repository, never()).save(any(User.class));
    }

    @Test
    void fallaSiLaContraseñaLlegaVacia() {
        assertThatThrownBy(() -> new SeedUserPasswordInitializer(repository, encoder, " ", "viewer-nueva"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("LAB_ADMIN_PASSWORD");
    }

    private static User user(String username, String hash) {
        return User.builder().username(username).passwordHash(hash)
                .role(Role.ADMIN).enabled(true).createdAt(Instant.now()).build();
    }
}
