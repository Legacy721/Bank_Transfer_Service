package com.Transfer.API.service;

import com.Transfer.API.data.SummaryResponse;
import com.Transfer.API.data.TransferRequest;
import com.Transfer.API.enums.TransactionStatus;
import com.Transfer.API.models.Transaction;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {


    Transaction processMoneyTransfer(TransferRequest request);
    Page<Transaction> getTransactions(
            TransactionStatus status,
            String accountNumber,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int offset,
            int limit);
    void calculateCommissions();
    SummaryResponse generateDailySummary(LocalDate date);
    void generateDailyTransactionSummary(LocalDate date);
}
