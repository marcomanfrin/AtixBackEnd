package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.DTO.plants.PlantRequest;
import marcomanfrin.atixbackend.DTO.plants.PlantResponse;
import marcomanfrin.atixbackend.DTO.plants.PlantUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IPlantService {
    PlantResponse createPlant(PlantRequest request);
    Page<PlantResponse> getAllPlants(Pageable pageable, String search);
    List<PlantResponse> getAllPlantsAsList();
    PlantResponse getPlantById(UUID id);
    PlantResponse updatePlant(UUID id, PlantUpdateRequest request);
    void deletePlant(UUID id);
}
