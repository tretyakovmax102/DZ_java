package ru.t1.java.demo.util;

import org.springframework.stereotype.Component;
import ru.t1.java.demo.aop.ReplaceResult;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.ClientDto;

@Component
public class ClientMapper {

    public static Client toEntity(ClientDto dto) {
        if (dto.getMiddleName() == null) {
//            throw new NullPointerException();
        }
        return Client.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .middleName(dto.getMiddleName())
                .build();
    }

    public ClientDto toDto(Client entity) {
        return ClientDto.builder()
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .middleName(entity.getMiddleName())
                .build();
    }

    //@ReplaceResult
    public Client toEntityWithId(ClientDto dto) {
        if (dto.getMiddleName() == null) {
            throw new NullPointerException();
        }
        return Client.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .middleName(dto.getMiddleName())
                .build();
    }

}
