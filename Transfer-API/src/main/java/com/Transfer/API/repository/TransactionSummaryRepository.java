package com.Transfer.API.repository;

import com.Transfer.API.models.TransactionSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface TransactionSummaryRepository extends JpaRepository<TransactionSummary,Long> {


    TransactionSummary findByDate(LocalDate date);
}
