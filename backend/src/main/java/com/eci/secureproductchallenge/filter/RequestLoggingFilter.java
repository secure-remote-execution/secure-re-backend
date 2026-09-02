package com.eci.secureproductchallenge.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Middleware de logging: deja constancia (timestamp, método, ruta, IP origen,
 * status, duración) de cada request para que el Blue Team pueda correlacionar
 * esta evidencia con el access.log de Nginx. No implementa autenticación ni
 * autorización (eso es Lab 4).
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("com.eci.secureproductchallenge.access");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Instant start = Instant.now();
        String clientIp = resolveClientIp(request);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = Instant.now().toEpochMilli() - start.toEpochMilli();
            log.info("timestamp={} method={} path={} query={} ip={} status={} durationMs={}",
                    start,
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getQueryString(),
                    clientIp,
                    response.getStatus(),
                    durationMs);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
