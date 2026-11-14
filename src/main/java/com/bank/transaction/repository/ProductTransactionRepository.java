package com.bank.transaction.repository;

import com.bank.transaction.model.ProductTransaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductTransactionRepository extends ReactiveMongoRepository<ProductTransaction, String> {
    
    Flux<ProductTransaction> findByCustomerIdAndProduct_ReferenceNumber(
            String customerId, 
            String referenceNumber, 
            Sort sort
    );        
    Flux<ProductTransaction> findByCustomerId(String customerId, Sort sort);
}
