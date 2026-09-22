package com.eci.secureproductchallenge.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Loguea cada request (timestamp, método, ruta, IP origen, status) para que
 * el Blue Team pueda correlacionar esta evidencia con access.log de Nginx
 * durante la Fase D del Laboratorio 3.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("APP_ACCESS");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Instant start = Instant.now();
        filterChain.doFilter(request, response);

        log.info("ts={} method={} path={} status={} ip={} userAgent={}",
                start,
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));
    }
}
