package com.dnt.catalog.controller;

import com.dnt.catalog.dto.*;
import com.dnt.catalog.service.DentalServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/services")
@RequiredArgsConstructor
public class DentalServiceController {

    private final DentalServiceService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<DentalServiceResponse> create(
            @Valid @RequestBody CreateDentalServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<DentalServiceResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<DentalServiceResponse>> findActive() {
        return ResponseEntity.ok(service.findActive());
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<DentalServiceResponse> findByPublicId(@PathVariable String publicId) {
        return ResponseEntity.ok(service.findByPublicId(publicId));
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<DentalServiceResponse> update(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateDentalServiceRequest request) {
        return ResponseEntity.ok(service.update(publicId, request));
    }

    @PatchMapping("/{publicId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<DentalServiceResponse> changeStatus(
            @PathVariable String publicId,
            @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(publicId, request.active()));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String publicId) {
        service.delete(publicId);
        return ResponseEntity.noContent().build();
    }
}
