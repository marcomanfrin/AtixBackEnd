package marcomanfrin.atixbackend.controllers;

import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.clients.ClientRequest;
import marcomanfrin.atixbackend.DTO.clients.ClientResponse;
import marcomanfrin.atixbackend.DTO.clients.ClientUpdateRequest;
import marcomanfrin.atixbackend.services.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/clients")
public class ClientsController {
    private final ClientService clientService;

    public ClientsController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    // TODO: @PreAuthorize("@securityService.isSeller(authentication)")
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientRequest request) {
        ClientResponse client = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(client);
    }

    @GetMapping
    public ResponseEntity<Page<ClientResponse>> getAllClients(Pageable pageable) {
        Page<ClientResponse> clients = clientService.getAllClients(pageable);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable UUID id) {
        ClientResponse client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable UUID id,
            @Valid @RequestBody ClientUpdateRequest request) {
        ClientResponse client = clientService.updateClient(id, request);
        return ResponseEntity.ok(client);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or @securityService.isTechnician(authentication)")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
