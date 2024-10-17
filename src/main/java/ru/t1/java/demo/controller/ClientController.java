package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.LoggableException;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.ClientDto;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.ClientService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;
    private final ClientRepository clientRepository;

    @PostMapping("/register")
    public ResponseEntity<String> registerClient(@RequestBody ClientDto clientDto) {
        try {
            clientService.registerClient(clientDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Client successfully registered");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error registering client: " + e.getMessage());
        }
    }

    @HandlingResult
    @GetMapping(value = "/parseClient")
    @LoggableException
    public void parseSource() {
        clientRepository.save(Client.builder()
                .firstName("John42")
                .build());
        clientRepository.findClientByFirstName("John42");
    }

    @GetMapping("/admin")
//    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin Board.";
    }
}
