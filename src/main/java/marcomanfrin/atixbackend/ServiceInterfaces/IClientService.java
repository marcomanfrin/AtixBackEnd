package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.DTO.clients.ClientRequest;
import marcomanfrin.atixbackend.DTO.clients.ClientResponse;
import marcomanfrin.atixbackend.DTO.clients.ClientUpdateRequest;
import marcomanfrin.atixbackend.enums.ClientType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IClientService {
    ClientResponse createClient(ClientRequest request);
    Page<ClientResponse> getAllClients(Pageable pageable, String search, ClientType type);
    List<ClientResponse> getAllClientsAsList();
    ClientResponse getClientById(UUID id);
    ClientResponse updateClient(UUID id, ClientUpdateRequest request);
    void deleteClient(UUID id);
}
