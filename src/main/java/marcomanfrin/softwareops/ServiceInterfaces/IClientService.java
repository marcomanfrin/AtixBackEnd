package marcomanfrin.softwareops.ServiceInterfaces;

import marcomanfrin.softwareops.DTO.clients.ClientRequest;
import marcomanfrin.softwareops.DTO.clients.ClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IClientService {
    ClientResponse createClient(ClientRequest request);
    Page<ClientResponse> getAllClients(Pageable pageable);
    ClientResponse getClientById(UUID id);
    ClientResponse updateClient(UUID id, ClientRequest request);
    void deleteClient(UUID id);
}
