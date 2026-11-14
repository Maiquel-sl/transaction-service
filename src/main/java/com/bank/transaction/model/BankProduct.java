package com.bank.transaction.model;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

public record BankProduct(
    @Field(targetType = FieldType.OBJECT_ID)
    String id,
    String type,        // BANK_ACCOUNT, CREDIT, CREDIT_CARD
    String subType,     // SAVINGS, CURRENT, FIXED_TERM, PERSONAL_CREDIT, BUSINESS_CREDIT
    String referenceNumber
) {}
