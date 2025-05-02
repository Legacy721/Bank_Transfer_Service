package com.Transfer.API;

import com.Transfer.API.data.SummaryResponse;
import com.Transfer.API.data.TransferRequest;
import com.Transfer.API.enums.TransactionStatus;
import com.Transfer.API.models.Account;
import com.Transfer.API.models.Transaction;
import com.Transfer.API.models.TransactionSummary;
import com.Transfer.API.repository.AccountRepository;
import com.Transfer.API.repository.TransactionRepository;
import com.Transfer.API.repository.TransactionSummaryRepository;
import com.Transfer.API.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionSummaryRepository transactionSummaryRepository;


    @InjectMocks
    private TransactionServiceImpl transactionService;


    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(transactionService, "TRANSACTION_FEE_RATE", new BigDecimal("0.005"));
        ReflectionTestUtils.setField(transactionService, "MAX_TRANSACTION_FEE", new BigDecimal("100"));
        ReflectionTestUtils.setField(transactionService, "COMMISSION_RATE", new BigDecimal("0.2"));

    }

    @Test
    public void testSuccessfulTransfer() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("1234567890");
        request.setDestinationAccountNumber("6789012345");
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Test transfer");

        Account sourceAccount = new Account();
        sourceAccount.setAccountNumber("1234567890");
        sourceAccount.setBalance(new BigDecimal("500.00"));

        Account destAccount = new Account();
        destAccount.setAccountNumber("6789012345");
        destAccount.setBalance(new BigDecimal("200.00"));

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("6789012345")).thenReturn(Optional.of(destAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        Transaction result = transactionService.processMoneyTransfer(request);

        // Assert
        assertEquals(TransactionStatus.SUCCESSFUL, result.getStatus());
        assertEquals(new BigDecimal("0.50"), result.getTransactionFee());
        assertEquals(new BigDecimal("100.50"), result.getBilledAmount());

        // Verify
        verify(accountRepository, times(1)).save(argThat(account ->
                account.getAccountNumber().equals("1234567890") &&
                        account.getBalance().compareTo(new BigDecimal("399.50")) == 0));

        verify(accountRepository, times(1)).save(argThat(account ->
                account.getAccountNumber().equals("6789012345") &&
                        account.getBalance().compareTo(new BigDecimal("300.00")) == 0));
    }



    @Test
    public void testInsufficientFundsTransfer() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("1234567890");
        request.setDestinationAccountNumber("6789012345");
        request.setAmount(new BigDecimal("1000.00"));
        request.setDescription("Test transfer");

        Account sourceAccount = new Account();
        sourceAccount.setAccountNumber("12345");
        sourceAccount.setBalance(new BigDecimal("500.00"));

        Account destAccount = new Account();
        destAccount.setAccountNumber("67890");
        destAccount.setBalance(new BigDecimal("200.00"));

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("6789012345")).thenReturn(Optional.of(destAccount));

        // Act
        Transaction result = transactionService.processMoneyTransfer(request);

        // Assert
        assertEquals(TransactionStatus.INSUFFICIENT_FUNDS, result.getStatus());

        // Verify
        verify(accountRepository, never()).save(sourceAccount);
        verify(accountRepository, never()).save(destAccount);
    }



    @Test
    public void testCalculateCommissions() {
        // Arrange
        Transaction t1 = new Transaction();
        t1.setStatus(TransactionStatus.SUCCESSFUL);
        t1.setTransactionFee(new BigDecimal("10.00"));
        t1.setCommissionWorthy(false);

        Transaction t2 = new Transaction();
        t2.setStatus(TransactionStatus.SUCCESSFUL);
        t2.setTransactionFee(new BigDecimal("5.00"));
        t2.setCommissionWorthy(false);

        List<Transaction> transactions = Arrays.asList(t1, t2);

        when(transactionRepository.findByStatus(TransactionStatus.SUCCESSFUL)).thenReturn(transactions);

        // Act
        transactionService.calculateCommissions();

        // Verify
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(transactionRepository).save(argThat(t ->
                t.isCommissionWorthy() &&
                        t.getCommission().compareTo(new BigDecimal("2.00")) == 0));
        verify(transactionRepository).save(argThat(t ->
                t.isCommissionWorthy() &&
                        t.getCommission().compareTo(new BigDecimal("1.00")) == 0));
    }


    @Test
    public void testGenerateDailySummary() {
        // Arrange
        LocalDate testDate = LocalDate.now();
        LocalDateTime startOfDay = testDate.atStartOfDay();
        LocalDateTime endOfDay = testDate.atTime(LocalTime.MAX);

        Transaction t1 = new Transaction();
        t1.setAmount(new BigDecimal("100.00"));
        t1.setTransactionFee(new BigDecimal("0.50"));
        t1.setCommission(new BigDecimal("0.10"));
        t1.setCommissionWorthy(true);

        Transaction t2 = new Transaction();
        t2.setAmount(new BigDecimal("200.00"));
        t2.setTransactionFee(new BigDecimal("1.00"));
        t2.setCommission(new BigDecimal("0.20"));
        t2.setCommissionWorthy(true);

        when(transactionRepository.findByDateCreatedBetweenAndStatus(
                eq(startOfDay), eq(endOfDay), eq(TransactionStatus.SUCCESSFUL)))
                .thenReturn(Arrays.asList(t1, t2));

        when(transactionRepository.findByDateCreatedBetweenAndStatus(
                eq(startOfDay), eq(endOfDay), eq(TransactionStatus.FAILED)))
                .thenReturn(Collections.emptyList());

        when(transactionRepository.findByDateCreatedBetweenAndStatus(
                eq(startOfDay), eq(endOfDay), eq(TransactionStatus.INSUFFICIENT_FUNDS)))
                .thenReturn(Collections.emptyList());

        // Act
        transactionService.generateDailyTransactionSummary(testDate);

        // Verify
        verify(transactionRepository, times(1)).findByDateCreatedBetweenAndStatus(
                startOfDay, endOfDay, TransactionStatus.SUCCESSFUL);
        verify(transactionRepository, times(1)).findByDateCreatedBetweenAndStatus(
                startOfDay, endOfDay, TransactionStatus.FAILED);
        verify(transactionRepository, times(1)).findByDateCreatedBetweenAndStatus(
                startOfDay, endOfDay, TransactionStatus.INSUFFICIENT_FUNDS);
    }



    @Test
    void generateDailySummary_ValidDate_ReturnsMappedResponse() {
        // Arrange
        LocalDate date = LocalDate.of(2023, 10, 5);
        TransactionSummary mockSummary = new TransactionSummary();
        mockSummary.setId(1L);
        mockSummary.setDate(date);
        mockSummary.setFailedTransactions(2);
        mockSummary.setSuccessfulTransactions(10);
        mockSummary.setInsufficientFundsTransactions(3);
        mockSummary.setTotalAmount(BigDecimal.valueOf(1500.00));
        mockSummary.setTotalCommissions(BigDecimal.valueOf(30.00));
        mockSummary.setTotalFees(BigDecimal.valueOf(50.00));

        when(transactionSummaryRepository.findByDate(date)).thenReturn(mockSummary);

        // Act
        SummaryResponse response = transactionService.generateDailySummary(date);

        // Assert
        assertAll(
                () -> assertEquals(date, response.getDate()),
                () -> assertEquals(10, response.getSuccessfulTransactions()),
                () -> assertEquals(2, response.getFailedTransactions()),
                () -> assertEquals(3, response.getInsufficientFundsTransactions()),
                () -> assertEquals(BigDecimal.valueOf(1500.00), response.getTotalAmount()),
                () -> assertEquals(BigDecimal.valueOf(50.00), response.getTotalFees()),
                () -> assertEquals(BigDecimal.valueOf(30.00), response.getTotalCommissions())
        );
        verify(transactionSummaryRepository, times(1)).findByDate(date);
    }


}
