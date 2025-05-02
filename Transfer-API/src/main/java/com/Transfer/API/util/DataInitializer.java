package com.Transfer.API.util;


import com.Transfer.API.models.Account;
import com.Transfer.API.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) throws Exception {

        logger.info("Checking if accounts data needs to be initialized...");
        List<Account> accounts = Arrays.asList(
                new Account(null, "1000000001", "John Doe", BigDecimal.valueOf(1500.75), LocalDateTime.now(), LocalDateTime.now()),
                new Account(null, "1000000002", "Jane Smith", BigDecimal.valueOf(2450.00), LocalDateTime.now(), LocalDateTime.now()),
                new Account(null, "1000000003", "Michael Brown", BigDecimal.valueOf(302.25), LocalDateTime.now(), LocalDateTime.now()),
                new Account(null, "1000000004", "Emily Davis", BigDecimal.valueOf(9800.10), LocalDateTime.now(), LocalDateTime.now()),
                new Account(null, "1000000005", "Chris Wilson", BigDecimal.valueOf(74.35), LocalDateTime.now(), LocalDateTime.now()),
                new Account(null, "1000000006", "Laura Johnson", BigDecimal.valueOf(5120.00), LocalDateTime.now(), LocalDateTime.now()),
                new Account(null, "1000000007", "Daniel Miller", BigDecimal.valueOf(230.90), LocalDateTime.now(), LocalDateTime.now()),
                new Account(null, "1000000008", "Sophia Moore", BigDecimal.valueOf(845.60), LocalDateTime.now(), LocalDateTime.now()),
                new Account(null, "1000000009", "James Anderson", BigDecimal.valueOf(1930.00), LocalDateTime.now(), LocalDateTime.now()),
                new Account(null, "1000000010", "Olivia Taylor", BigDecimal.valueOf(110.75), LocalDateTime.now(), LocalDateTime.now())
        );

        for (Account account : accounts) {
            if (!accountRepository.findByAccountNumber(account.getAccountNumber()).isPresent()) {
                logger.info("Initializing data");
                accountRepository.save(account);
            }
        }

        logger.info("Successfully initialized account data");

    }
}
