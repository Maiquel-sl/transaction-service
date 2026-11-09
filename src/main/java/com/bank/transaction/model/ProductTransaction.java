package com.bank.transaction.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product_transactions")
public class ProductTransaction {
    @Id
    private String id;
    private String transactionId;
    private String customerId;
    private BankProduct product;
    private TransactionOperation operation;
    private AuditData audit;

    // Getters y Setters de Java 27
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public BankProduct getProduct() { return product; }
    public void setProduct(BankProduct product) { this.product = product; }
    public TransactionOperation getOperation() { return operation; }
    public void setOperation(TransactionOperation operation) { this.operation = operation; }
    public AuditData getAudit() { return audit; }
    public void setAudit(AuditData audit) { this.audit = audit; }
}
