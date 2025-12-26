package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.entities.Client;
import marcomanfrin.softwareops.enums.ClientType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface IClientService {

    Client createClient(String name, ClientType type);
    List<Client> getAllClients();
    Page<Client> getAllClients(Pageable pageable);
    Client updateClient(UUID id, String name, ClientType type);
    void deleteClient(UUID id);
    List<Client> searchClients(String query);
    Client getClientByIdOrThrow(UUID id);
}
