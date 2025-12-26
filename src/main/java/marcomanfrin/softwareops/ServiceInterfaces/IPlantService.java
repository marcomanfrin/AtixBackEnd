package marcomanfrin.softwareops.ServiceInterfaces;

import marcomanfrin.softwareops.DTO.plants.PlantRequest;
import marcomanfrin.softwareops.DTO.plants.PlantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IPlantService {
    PlantResponse createPlant(PlantRequest request);
    Page<PlantResponse> getAllPlants(Pageable pageable);
    PlantResponse getPlantById(UUID id);
    PlantResponse updatePlant(UUID id, PlantRequest request);
    void deletePlant(UUID id);
}
