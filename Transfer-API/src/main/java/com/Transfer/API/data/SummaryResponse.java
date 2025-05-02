package com.Transfer.API.data;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SummaryResponse {
    LocalDate date;
    int successfulTransactions;
    int failedTransactions;
    int insufficientFundsTransactions;
    BigDecimal totalAmount;
    BigDecimal totalFees;
    BigDecimal totalCommissions;
}
