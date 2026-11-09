package com.bank.transaction.service;

import com.bank.transaction.model.ProductTransaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductTransactionService {
    Mono<ProductTransaction> registerTransaction(ProductTransaction transaction);
    Flux<ProductTransaction> getProductHistory(String customerId, String referenceNumber);
    Flux<ProductTransaction> getAllCustomerTransactions(String customerId);
}
