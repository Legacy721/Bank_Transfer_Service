package com.Transfer.API.controller;


import com.Transfer.API.data.SummaryResponse;
import com.Transfer.API.data.TransferRequest;
import com.Transfer.API.enums.TransactionStatus;
import com.Transfer.API.models.Transaction;
import com.Transfer.API.service.TransactionService;
import com.Transfer.API.service.impl.TransactionServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);


    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(@Valid @RequestBody TransferRequest request, BindingResult bindingResult) {
        logger.info("Transfer Money Controller");

        if (bindingResult.hasErrors())
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());

        return ResponseEntity.ok(transactionService.processMoneyTransfer(request));
    }


    @GetMapping
    public ResponseEntity<?> getTransactions(
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("Get Transactions Controller");
        List<Transaction> transactions = transactionService.getTransactions(status, accountNumber, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }


    @GetMapping("/summary/{date}")
    public ResponseEntity<?> getDailySummary(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        logger.info("Get Summary Controller");
        return ResponseEntity.ok(transactionService.generateDailySummary(date));
    }
}
