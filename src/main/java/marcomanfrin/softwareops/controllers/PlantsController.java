package marcomanfrin.softwareops.controllers;

import jakarta.validation.Valid;
import marcomanfrin.softwareops.DTO.plants.CreatePlantRequest;
import marcomanfrin.softwareops.DTO.plants.PlantResponse;
import marcomanfrin.softwareops.DTO.plants.UpdatePlantRequest;
import marcomanfrin.softwareops.DTO.works.WorkResponseDTO;
import marcomanfrin.softwareops.services.IPlantService;
import marcomanfrin.softwareops.services.IWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/plants")
public class PlantsController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private IPlantService plantService;

    @Autowired
    private IWorkService workService;

    @PostMapping
    public ResponseEntity<PlantResponse> createPlant(@Valid @RequestBody CreatePlantRequest request) {
        PlantResponse created = plantService.createPlant(request);
        return ResponseEntity
                .created(URI.create("/plants/" + created.id()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<Page<PlantResponse>> getAllPlants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {
        PageRequest pageRequest = buildPageRequest(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(plantService.getAllPlants(pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantResponse> getPlantById(@PathVariable UUID id) {
        return ResponseEntity.ok(plantService.getPlantById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PlantResponse> updatePlant(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlantRequest request
    ) {
        return ResponseEntity.ok(plantService.updatePlant(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deletePlant(@PathVariable UUID id) {
        plantService.deletePlant(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/invoice")
    @PreAuthorize("@securityService.isAdministrative(authentication)")
    public ResponseEntity<PlantResponse> invoicePlant(@PathVariable UUID id) {
        return ResponseEntity.ok(plantService.invoicePlant(id));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<Page<PlantResponse>> getPlantsByClient(
            @PathVariable UUID clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {
        PageRequest pageRequest = buildPageRequest(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(plantService.getPlantsByClient(clientId, pageRequest));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PlantResponse>> searchPlants(
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {
        PageRequest pageRequest = buildPageRequest(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(plantService.searchPlants(query, pageRequest));
    }

    @PostMapping("/{id}/works")
    @PreAuthorize("@securityService.isTechnician(authentication)")
    public ResponseEntity<WorkResponseDTO> createWorkFromPlant(@PathVariable UUID id) {
        WorkResponseDTO created = workService.createWorkFromPlant(id);
        return ResponseEntity
                .created(URI.create("/works/" + created.id()))
                .body(created);
    }

    private PageRequest buildPageRequest(int page, int size, Sort sort) {
        int safePage = Math.max(0, page);
        int safeSize = (size < 1 || size > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : size;
        return PageRequest.of(safePage, safeSize, sort);
    }
}
