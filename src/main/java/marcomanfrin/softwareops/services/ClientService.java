package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.entities.Client;
import marcomanfrin.softwareops.enums.ClientType;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.repositories.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService implements IClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Client createClient(String name, ClientType type) {
        String validatedName = validateName(name);

        if (type == null) {
            throw new IllegalArgumentException("Client type is required");
        }

        if (clientRepository.existsByNameIgnoreCase(validatedName)) {
            throw new IllegalArgumentException("Client name already exists");
        }

        Client client = new Client();
        client.setName(validatedName);
        client.setType(type);

        return clientRepository.save(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Client getClientByIdOrThrow(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found: " + id));
    }

    @Override
    public Client updateClient(UUID id, String name, ClientType type) {
        Client client = getClientByIdOrThrow(id);

        if (name != null) {
            String validatedName = validateName(name);

            boolean nameChanged = !client.getName().equalsIgnoreCase(validatedName);
            if (nameChanged && clientRepository.existsByNameIgnoreCase(validatedName)) {
                throw new IllegalArgumentException("Client name already exists");
            }

            client.setName(validatedName);
        }

        if (type != null) {
            client.setType(type);
        }

        return clientRepository.save(client);
    }

    @Override
    public void deleteClient(UUID id) {
        Client client = getClientByIdOrThrow(id);
        clientRepository.delete(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> searchClients(String query) {
        String q = (query == null) ? "" : query.trim();
        if (q.isBlank()) return getAllClients();
        return clientRepository.findByNameContainingIgnoreCase(q);
    }


    private String validateName(String name) {
        if (name == null || name.trim().isBlank()) {
            throw new IllegalArgumentException("Client name cannot be blank");
        }
        return name.trim();
    }
}
