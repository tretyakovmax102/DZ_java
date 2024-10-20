package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.kafka.producer.KafkaTransactionProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.CheckResponse;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.TransactionService;
import ru.t1.java.demo.util.AccountType;
import ru.t1.java.demo.web.CheckWebClient;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Сервис управления транзакциями
 * unlockAccount разблокировка счетов
 * processTransaction обработка транзакции
 * handleTransactionError для обработки ошибок транзакций
 * cancelTransaction отмена транзакций
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final KafkaTransactionProducer kafkaTransactionProducer;
    private final CheckWebClient checkWebClient;
    private AccountService accountService;
    @Value("${t1.kafka.topic.transaction_errors}")
    private String transactionErrorTopic;

    @Override
    public Transaction getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId).orElse(null);
    }

    @Override
    @Transactional
    public boolean unlockAccount(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);

        if (transaction == null) {
            log.error("Transaction not found with id {}", transactionId);
            return false;
        }
        Account account = transaction.getAccount();

        if (account.getAccountType() == AccountType.CREDIT) {
            if (account.getBalance().compareTo(transaction.getAmount()) >= 0) {
                account.unblock();
                accountService.save(account);
                processTransaction(transaction);
                return true;
            } else {
                log.info("Insufficient balance for account {}", account.getId());
                return false;
            }
        } else if (account.getAccountType() == AccountType.DEBIT) {
            account.unblock();
            accountService.save(account);
            processTransaction(transaction);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void processTransaction(Transaction transaction) {
        Account account = transaction.getAccount();

        if (account.isBlocked()) {
            kafkaTransactionProducer.sendTo(transactionErrorTopic, transaction.getId());
            return;
        }
        Optional<CheckResponse> checkResponse = checkWebClient.check(account.getClientId());
        if (checkResponse.isPresent() && checkResponse.get().isBlocked()) {
            log.warn("Transaction blocked for account {}", account.getId());
            kafkaTransactionProducer.sendTo(transactionErrorTopic, transaction.getId());
            return;
        }
        transaction.setClientId(account.getClientId());
        if (transaction.getType().equalsIgnoreCase("DEBIT")) {
            if (account.getBalance().compareTo(transaction.getAmount()) < 0) {
                kafkaTransactionProducer.sendTo(transactionErrorTopic, transaction.getId());
            } else {
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
                transactionRepository.save(transaction);
                kafkaTransactionProducer.sendTo("t1_demo_client_transactions", transaction.getId());
            }
        } else if (transaction.getType().equalsIgnoreCase("CREDIT")) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
            transactionRepository.save(transaction);
            kafkaTransactionProducer.sendTo("t1_demo_client_transactions", transaction.getId());
        }
    }

    @Override
    @Transactional
    public void handleTransactionError(Long transactionId) {

        Transaction transaction = getTransactionById(transactionId);

        if (transaction != null) {
            Account account = transaction.getAccount();
            if (account.isBlocked()) {
                boolean isUnlocked = unlockAccount(account.getId());
                if (isUnlocked) {
                    processTransaction(transaction);
                } else {
                    log.error("Не удалось разблокировать счёт с ID: " + account.getId());
                }
            } else {
                processTransaction(transaction);
            }
        } else {
            log.error("Транзакция с ID " + transactionId + " не найдена.");
        }
    }

    @Override
    @Transactional
    public void cancelTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null) {
            log.error("Транзакция с ID {} не найдена", transactionId);
            return;
        }

        Account account = transaction.getAccount();
        if (transaction.getType().equalsIgnoreCase("DEBIT")) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        } else if (transaction.getType().equalsIgnoreCase("CREDIT")) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        }

        transactionRepository.delete(transaction);
        log.info("Транзакция с ID {} успешно отменена", transactionId);
    }

    @Override
    public List<TransactionDto> parseJson() {
        ObjectMapper mapper = new ObjectMapper();

        TransactionDto[] transactions = new TransactionDto[0];
        try {
            transactions = mapper.readValue(new File("src/main/resources/MOCK_DATA_TRANSACTION.json")
                    , TransactionDto[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Arrays.asList(transactions);
    }
}
