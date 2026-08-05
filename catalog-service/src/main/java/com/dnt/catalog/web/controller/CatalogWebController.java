package com.dnt.catalog.web.controller;

import com.dnt.catalog.dto.CreateDentalServiceRequest;
import com.dnt.catalog.dto.DentalServiceResponse;
import com.dnt.catalog.dto.UpdateDentalServiceRequest;
import com.dnt.catalog.service.DentalServiceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/catalog/services")
@RequiredArgsConstructor
public class CatalogWebController {

    private final DentalServiceService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String listServices(Model model, HttpServletRequest request) {
        List<DentalServiceResponse> services = service.findAll();
        model.addAttribute("services", services);
        addUserInfo(model, request);
        return "catalog-services";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String showCreateForm(Model model, HttpServletRequest request) {
        model.addAttribute("request", new CreateDentalServiceRequest());
        model.addAttribute("publicId", null);
        addUserInfo(model, request);
        return "catalog-service-form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String createService(@Valid @ModelAttribute("request") CreateDentalServiceRequest request,
                                BindingResult result, Model model, HttpServletRequest req) {
        if (result.hasErrors()) {
            model.addAttribute("publicId", null);
            addUserInfo(model, req);
            return "catalog-service-form";
        }
        try {
            service.create(request);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("publicId", null);
            addUserInfo(model, req);
            return "catalog-service-form";
        }
        return "redirect:/web/catalog/services";
    }

    @GetMapping("/{publicId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String showEditForm(@PathVariable String publicId, Model model, HttpServletRequest request) {
        DentalServiceResponse svc = service.findByPublicId(publicId);
        CreateDentalServiceRequest form = CreateDentalServiceRequest.builder()
                .name(svc.getName())
                .description(svc.getDescription())
                .price(svc.getPrice())
                .durationMinutes(svc.getDurationMinutes())
                .build();
        model.addAttribute("request", form);
        model.addAttribute("publicId", publicId);
        addUserInfo(model, request);
        return "catalog-service-form";
    }

    @PostMapping("/{publicId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String updateService(@PathVariable String publicId,
                                @Valid @ModelAttribute("request") UpdateDentalServiceRequest request,
                                BindingResult result, Model model, HttpServletRequest req) {
        if (result.hasErrors()) {
            model.addAttribute("publicId", publicId);
            addUserInfo(model, req);
            return "catalog-service-form";
        }
        try {
            service.update(publicId, request);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("publicId", publicId);
            addUserInfo(model, req);
            return "catalog-service-form";
        }
        return "redirect:/web/catalog/services";
    }

    @GetMapping("/{publicId}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String toggleService(@PathVariable String publicId) {
        DentalServiceResponse svc = service.findByPublicId(publicId);
        service.changeStatus(publicId, !svc.getActive());
        return "redirect:/web/catalog/services";
    }

    private void addUserInfo(Model model, HttpServletRequest request) {
        String token = extractToken(request);
        model.addAttribute("token", token != null ? token : "");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            model.addAttribute("userRoles", roles);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            return tokenParam;
        }
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
