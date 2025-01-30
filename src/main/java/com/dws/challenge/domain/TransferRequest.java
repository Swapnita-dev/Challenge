package com.dws.challenge.domain;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransferRequest {

  @NotNull
  @NotEmpty
  private final String accountFromId;

  @NotNull
  @NotEmpty
  private final String accountToId;

  @NotNull
  private BigDecimal amount;

}
