package marcomanfrin.atixbackend.controllers;

import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.plants.PlantRequest;
import marcomanfrin.atixbackend.DTO.plants.PlantResponse;
import marcomanfrin.atixbackend.services.PlantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/plants")
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'USER')")
public class PlantsController {
    private final PlantService plantService;

    public PlantsController(PlantService plantService) {
        this.plantService = plantService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<PlantResponse> createPlant(@Valid @RequestBody PlantRequest request) {
        PlantResponse plant = plantService.createPlant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(plant);
    }

    @GetMapping
    public ResponseEntity<Page<PlantResponse>> getAllPlants(Pageable pageable) {
        Page<PlantResponse> plants = plantService.getAllPlants(pageable);
        return ResponseEntity.ok(plants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantResponse> getPlantById(@PathVariable UUID id) {
        PlantResponse plant = plantService.getPlantById(id);
        return ResponseEntity.ok(plant);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<PlantResponse> updatePlant(
            @PathVariable UUID id,
            @Valid @RequestBody PlantRequest request) {
        PlantResponse plant = plantService.updatePlant(id, request);
        return ResponseEntity.ok(plant);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlant(@PathVariable UUID id) {
        plantService.deletePlant(id);
        return ResponseEntity.noContent().build();
    }
}
