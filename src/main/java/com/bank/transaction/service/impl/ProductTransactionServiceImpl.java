package com.bank.transaction.service.impl;

import com.bank.transaction.model.ProductTransaction;
import com.bank.transaction.repository.ProductTransactionRepository;
import com.bank.transaction.service.ProductTransactionService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
public class ProductTransactionServiceImpl implements ProductTransactionService {

    private final ProductTransactionRepository repository;

    public ProductTransactionServiceImpl(ProductTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<ProductTransaction> registerTransaction(ProductTransaction transaction) {
        transaction.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        return repository.save(transaction);
    }

    @Override
    public Flux<ProductTransaction> getProductHistory(String customerId, String referenceNumber) {
        return repository.findByCustomerIdAndProductReferenceNumberOrderByAuditTimestampDesc(customerId, referenceNumber);
    }

    @Override
    public Flux<ProductTransaction> getAllCustomerTransactions(String customerId) {
        return repository.findByCustomerId(customerId);
    }
}
