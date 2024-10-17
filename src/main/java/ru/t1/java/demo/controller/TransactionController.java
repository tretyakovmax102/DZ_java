package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.LoggableException;
import ru.t1.java.demo.aop.Track;
import ru.t1.java.demo.kafka.producer.KafkaTransactionProducer;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.service.TransactionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final KafkaTransactionProducer kafkaTransactionProducer;
    @Value("${t1.kafka.topic.transaction_registration}")
    private String topic;

    @PostMapping("/create-transaction")
    public ResponseEntity<?> createTransaction(@RequestBody Transaction transaction) {
        transactionService.processTransaction(transaction);
        return ResponseEntity.ok("Transaction processed");
    }

    @LoggableException
    @Track
    @GetMapping(value = "/parse-transaction")
    @HandlingResult
    public void parseSource() {
        List<TransactionDto> transactionDtos = transactionService.parseJson();
        transactionDtos.forEach(dto -> {
            kafkaTransactionProducer.sendTo(topic, dto);
        });
    }

}

