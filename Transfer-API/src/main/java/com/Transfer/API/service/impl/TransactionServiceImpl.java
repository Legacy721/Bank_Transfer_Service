package com.Transfer.API.service.impl;

import com.Transfer.API.data.SummaryResponse;
import com.Transfer.API.data.TransferRequest;
import com.Transfer.API.enums.TransactionStatus;
import com.Transfer.API.exceptions.DatabaseException;
import com.Transfer.API.exceptions.InvalidRequestException;
import com.Transfer.API.exceptions.ResourceNotFoundException;
import com.Transfer.API.models.Account;
import com.Transfer.API.models.Transaction;
import com.Transfer.API.models.TransactionSummary;
import com.Transfer.API.repository.AccountRepository;
import com.Transfer.API.repository.TransactionRepository;
import com.Transfer.API.repository.TransactionSummaryRepository;
import com.Transfer.API.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {


    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Value("${transaction.fee.rate}")
    private BigDecimal TRANSACTION_FEE_RATE;
    @Value("${max.transaction.fee}")
    private BigDecimal MAX_TRANSACTION_FEE;
    @Value("${commission.rate}")
    private BigDecimal COMMISSION_RATE;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionSummaryRepository transactionSummaryRepository;



    @Override
    public Transaction processMoneyTransfer(TransferRequest request) {
        logger.info("request/Processing money transfer request {}", request );

        validatingTransferRequest(request);

        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateUniqueTransRef());
        transaction.setAmount(request.getAmount());
        transaction.setSourceAccountNumber(request.getSourceAccountNumber());
        transaction.setDestinationAccountNumber(request.getDestinationAccountNumber());
        transaction.setDescription(request.getDescription());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCommissionWorthy(false);
        transaction.setCommission(BigDecimal.ZERO);


        BigDecimal transactionFee = request.getAmount().multiply(TRANSACTION_FEE_RATE);
        transactionFee = transactionFee.setScale(2, RoundingMode.HALF_UP);

        if (transactionFee.compareTo(MAX_TRANSACTION_FEE) > 0)
            transactionFee = MAX_TRANSACTION_FEE;

        transaction.setTransactionFee(transactionFee);
        transaction.setBilledAmount(request.getAmount().add(transactionFee));

        transactionRepository.save(transaction);

        try {

            Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));

            Account destinationAccount = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

            BigDecimal totalDebit = request.getAmount().add(transactionFee);
            if (sourceAccount.getBalance().compareTo(totalDebit) < 0) {
                transaction.setStatus(TransactionStatus.INSUFFICIENT_FUNDS);
                transaction.setStatusMessage("Insufficient funds in source account");
                transactionRepository.save(transaction);
                return transaction;
            }


            sourceAccount.setBalance(sourceAccount.getBalance().subtract(totalDebit));
            destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);

            transaction.setStatus(TransactionStatus.SUCCESSFUL);
            transaction.setStatusMessage("Transfer completed successfully");

            return transactionRepository.save(transaction);

        } catch (ResourceNotFoundException ex) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setStatusMessage(ex.getMessage());
            return transactionRepository.save(transaction);
        } catch (Exception ex) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setStatusMessage("Transfer failed: " + ex.getMessage());
            return transactionRepository.save(transaction);
        }
    }




    @Transactional(readOnly = true)
    @Override
    public Page<Transaction> getTransactions(
            TransactionStatus status,
            String accountNumber,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int offset,
            int limit) {
        logger.info("request/get transaction list...");

        Pageable pageable = PageRequest.of(offset - 1, limit, Sort.by(Sort.Direction.DESC, "id"));

       try{
           return transactionRepository.findWithFilters(status, accountNumber, startDate, endDate, pageable);

       }catch (Exception e){
           logger.error("Exception:" + e.getMessage());
           logger.debug("An error has occurred: "+ e.getCause() );
           throw new DatabaseException("An error has occurred: " +e.getMessage());
       }
    }



    @Override
    public void calculateCommissions() {

        try{

            List<Transaction> successfulTransactions = transactionRepository.findByStatus(TransactionStatus.SUCCESSFUL)
                    .stream()
                    .filter(t -> !t.isCommissionWorthy())
                    .toList();

            for (Transaction transaction : successfulTransactions) {
                BigDecimal commission = transaction.getTransactionFee().multiply(COMMISSION_RATE);
                transaction.setCommissionWorthy(true);
                transaction.setCommission(commission);
                transactionRepository.save(transaction);
            }
        }catch (Exception e){
            logger.error("Exception:" + e.getMessage());
            logger.debug("An error has occurred: "+ e.getCause() );
            throw new DatabaseException("An error has occurred: " +e.getMessage());
        }
    }


    @Override
    public SummaryResponse generateDailySummary(LocalDate date){
        logger.info("request/Get daily summary of transactions for {} date", date);

        try{

            TransactionSummary transactionSummary = transactionSummaryRepository.findByDate(date);
            return new SummaryResponse(
                    transactionSummary.getDate(),
                    transactionSummary.getSuccessfulTransactions(),
                    transactionSummary.getFailedTransactions(),
                    transactionSummary.getInsufficientFundsTransactions(),
                    transactionSummary.getTotalAmount(),
                    transactionSummary.getTotalFees(),
                    transactionSummary.getTotalCommissions()
            );

        }catch (Exception e){
            logger.error("Exception:" + e.getMessage());
            logger.debug("An error has occurred: "+ e.getCause() );
            throw new DatabaseException("An error has occurred: " +e.getMessage());
        }

    }


    @Override
    public void generateDailyTransactionSummary(LocalDate date) {


        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        try{
            List<Transaction> successfulTransactions = transactionRepository.findByDateCreatedBetweenAndStatus(
                    startOfDay, endOfDay, TransactionStatus.SUCCESSFUL);

            List<Transaction> failedTransactions = transactionRepository.findByDateCreatedBetweenAndStatus(
                    startOfDay, endOfDay, TransactionStatus.FAILED);

            List<Transaction> insufficientFundsTransactions = transactionRepository.findByDateCreatedBetweenAndStatus(
                    startOfDay, endOfDay, TransactionStatus.INSUFFICIENT_FUNDS);

            BigDecimal totalAmount = successfulTransactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalFees = successfulTransactions.stream()
                    .map(Transaction::getTransactionFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCommissions = successfulTransactions.stream()
                    .filter(Transaction::isCommissionWorthy)
                    .map(Transaction::getCommission)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            TransactionSummary transactionSummary = TransactionSummary.builder()
                    .failedTransactions(failedTransactions.size())
                    .insufficientFundsTransactions(insufficientFundsTransactions.size())
                    .successfulTransactions(successfulTransactions.size())
                    .totalFees(totalFees)
                    .date(date)
                    .totalAmount(totalAmount)
                    .totalCommissions(totalCommissions)
                    .build();

            transactionSummaryRepository.save(transactionSummary);
        }catch (Exception e){
            logger.error("Exception:" + e.getMessage());
            logger.debug("An error has occurred: "+ e.getCause() );
            throw new DatabaseException("An error has occurred: " +e.getMessage());
        }

    }


    public static String generateUniqueTransRef() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");
        return "TR" +now.format(formatter);
    }



    private void validatingTransferRequest(TransferRequest request){

        if(request.getSourceAccountNumber().trim().length() != 10)
            throw new InvalidRequestException("Invalid source account number");

        if(request.getDestinationAccountNumber().trim().length() != 10)
            throw new InvalidRequestException("Invalid destination account number");

        if(isNullOrEmpty(request.getSourceAccountNumber()))
            throw new InvalidRequestException("Source account number cannot be empty or null");

        if(isNullOrEmpty(request.getDestinationAccountNumber()))
            throw new InvalidRequestException("Destination account number cannot be empty or null");

        if(request.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidRequestException("Amount cannot be lesser than zero or zero");
    }


    private boolean isNullOrEmpty(String value) {
        return value == null || value.isBlank() || value.trim().isEmpty();
    }

}
