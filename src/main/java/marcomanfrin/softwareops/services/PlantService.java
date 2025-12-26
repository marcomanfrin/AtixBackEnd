package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.DTO.plants.CreatePlantRequest;
import marcomanfrin.softwareops.DTO.plants.PlantResponse;
import marcomanfrin.softwareops.DTO.plants.UpdatePlantRequest;
import marcomanfrin.softwareops.entities.Client;
import marcomanfrin.softwareops.entities.Plant;
import marcomanfrin.softwareops.entities.WorkFromPlant;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.exceptions.ValidationException;
import marcomanfrin.softwareops.repositories.ClientRepository;
import marcomanfrin.softwareops.repositories.PlantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
public class PlantService implements IPlantService {

    private final PlantRepository plantRepository;
    private final ClientRepository clientRepository;

    public PlantService(PlantRepository plantRepository, ClientRepository clientRepository) {
        this.plantRepository = plantRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public PlantResponse createPlant(CreatePlantRequest request) {
        List<String> errors = new ArrayList<>();

        String normalizedName = normalize(request.name());
        String normalizedOrder = normalize(request.orderNumber());
        String normalizedNotes = normalizeNullable(request.notes());

        if (normalizedName == null) errors.add("Plant name è obbligatorio");
        if (normalizedOrder == null) errors.add("Order number è obbligatorio");
        if (request.primaryClientId() == null) errors.add("Primary client id è obbligatorio");

        if (!errors.isEmpty()) throw new ValidationException(errors);

        Client primaryClient = clientRepository.findById(request.primaryClientId())
                .orElseThrow(() -> new NotFoundException("Primary client non trovato: " + request.primaryClientId()));

        Client finalClient = null;
        if (request.finalClientId() != null) {
            finalClient = clientRepository.findById(request.finalClientId())
                    .orElseThrow(() -> new NotFoundException("Final client non trovato: " + request.finalClientId()));
        }

        Plant plant = new Plant();
        plant.setName(normalizedName);
        plant.setNotes(normalizedNotes);
        plant.setOrderNumber(normalizedOrder);
        plant.setPrimaryClient(primaryClient);
        plant.setFinalClient(finalClient);
        plant.setInvoiced(false);
        plant.setInvoicedAt(null);
        var saved = plantRepository.save(plant);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlantResponse> getAllPlants() {
        return plantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlantResponse> getAllPlants(Pageable pageable) {
        return plantRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PlantResponse getPlantById(UUID id) {
        if (id == null) throw new ValidationException(List.of("Plant id è obbligatorio"));
        return plantRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Plant non trovato: " + id));
    }

    @Override
    public PlantResponse updatePlant(UUID id, UpdatePlantRequest request) {
        Plant plant = plantRepository.findById(id).orElseThrow(() -> new NotFoundException("Plant non trovato: " + id));

        String normalizedName = normalize(request.name());
        String normalizedOrder = normalize(request.orderNumber());
        String normalizedNotes = normalizeNullable(request.notes());

        if (normalizedName != null) plant.setName(normalizedName);
        if (normalizedOrder != null) plant.setOrderNumber(normalizedOrder);
        plant.setNotes(normalizedNotes);

        var saved = plantRepository.save(plant);
        return toResponse(saved);
    }

    @Override
    public void deletePlant(UUID id) {
        Plant plant = plantRepository.findById(id).orElseThrow(() -> new NotFoundException("Plant non trovato: " + id));

        // stacco i work: restano in DB, solo senza impianto collegato
        for (WorkFromPlant w : plant.getWorksFromPlant()) {
            w.setPlant(null);
        }

        plantRepository.delete(plant);
    }

    @Override
    public PlantResponse invoicePlant(UUID id) {
        Plant plant = plantRepository.findById(id).orElseThrow(() -> new NotFoundException("Plant non trovato: " + id));
        if (plant.getInvoiced()) return toResponse(plant);

        plant.setInvoiced(true);
        plant.setInvoicedAt(LocalDateTime.now());
        var saved = plantRepository.save(plant);
        return toResponse(saved);
    }

    @Override
    public List<PlantResponse> getPlantsByClient(UUID clientId) {
        List<Plant> primary = plantRepository.findByPrimaryClient_Id(clientId);
        List<Plant> fin = plantRepository.findByFinalClient_Id(clientId);

        return Stream.concat(primary.stream(), fin.stream())
                .distinct()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Page<PlantResponse> getPlantsByClient(UUID clientId, Pageable pageable) {
        return plantRepository
                .findDistinctByPrimaryClient_IdOrFinalClient_Id(clientId, clientId, pageable)
                .map(this::toResponse);
    }

    @Override
    public List<PlantResponse> searchPlants(String query) {
        String q = (query == null) ? "" : query.trim();
        if (q.isBlank()) return getAllPlants();
        return plantRepository
                .search(q)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Page<PlantResponse> searchPlants(String query, Pageable pageable) {
        String q = (query == null) ? "" : query.trim();
        if (q.isBlank()) return getAllPlants(pageable);
        return plantRepository
                .search(q, pageable)
                .map(this::toResponse);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isBlank() ? null : v;
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isBlank() ? null : v;
    }

    private PlantResponse toResponse(Plant plant) {
        return new PlantResponse(
                plant.getId(),
                plant.getName(),
                plant.getNotes(),
                plant.getOrderNumber(),
                plant.getPrimaryClient().getId(),
                plant.getFinalClient() != null ? plant.getFinalClient().getId() : null,
                plant.getInvoiced()
        );
    }
}
