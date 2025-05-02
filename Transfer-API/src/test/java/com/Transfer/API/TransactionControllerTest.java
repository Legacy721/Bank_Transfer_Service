package com.Transfer.API;

import com.Transfer.API.controller.TransactionController;
import com.Transfer.API.data.SummaryResponse;
import com.Transfer.API.data.TransferRequest;
import com.Transfer.API.enums.TransactionStatus;
import com.Transfer.API.models.Transaction;
import com.Transfer.API.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }


    @Test
    void transferMoney_ValidRequest_ReturnsTransaction() throws Exception {

        // Arrange
        TransferRequest request = new TransferRequest();
        request.setDescription("Test");
        request.setSourceAccountNumber("1234567890");
        request.setDestinationAccountNumber("6789012345");
        request.setAmount(BigDecimal.valueOf(40.00));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setSourceAccountNumber("123456");
        transaction.setDestinationAccountNumber("654321");
        transaction.setTransactionFee(BigDecimal.valueOf(100.0));
        transaction.setStatus(TransactionStatus.SUCCESSFUL);

        when(transactionService.processMoneyTransfer(any(TransferRequest.class))).thenReturn(transaction);

        //Assert
        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SUCCESSFUL"));
    }


    @Test
    void transferMoney_InvalidRequest_ReturnsBadRequest() throws Exception {
        TransferRequest request = new TransferRequest();

        request.setSourceAccountNumber(null);
        request.setDestinationAccountNumber(null);
        request.setAmount(BigDecimal.valueOf(-50.0));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getTransactions_WithAllParameters_ReturnsFilteredTransactions() throws Exception {

        //Arrange
        List<Transaction> transactions = Arrays.asList(
                new Transaction(), new Transaction()
        );
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 2, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 1, 2, 23, 59);

        // Act
        when(transactionService.getTransactions(eq(TransactionStatus.SUCCESSFUL), eq("1234567890"), eq(startDate), eq(endDate)))
                .thenReturn(transactions);

        // Assert
        mockMvc.perform(get("/api/v1/transactions")
                .param("status", "SUCCESSFUL")
                .param("accountNumber", "1234567890")
                .param("startDate", "2023-01-02T00:00:00")
                .param("endDate", "2023-01-02T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));
    }


    @Test
    void getTransactions_NoParameters_ReturnsAllTransactions() throws Exception {
        List<Transaction> transactions = Arrays.asList(new Transaction(), new Transaction());

        when(transactionService.getTransactions(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));
    }



    @Test
    void getTransactions_InvalidStatus_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/transactions")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getDailySummary_ValidDate_ReturnsSummary() throws Exception {
        LocalDate date = LocalDate.of(2023, 10, 10);
        SummaryResponse summary = new SummaryResponse();
        summary.setDate(date);
        summary.setTotalAmount(BigDecimal.valueOf(500.0));
        summary.setSuccessfulTransactions(3);

        when(transactionService.generateDailySummary(eq(date))).thenReturn(summary);

        mockMvc.perform(get("/api/v1/transactions/summary/2023-10-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2023-10-10"))
                .andExpect(jsonPath("$.totalAmount").value(500.0))
                .andExpect(jsonPath("$.successfulTransactions").value(3));
    }

    @Test
    void getDailySummary_InvalidDateFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/summary/2023-10-10T12:30"))
                .andExpect(status().isBadRequest());
    }
}