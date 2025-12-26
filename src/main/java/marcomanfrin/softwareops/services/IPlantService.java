package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.DTO.plants.CreatePlantRequest;
import marcomanfrin.softwareops.DTO.plants.PlantResponse;
import marcomanfrin.softwareops.DTO.plants.UpdatePlantRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IPlantService {
    PlantResponse createPlant(CreatePlantRequest request);
    List<PlantResponse> getAllPlants();
    Page<PlantResponse> getAllPlants(Pageable pageable);
    PlantResponse getPlantById(UUID id);
    PlantResponse updatePlant(UUID id, UpdatePlantRequest request);
    void deletePlant(UUID id);
    PlantResponse invoicePlant(UUID id);
    List<PlantResponse> getPlantsByClient(UUID clientId);
    Page<PlantResponse> getPlantsByClient(UUID clientId, Pageable pageable);
    List<PlantResponse> searchPlants(String query);
    Page<PlantResponse> searchPlants(String query, Pageable pageable);
}
