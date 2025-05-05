package com.Transfer.API.repository;

import com.Transfer.API.enums.TransactionStatus;
import com.Transfer.API.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findBySourceAccountNumberOrDestinationAccountNumber(String accountNumber, String accountNumber2);
    List<Transaction> findByDateCreatedBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:accountNumber IS NULL OR t.sourceAccountNumber = :accountNumber OR t.destinationAccountNumber = :accountNumber) AND " +
            "(:startDate IS NULL OR t.dateCreated >= :startDate) AND " +
            "(:endDate IS NULL OR t.dateCreated <= :endDate)")
    Page<Transaction> findWithFilters(
            @Param("status") TransactionStatus status,
            @Param("accountNumber") String accountNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    List<Transaction> findByDateCreatedBetweenAndStatus(
            LocalDateTime startOfDay,
            LocalDateTime endOfDay,
            TransactionStatus status);
}
