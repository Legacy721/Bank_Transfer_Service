package com.Transfer.API.data;

import com.Transfer.API.enums.TransactionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionFilter {

    TransactionStatus status;
    String accountNumber;
    LocalDateTime startDate;
    LocalDateTime endDate;
}
