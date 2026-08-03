package com.dnt.clinical.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Evita que el navegador (o su boton "atras") sirva una version cacheada de
 * una pagina protegida -por ejemplo, un redirect a /web/login capturado ANTES
 * de iniciar sesion-. Sin esto, es posible ver un "regreso al login" fantasma
 * despues de haber iniciado sesion correctamente.
 *
 * Se registra con la maxima precedencia para envolver TAMBIEN las respuestas
 * de redirect generadas por Spring Security.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NoCacheHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/web/")) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }
        filterChain.doFilter(request, response);
    }
}
