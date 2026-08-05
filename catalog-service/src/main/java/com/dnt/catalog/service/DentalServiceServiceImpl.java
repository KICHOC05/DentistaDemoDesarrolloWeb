package com.dnt.catalog.service;

import com.dnt.catalog.dto.*;
import com.dnt.catalog.exception.BusinessRuleException;
import com.dnt.catalog.exception.ResourceNotFoundException;
import com.dnt.catalog.model.DentalService;
import com.dnt.catalog.repository.DentalServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DentalServiceServiceImpl implements DentalServiceService {

    private final DentalServiceRepository repository;

    @Override
    @Transactional
    public DentalServiceResponse create(CreateDentalServiceRequest request) {
        if (repository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessRuleException("Ya existe un servicio dental con el nombre: " + request.getName());
        }

        DentalService entity = DentalService.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .price(request.getPrice())
                .durationMinutes(request.getDurationMinutes())
                .build();

        DentalService saved = repository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentalServiceResponse> findAll() {
        return repository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentalServiceResponse> findActive() {
        return repository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DentalServiceResponse findByPublicId(String publicId) {
        DentalService entity = repository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio dental", "publicId", publicId));
        return toResponse(entity);
    }

    @Override
    @Transactional
    public DentalServiceResponse update(String publicId, UpdateDentalServiceRequest request) {
        DentalService entity = repository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio dental", "publicId", publicId));

        if (request.getName() != null && !request.getName().isBlank()) {
            String newName = request.getName().trim();
            if (repository.existsByNameIgnoreCaseAndPublicIdNot(newName, publicId)) {
                throw new BusinessRuleException("Ya existe un servicio dental con el nombre: " + newName);
            }
            entity.setName(newName);
        }

        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription().trim());
        }

        if (request.getPrice() != null) {
            entity.setPrice(request.getPrice());
        }

        if (request.getDurationMinutes() != null) {
            entity.setDurationMinutes(request.getDurationMinutes());
        }

        DentalService saved = repository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DentalServiceResponse changeStatus(String publicId, boolean active) {
        DentalService entity = repository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio dental", "publicId", publicId));
        entity.setActive(active);
        DentalService saved = repository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(String publicId) {
        if (!repository.findByPublicId(publicId).isPresent()) {
            throw new ResourceNotFoundException("Servicio dental", "publicId", publicId);
        }
        repository.findByPublicId(publicId).ifPresent(repository::delete);
    }

    private DentalServiceResponse toResponse(DentalService entity) {
        return DentalServiceResponse.builder()
                .publicId(entity.getPublicId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .durationMinutes(entity.getDurationMinutes())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
