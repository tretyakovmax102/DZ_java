package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.kafka.producer.KafkaAccountProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.util.AccountType;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * registerAccount регистрация счета
 * unlockAccount разблокировка счета
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final KafkaAccountProducer kafkaAccountProducer;

    @Override
    @Transactional
    public void registerAccount(AccountDto accountDto) {
        Client client = clientRepository.findById(accountDto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Account account = Account.builder()
                .clientId(client.getId())
                .accountType(accountDto.getAccountType())
                .balance(accountDto.getBalance())
                .isBlocked(false)
                .build();

        Account savedAccount = accountRepository.save(account);
        kafkaAccountProducer.send(savedAccount.getId());
    }

    @Override
    @Transactional
    public boolean unlockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (account.getAccountType() == AccountType.CREDIT && account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        account.unblock();
        accountRepository.save(account);
        return true;
    }


    @Override
    @Transactional
    public void save(Account account) {
        accountRepository.save(account);
    }

    @Override
    public List<AccountDto> parseJson() {
        ObjectMapper mapper = new ObjectMapper();
        AccountDto[] accounts = new AccountDto[0];
        try {
            accounts = mapper.readValue(new File("src/main/resources/MOCK_DATA_ACCOUNT.json"), AccountDto[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.asList(accounts);
    }

}

