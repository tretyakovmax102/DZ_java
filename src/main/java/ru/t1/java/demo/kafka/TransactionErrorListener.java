package ru.t1.java.demo.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.service.TransactionService;

@Slf4j
@Component
public class TransactionErrorListener {

    private TransactionService transactionService;

    @KafkaListener(topics = "t1_demo_client_transaction_errors", groupId = "t1-demo")
    public void listenError(Long transactionId) {
        Transaction transaction = transactionService.getTransactionById(transactionId);

        if (transaction != null) {
            Account account = transaction.getAccount();

            if (account.isBlocked()) {
                boolean isUnlocked = transactionService.unlockAccount(account.getId());
                if (isUnlocked) {
                    transactionService.processTransaction(transaction);
                } else {
                   log.info("Failed to unlock account with ID: " + account.getId());
                }
            } else {
                transactionService.processTransaction(transaction);
            }
        } else {
            log.info("Transaction with ID " + transactionId + " not found.");
        }
    }
}
