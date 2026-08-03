package com.dnt.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GatewayErrorController implements ErrorController {

    @Value("${app.gateway.url}")
    private String gatewayUrl;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String path = (String) request.getAttribute("jakarta.servlet.error.request_uri");

        if (status != null && status == 404 && path != null && isOtherServicePath(path)) {
            return "redirect:" + gatewayUrl + path;
        }
        return "error";
    }

    private boolean isOtherServicePath(String path) {
        return path.startsWith("/web/patients")
            || path.startsWith("/web/doctors")
            || path.startsWith("/web/appointments")
            || path.startsWith("/web/clinical")
            || path.startsWith("/api/patients")
            || path.startsWith("/api/doctors")
            || path.startsWith("/api/appointments")
            || path.startsWith("/api/clinical");
    }
}
