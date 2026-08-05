package com.dnt.catalog.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @Value("${app.gateway.url}")
    private String gatewayUrl;

    @ModelAttribute("gatewayUrl")
    public String gatewayUrl() {
        return gatewayUrl;
    }
}
