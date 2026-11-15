package com.bank.transaction.service.impl;

import com.bank.transaction.model.ProductTransaction;
import com.bank.transaction.repository.ProductTransactionRepository;
import com.bank.transaction.service.ProductTransactionService;
import com.bank.transaction.util.ReactiveFallbackHandler;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class ProductTransactionServiceImpl implements ProductTransactionService {

    private final ProductTransactionRepository repository;
    private static final String AUDIT_TIMESTAMP_PROPERTY = "audit.timestamp";

    public ProductTransactionServiceImpl(ProductTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericMonoFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Mono<ProductTransaction> registerTransaction(ProductTransaction transaction) {
        transaction.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        return repository.save(transaction);
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericFluxFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Flux<ProductTransaction> getProductHistory(String customerId, String referenceNumber) {
        Sort sortByLatest = Sort.by(Sort.Direction.DESC, AUDIT_TIMESTAMP_PROPERTY);
        return repository.findByCustomerIdAndProduct_ReferenceNumber(
                customerId.trim(),
                referenceNumber.trim(),
                sortByLatest);
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericFluxFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Flux<ProductTransaction> getAllCustomerTransactions(String customerId) {
        Sort sortByLatest = Sort.by(Sort.Direction.DESC, AUDIT_TIMESTAMP_PROPERTY);
        return repository.findByCustomerId(customerId.trim(), sortByLatest);
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericFluxFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Flux<ProductTransaction> getProductReportByDateRange(String productType, LocalDateTime start,
            LocalDateTime end) {
        return repository.findByProduct_TypeAndAudit_TimestampBetween(
                productType.trim(), start, end, Sort.by(Sort.Direction.DESC, AUDIT_TIMESTAMP_PROPERTY));
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericFluxFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Flux<ProductTransaction> getLast10CardMovements(String cardNumber) {
        PageRequest top10 = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, AUDIT_TIMESTAMP_PROPERTY));
        return repository.findByProduct_CardNumber(cardNumber.trim(), top10);
    }

    // =========================================================================
    // MÉTODOS FALLBACK GENÉRICOS PARA TODA LA CLASE
    // =========================================================================

    @SuppressWarnings("unused")
    private <T> Mono<T> genericMonoFallback(Throwable ex) {
        return ReactiveFallbackHandler.handleMonoFallback("TransactionService", ex);
    }

    @SuppressWarnings("unused")
    private <T> Flux<T> genericFluxFallback(Throwable ex) {
        return ReactiveFallbackHandler.handleFluxFallback("TransactionService", ex);
    }
}
