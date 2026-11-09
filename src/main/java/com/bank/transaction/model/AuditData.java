package com.bank.transaction.model;

import java.time.LocalDateTime;

public record AuditData(
    String sourceService, // account-service, credit-service
    LocalDateTime timestamp
) {}
