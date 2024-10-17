package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.model.Correction;
import ru.t1.java.demo.repository.CorrectionRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.TransactionService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис корректировки
 * handleTransactionError слушает топик Kafka и обрабатывает ошибки транзакций
 * retryFailedTransactions периодическая задача для повторной обработки транзакций
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CorrectionServiceImpl {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final CorrectionRepository correctionRepository;

    @Transactional
    @KafkaListener(topics = "t1_demo_client_transaction_errors", groupId = "t1-demo")
    public void handleTransactionError(Long transactionId) {
        boolean unlocked = transactionService.unlockAccount(transactionId);

        if (unlocked) {
            transactionRepository.deleteById(transactionId);
        } else {
            log.error("Failed to unlock account for transaction {}", transactionId);
            if (!correctionRepository.existsByTransactionId(transactionId)) {
                Correction correction = new Correction();
                correction.setTransactionId(transactionId);
                correction.setTimestamp(LocalDateTime.now());
                correctionRepository.save(correction);
            }
        }
    }

    @Scheduled(fixedDelayString = "${correction.retry.delay}")
    @Transactional
    public void retryFailedTransactions() {
        List<Correction> corrections = correctionRepository.findAll();

        for (Correction correction : corrections) {
            Long transactionId = correction.getTransactionId();
            boolean unlocked = transactionService.unlockAccount(transactionId);
            if (unlocked) {
                correctionRepository.deleteByTransactionId(transactionId);
                transactionRepository.deleteById(transactionId);
                log.info("Transaction {} successfully processed and removed from corrections", transactionId);
            } else {
                log.warn("Retry for transaction {} failed", transactionId);
            }
        }
    }
}
