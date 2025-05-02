package com.Transfer.API.data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferRequest {

    @NotBlank(message = "Source account number not valid")
    @Size(min = 10, max = 10, message = "Must be exactly 10 characters")
    String sourceAccountNumber;

    @NotBlank(message = "Destination account number not valid")
    @Size(min = 10, max = 10, message = "Must be exactly 10 characters")
    String destinationAccountNumber;

    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    BigDecimal amount;

    String description;
}
