package com.Transfer.API.models;

import com.Transfer.API.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String transactionReference;
    BigDecimal amount;
    BigDecimal transactionFee;
    BigDecimal billedAmount;
    String description;

    @CreationTimestamp
    LocalDateTime dateCreated;
    @UpdateTimestamp
    LocalDateTime dateUpdated;

    @Enumerated(EnumType.STRING)
    TransactionStatus status;

    String statusMessage;
    boolean commissionWorthy;
    BigDecimal commission;
    String sourceAccountNumber;
    String destinationAccountNumber;
}
