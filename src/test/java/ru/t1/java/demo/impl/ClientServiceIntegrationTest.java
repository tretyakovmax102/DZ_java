package ru.t1.java.demo.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import ru.t1.java.demo.kafka.producer.KafkaClientProducer;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.ClientDto;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.impl.ClientServiceImpl;
import ru.t1.java.demo.web.CheckWebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.yml")
class ClientServiceIntegrationTest {

    @Mock
    KafkaClientProducer kafkaClientProducer;

    @MockBean
    ClientRepository clientRepository;

    //    @MockBean
    @Autowired
    CheckWebClient checkWebClient;

    //    @InjectMocks
    @Autowired
    ClientServiceImpl clientService;

    @Test
    void registerClientDtoTest() {

        ClientDto clientDto = new ClientDto();
        clientDto.setFirstName("John");
        clientDto.setLastName("Doe");
        clientDto.setMiddleName("Middle");

        Client client = Client.builder()
                .firstName(clientDto.getFirstName())
                .lastName(clientDto.getLastName())
                .middleName(clientDto.getMiddleName())
                .build();

        Client savedClient = Client.builder()
                .firstName("John")
                .lastName("Doe")
                .middleName("Middle")
                .build();

        when(clientRepository.save(client)).thenReturn(savedClient);
        clientService.registerClient(clientDto);

        assertThat(savedClient.getFirstName()).isEqualTo(clientDto.getFirstName());
        assertThat(savedClient.getLastName()).isEqualTo(clientDto.getLastName());
        assertThat(savedClient.getMiddleName()).isEqualTo(clientDto.getMiddleName());
    }

}
