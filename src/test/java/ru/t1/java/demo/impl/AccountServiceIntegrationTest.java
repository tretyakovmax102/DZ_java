package ru.t1.java.demo.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import ru.t1.java.demo.kafka.producer.KafkaAccountProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.impl.AccountServiceImpl;
import ru.t1.java.demo.util.AccountType;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.yml")
class AccountServiceIntegrationTest {

    @Mock
    KafkaAccountProducer kafkaAccountProducer;

    @MockBean
    AccountRepository accountRepository;

    @MockBean
    ClientRepository clientRepository;

    @Autowired
    AccountServiceImpl accountService;

    @Test
    void registerAccountTest() {
        AccountDto accountDto = new AccountDto();
        accountDto.setClientId(1L);
        accountDto.setAccountType(AccountType.DEBIT);
        accountDto.setBalance(BigDecimal.valueOf(1000));

        Client client = new Client();
        client.setId(1L);

        Account account = Account.builder()
                .clientId(client.getId())
                .accountType(accountDto.getAccountType())
                .balance(accountDto.getBalance())
                .isBlocked(false)
                .build();

        Account savedAccount = Account.builder()
                .id(100L)
                .clientId(client.getId())
                .accountType(accountDto.getAccountType())
                .balance(accountDto.getBalance())
                .isBlocked(false)
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        doNothing().when(kafkaAccountProducer).send(savedAccount.getId());
        accountService.registerAccount(accountDto);

        verify(accountRepository).save(any(Account.class));
        assertThat(savedAccount.getClientId()).isEqualTo(accountDto.getClientId());
        assertThat(savedAccount.getAccountType()).isEqualTo(accountDto.getAccountType());
        assertThat(savedAccount.getBalance()).isEqualTo(accountDto.getBalance());
    }

    @Test
    void unlockAccountSuccessTest() {
        Long accountId = 1L;
        Account account = Account.builder()
                .id(accountId)
                .accountType(AccountType.CREDIT)
                .balance(BigDecimal.valueOf(500))
                .isBlocked(true)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        boolean result = accountService.unlockAccount(accountId);
        verify(accountRepository).save(account);
        assertThat(result).isTrue();
        assertThat(account.isBlocked()).isFalse();
    }

    @Test
    void unlockAccountNegativeBalanceTest() {
        Long accountId = 2L;
        Account account = Account.builder()
                .id(accountId)
                .accountType(AccountType.CREDIT)
                .balance(BigDecimal.valueOf(-100))
                .isBlocked(true)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        boolean result = accountService.unlockAccount(accountId);
        verify(accountRepository, never()).save(account);
        assertThat(result).isFalse();
        assertThat(account.isBlocked()).isTrue();
    }

    @Test
    void unlockAccountNotFoundTest() {
        Long accountId = 3L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> accountService.unlockAccount(accountId)
        );
        assertThat(exception.getMessage()).isEqualTo("Account not found");
    }
}
