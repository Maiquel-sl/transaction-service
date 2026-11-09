package com.bank.transaction.repository;

import com.bank.transaction.model.ProductTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductTransactionRepository extends ReactiveMongoRepository<ProductTransaction, String> {
    
    // Resuelve la consulta indexada ordenada por el timestamp más reciente
    Flux<ProductTransaction> findByCustomerIdAndProductReferenceNumberOrderByAuditTimestampDesc(
            String customerId, String referenceNumber);
            
    Flux<ProductTransaction> findByCustomerId(String customerId);
}
