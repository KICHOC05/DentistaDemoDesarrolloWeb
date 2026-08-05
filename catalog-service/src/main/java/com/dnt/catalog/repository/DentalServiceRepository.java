package com.dnt.catalog.repository;

import com.dnt.catalog.model.DentalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DentalServiceRepository extends JpaRepository<DentalService, Long> {

    Optional<DentalService> findByPublicId(String publicId);

    List<DentalService> findAllByOrderByNameAsc();

    List<DentalService> findByActiveTrueOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndPublicIdNot(String name, String publicId);
}
