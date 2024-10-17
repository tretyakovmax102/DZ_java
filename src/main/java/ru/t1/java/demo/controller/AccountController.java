package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.LoggableException;
import ru.t1.java.demo.aop.Track;
import ru.t1.java.demo.kafka.producer.KafkaAccountProducer;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.service.AccountService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final KafkaAccountProducer kafkaAccountProducer;
    @Value("${t1.kafka.topic.account_registration}")
    private String topic;

    @PutMapping("/{id}/unlock")
    public ResponseEntity<String> unlockAccount(@PathVariable Long id) {
        boolean unlocked = accountService.unlockAccount(id);
        if (unlocked) {
            return ResponseEntity.ok("Account unlocked");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unable to unlock account");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerAccount(@RequestBody AccountDto accountDto) {
        try {
            accountService.registerAccount(accountDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Account successfully registered");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client not found: " + e.getMessage());
        }
    }

    @LoggableException
    @Track
    @GetMapping(value = "/parseAccount")
    @HandlingResult
    public void parseSource() {
        List<AccountDto> accountDtos = accountService.parseJson();
        accountDtos.forEach(dto -> {
            kafkaAccountProducer.sendTo(topic, dto);
        });
    }

}