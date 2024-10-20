package ru.t1.java.demo.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.CheckResponse;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.impl.TransactionServiceImpl;
import ru.t1.java.demo.web.CheckWebClient;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.yml")
class TransactionServiceIntegrationTest {

    @Autowired
    TransactionServiceImpl transactionService;

    @MockBean
    CheckWebClient checkWebClient;

    @MockBean
    TransactionRepository transactionRepository;

    @Test
    void processTransactionSuccess() {
        Account account = new Account();
        account.setClientId(1L);
        account.setBalance(new BigDecimal("100.00"));
        account.setBlocked(false);
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setAccount(account);
        transaction.setType("DEBIT");
        when(checkWebClient.check(1L)).thenReturn(Optional.of(CheckResponse.builder().blocked(false).build()));
        transactionService.processTransaction(transaction);
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("50.00"));
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void processTransactionNotProcessTransaction() {
        Account account = new Account();
        account.setClientId(1L);
        account.setBalance(new BigDecimal("100.00"));
        account.setBlocked(false);
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setAccount(account);
        transaction.setType("DEBIT");
        when(checkWebClient.check(1L)).thenReturn(Optional.of(CheckResponse.builder().blocked(true).build()));
        transactionService.processTransaction(transaction);
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        verify(transactionRepository, never()).save(transaction);
    }

    @Test
    void cancelTransactionTransactionExists() {
        Long transactionId = 1L;
        Account account = Account.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(1000))
                .build();
        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .account(account)
                .amount(BigDecimal.valueOf(200))
                .type("DEBIT")
                .build();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        transactionService.cancelTransaction(transactionId);
        assertThat(account.getBalance()).isEqualTo(BigDecimal.valueOf(1200));
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void cancelTransactionDoesNotExist() {
        Long transactionId = 1L;
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());
        transactionService.cancelTransaction(transactionId);
        verify(transactionRepository, never()).delete(any(Transaction.class));
    }

    @Test
    void cancelTransactionCorrectly() {
        Long transactionId = 2L;
        Account account = Account.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(1000))
                .build();
        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .account(account)
                .amount(BigDecimal.valueOf(200))
                .type("CREDIT")
                .build();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        transactionService.cancelTransaction(transactionId);
        assertThat(account.getBalance()).isEqualTo(BigDecimal.valueOf(800));
        verify(transactionRepository).delete(transaction);
    }
}
