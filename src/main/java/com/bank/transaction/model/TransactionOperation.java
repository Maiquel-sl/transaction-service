package com.bank.transaction.model;

import java.math.BigDecimal;

public record TransactionOperation(
    String type,        // DEPOSIT, WITHDRAWAL, CREDIT_PAYMENT, CARD_CONSUMPTION, CARD_PAYMENT
    BigDecimal amount,
    String currency,
    String description
) {}
