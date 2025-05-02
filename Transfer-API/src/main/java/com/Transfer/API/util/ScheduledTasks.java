package com.Transfer.API.util;

import com.Transfer.API.data.SummaryResponse;
import com.Transfer.API.models.TransactionSummary;
import com.Transfer.API.service.impl.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Component
@RequiredArgsConstructor
@EnableScheduling
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    private final TransactionServiceImpl transactionServiceImpl;


    @Scheduled(cron = "0 0 1 * * ?")
    public void calculateCommissions() {
        logger.info("Starting scheduled commission calculation");
        transactionServiceImpl.calculateCommissions();
        logger.info("Completed commission calculation");
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailySummary() {

        LocalDate yesterday = LocalDate.now().minusDays(1);
        logger.info("Generating transaction summary for {}", yesterday);
        transactionServiceImpl.generateDailyTransactionSummary(yesterday);
        logger.info("Generated summary successfully");
    }
}
