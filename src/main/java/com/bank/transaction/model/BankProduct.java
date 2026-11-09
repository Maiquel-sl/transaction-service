package com.bank.transaction.model;

public record BankProduct(
    String id,
    String type,        // BANK_ACCOUNT, CREDIT, CREDIT_CARD
    String subType,     // SAVINGS, CURRENT, FIXED_TERM, PERSONAL_CREDIT, BUSINESS_CREDIT
    String referenceNumber
) {}
