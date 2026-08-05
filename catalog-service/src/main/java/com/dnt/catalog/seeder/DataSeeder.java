package com.dnt.catalog.seeder;

import com.dnt.catalog.model.DentalService;
import com.dnt.catalog.repository.DentalServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DentalServiceRepository repository;

    @Override
    public void run(String... args) {
        seed("Consulta general", "Revision dental completa y diagnostico", new BigDecimal("350.00"), 30);
        seed("Limpieza dental", "Limpieza general y eliminacion de sarro", new BigDecimal("650.00"), 45);
        seed("Extraccion dental", "Extraccion de piezas dentales danadas", new BigDecimal("850.00"), 60);
        seed("Blanqueamiento dental", "Blanqueamiento profesional con laser", new BigDecimal("1200.00"), 90);
        seed("Aplicacion de resina", "Resina compuesta para caries y restauraciones", new BigDecimal("500.00"), 45);
    }

    private void seed(String name, String description, BigDecimal price, Integer durationMinutes) {
        if (!repository.existsByNameIgnoreCase(name)) {
            DentalService service = DentalService.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .durationMinutes(durationMinutes)
                    .build();
            repository.save(service);
        }
    }
}
