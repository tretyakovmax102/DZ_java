package ru.t1.java.demo.service;

import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.TransactionDto;

import java.util.List;

public interface TransactionService {

    List<TransactionDto> parseJson();

    void processTransaction(Transaction transaction);

    Transaction getTransactionById(Long transactionId);

    boolean unlockAccount(Long transactionId);

    void cancelTransaction(Long transactionId);

    void handleTransactionError(Long transactionId);

}
