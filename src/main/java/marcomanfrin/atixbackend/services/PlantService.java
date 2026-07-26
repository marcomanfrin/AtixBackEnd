package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.plants.PlantRequest;
import marcomanfrin.atixbackend.DTO.plants.PlantResponse;
import marcomanfrin.atixbackend.DTO.plants.PlantUpdateRequest;
import marcomanfrin.atixbackend.ServiceInterfaces.IPlantService;
import marcomanfrin.atixbackend.entities.Plant;
import marcomanfrin.atixbackend.exceptions.NotFoundException;
import marcomanfrin.atixbackend.repositories.PlantRepository;
import marcomanfrin.atixbackend.repositories.WorkRepository;
import marcomanfrin.atixbackend.specifications.PlantSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlantService implements IPlantService {
    private final PlantRepository plantRepository;
    private final WorkRepository workRepository;

    public PlantService(PlantRepository plantRepository, WorkRepository workRepository) {
        this.plantRepository = plantRepository;
        this.workRepository = workRepository;
    }

    @Override
    @Transactional
    public PlantResponse createPlant(PlantRequest request) {
        Plant plant = new Plant(
                request.name(),
                request.notes(),
                request.nasDirectory(),
                request.pswPhrase(),
                request.pswPlatform(),
                request.pswStation()
        );
        Plant savedPlant = plantRepository.save(plant);
        return toPlantResponse(savedPlant);
    }

    @Override
    public Page<PlantResponse> getAllPlants(Pageable pageable, String search) {
        Specification<Plant> spec = Specification.where(PlantSpecification.searchByKeyword(search));
        return plantRepository.findAll(spec, pageable)
                .map(this::toPlantResponse);
    }

    @Override
    public List<PlantResponse> getAllPlantsAsList() {
        return plantRepository.findAll().stream()
                .map(this::toPlantResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PlantResponse getPlantById(UUID id) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plant not found with id: " + id));
        return toPlantResponse(plant);
    }

    @Override
    @Transactional
    public PlantResponse updatePlant(UUID id, PlantUpdateRequest request) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plant not found with id: " + id));

        // PATCH logic: update only non-null fields
        if (request.name() != null) {
            plant.setName(request.name());
        }
        if (request.notes() != null) {
            plant.setNotes(request.notes());
        }
        if (request.nasDirectory() != null) {
            plant.setNasDirectory(request.nasDirectory());
        }
        if (request.pswPhrase() != null) {
            plant.setPswPhrase(request.pswPhrase());
        }
        if (request.pswPlatform() != null) {
            plant.setPswPlatform(request.pswPlatform());
        }
        if (request.pswStation() != null) {
            plant.setPswStation(request.pswStation());
        }

        Plant updatedPlant = plantRepository.save(plant);
        return toPlantResponse(updatedPlant);
    }

    @Override
    @Transactional
    public void deletePlant(UUID id) {
        if (!plantRepository.existsById(id)) {
            throw new IllegalArgumentException("Plant not found with id: " + id);
        }

        // Clear plant references from Works
        workRepository.findAll().forEach(work -> {
            if (work.getPlant() != null && work.getPlant().getId().equals(id)) {
                work.setPlant(null);
                workRepository.save(work);
            }
        });

        plantRepository.deleteById(id);
    }

    private PlantResponse toPlantResponse(Plant plant) {
        return new PlantResponse(
                plant.getId(),
                plant.getName(),
                plant.getNotes(),
                plant.getNasDirectory(),
                plant.getPswPhrase(),
                plant.getPswPlatform(),
                plant.getPswStation(),
                plant.getCreatedAt()
        );
    }
}
