package com.dnt.catalog.service;

import com.dnt.catalog.dto.*;

import java.util.List;

public interface DentalServiceService {

    DentalServiceResponse create(CreateDentalServiceRequest request);

    List<DentalServiceResponse> findAll();

    List<DentalServiceResponse> findActive();

    DentalServiceResponse findByPublicId(String publicId);

    DentalServiceResponse update(String publicId, UpdateDentalServiceRequest request);

    DentalServiceResponse changeStatus(String publicId, boolean active);

    void delete(String publicId);
}
