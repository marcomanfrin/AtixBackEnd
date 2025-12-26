package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.DTO.plants.PlantRequest;
import marcomanfrin.softwareops.DTO.plants.PlantResponse;
import marcomanfrin.softwareops.ServiceInterfaces.IPlantService;
import marcomanfrin.softwareops.entities.Plant;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.repositories.PlantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PlantService implements IPlantService {
    private final PlantRepository plantRepository;

    public PlantService(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
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
    public Page<PlantResponse> getAllPlants(Pageable pageable) {
        return plantRepository.findAll(pageable)
                .map(this::toPlantResponse);
    }

    @Override
    public PlantResponse getPlantById(UUID id) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plant not found with id: " + id));
        return toPlantResponse(plant);
    }

    @Override
    @Transactional
    public PlantResponse updatePlant(UUID id, PlantRequest request) {
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
                plant.getPswStation()
        );
    }
}
