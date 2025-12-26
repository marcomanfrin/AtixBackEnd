package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.DTO.clients.ClientRequest;
import marcomanfrin.softwareops.DTO.clients.ClientResponse;
import marcomanfrin.softwareops.ServiceInterfaces.IClientService;
import marcomanfrin.softwareops.entities.Client;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.repositories.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClientService implements IClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        Client client = new Client(request.name(), request.type());
        Client savedClient = clientRepository.save(client);
        return toClientResponse(savedClient);
    }

    @Override
    public Page<ClientResponse> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable)
                .map(this::toClientResponse);
    }

    @Override
    public ClientResponse getClientById(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found with id: " + id));
        return toClientResponse(client);
    }

    @Override
    @Transactional
    public ClientResponse updateClient(UUID id, ClientRequest request) {
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
