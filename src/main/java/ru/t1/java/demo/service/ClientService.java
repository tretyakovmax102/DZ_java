package ru.t1.java.demo.service;

import ru.t1.java.demo.model.dto.ClientDto;

import java.util.List;

public interface ClientService {

    List<ClientDto> parseJson();

    void registerClient(ClientDto clientDto);
}
