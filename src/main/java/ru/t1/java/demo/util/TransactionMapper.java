package ru.t1.java.demo.util;

import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.TransactionDto;

@Component
public class TransactionMapper {

    public static Transaction toEntity(TransactionDto dto) {
        Transaction transaction = Transaction.builder()
                .amount(dto.getAmount())
                .clientId(dto.getClientId())
                .type(dto.getType())
                .status(dto.getStatus())
                .build();
        return transaction;
    }

    public TransactionDto toDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .clientId(transaction.getClientId())
                .accountId(transaction.getAccount().getId())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .build();
    }
}

