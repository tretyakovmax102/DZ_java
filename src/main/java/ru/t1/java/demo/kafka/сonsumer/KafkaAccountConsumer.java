package ru.t1.java.demo.kafka.сonsumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.service.AccountService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaAccountConsumer {

    private final AccountService accountService;

    @KafkaListener(id = "${t1.kafka.consumer.group-id}-account",
            topics = "${t1.kafka.topic.account_registration}",
            containerFactory = "accountKafkaListenerContainerFactory")
    public void listener(@Payload List<AccountDto> messageList,
                         Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.debug("Account consumer: Обработка новых сообщений");

        try {
            for (AccountDto accountDto : messageList) {
                try {
                    accountService.registerAccount(accountDto);
                } catch (RuntimeException e) {
                    log.error("Ошибка при регистрации аккаунта для клиента с ID: {}", accountDto.getClientId(), e);
                }
            }
        } finally {
            ack.acknowledge();
        }

        log.debug("Account consumer: записи обработаны");
    }
}

