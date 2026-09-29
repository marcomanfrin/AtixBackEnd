package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.clients.ClientRequest;
import marcomanfrin.atixbackend.DTO.clients.ClientResponse;
import marcomanfrin.atixbackend.DTO.clients.ClientUpdateRequest;
import marcomanfrin.atixbackend.ServiceInterfaces.IClientService;
import marcomanfrin.atixbackend.entities.Client;
import marcomanfrin.atixbackend.enums.ClientType;
import marcomanfrin.atixbackend.exceptions.NotFoundException;
import marcomanfrin.atixbackend.repositories.ClientRepository;
import marcomanfrin.atixbackend.repositories.WorkRepository;
import marcomanfrin.atixbackend.specifications.ClientSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClientService implements IClientService {
    private final ClientRepository clientRepository;
    private final WorkRepository workRepository;

    public ClientService(ClientRepository clientRepository, WorkRepository workRepository) {
        this.clientRepository = clientRepository;
        this.workRepository = workRepository;
    }

    @Override
    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        Client client = new Client(request.name(), request.type());
        Client savedClient = clientRepository.save(client);
        return toClientResponse(savedClient);
    }

    @Override
    public Page<ClientResponse> getAllClients(Pageable pageable, String search, ClientType type) {
        return clientRepository.findAll(
                        ClientSpecification.searchByKeyword(search).and(ClientSpecification.hasType(type)),
                        pageable)
                .map(this::toClientResponse);
    }

    @Override
    public List<ClientResponse> getAllClientsAsList() {
        return clientRepository.findAll(Sort.by("name")).stream()
                .map(this::toClientResponse)
                .toList();
    }

    @Override
    public ClientResponse getClientById(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found with id: " + id));
        return toClientResponse(client);
    }

    @Override
    @Transactional
    public ClientResponse updateClient(UUID id, ClientUpdateRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found with id: " + id));

        // PATCH logic: update only non-null fields
        if (request.name() != null) {
            client.setName(request.name());
        }
        if (request.type() != null) {
            client.setType(request.type());
        }

        Client updatedClient = clientRepository.save(client);
        return toClientResponse(updatedClient);
    }

    @Override
    @Transactional
    public void deleteClient(UUID id) {
        if (!clientRepository.existsById(id)) {
            throw new NotFoundException("Client not found with id: " + id);
        }

        // Clear client references from Works (both atixClient and finalClient)
        workRepository.findAll().forEach(work -> {
            boolean needsSave = false;
            if (work.getAtixClient() != null && work.getAtixClient().getId().equals(id)) {
                work.setAtixClient(null);
                needsSave = true;
            }
            if (work.getFinalClient() != null && work.getFinalClient().getId().equals(id)) {
                work.setFinalClient(null);
                needsSave = true;
            }
            if (needsSave) {
                workRepository.save(work);
            }
        });

        clientRepository.deleteById(id);
    }

    private ClientResponse toClientResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getType()
        );
    }
}
