package com.dnt.appointment.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Los enlaces de navegacion entre modulos (Dashboard, Usuarios, Pacientes,
 * Doctores, Citas, Expediente Clinico, Cerrar Sesion) deben apuntar SIEMPRE
 * al gateway, nunca a una ruta relativa. Si una pagina se sirve directamente
 * desde el puerto de un microservicio (sin pasar por el gateway), una ruta
 * relativa como "/web/dashboard" se resolveria contra ESE mismo puerto, que
 * no tiene esa ruta -> error/redireccion inesperada.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @Value("${app.gateway.url}")
    private String gatewayUrl;

    @ModelAttribute("gatewayUrl")
    public String gatewayUrl() {
        return gatewayUrl;
    }
}
