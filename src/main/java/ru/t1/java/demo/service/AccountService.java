package ru.t1.java.demo.service;

import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.AccountDto;

import java.util.List;

public interface AccountService {

    void registerAccount(AccountDto accountDto);

    List<AccountDto> parseJson();

    boolean unlockAccount(Long accountId);

    void save(Account account);
}

