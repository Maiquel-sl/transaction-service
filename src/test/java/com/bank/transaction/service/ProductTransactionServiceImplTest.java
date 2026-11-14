package com.bank.transaction.service;

import com.bank.transaction.model.AuditData;
import com.bank.transaction.model.BankProduct;
import com.bank.transaction.model.ProductTransaction;
import com.bank.transaction.model.TransactionOperation;
import com.bank.transaction.repository.ProductTransactionRepository;
import com.bank.transaction.service.impl.ProductTransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ProductTransactionServiceImplTest {

    @Mock
    private ProductTransactionRepository repository;

    @InjectMocks
    private ProductTransactionServiceImpl service;

    private ProductTransaction mockTx;

    @BeforeEach
    void setUp() {
        mockTx = new ProductTransaction();
        mockTx.setId("db-id-001");
        mockTx.setCustomerId("cust-12345");
        mockTx.setProduct(new BankProduct("prod-99", "BANK_ACCOUNT", "SAVINGS", "ACC-778899"));
        mockTx.setOperation(new TransactionOperation("WITHDRAWAL", new BigDecimal("50.00"), "PEN", "ATM cash out"));
        mockTx.setAudit(new AuditData("account-service", LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should successfully ingest and tag transaction with a systemic key")
    void testRegisterTransactionSuccess() {
        // Arrange
        Mockito.when(repository.save(any(ProductTransaction.class))).thenReturn(Mono.just(mockTx));

        // Act
        Mono<ProductTransaction> result = service.registerTransaction(mockTx);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(tx ->
                        tx.getTransactionId() != null &&
                        tx.getTransactionId().startsWith("TXN-") &&
                        "cust-12345".equals(tx.getCustomerId())
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return a descending chronological stream of a specific product history")
    void testGetProductHistoryChronological() {
        // Arrange
        Mockito.when(repository.findByCustomerIdAndProduct_ReferenceNumber(
                        eq("cust-12345"),
                        eq("ACC-778899"),
                        any(Sort.class)))
                .thenReturn(Flux.just(mockTx));

        // Act
        Flux<ProductTransaction> result = service.getProductHistory("cust-12345", "ACC-778899");

        // Assert
        StepVerifier.create(result)
                .expectNext(mockTx)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return all transactions belonging to a customer ordered chronologically")
    void testGetAllCustomerTransactions() {
        // Arrange
        Mockito.when(repository.findByCustomerId(eq("cust-12345"), any(Sort.class)))
                .thenReturn(Flux.just(mockTx));

        // Act
        Flux<ProductTransaction> result = service.getAllCustomerTransactions("cust-12345");

        // Assert
        StepVerifier.create(result)
                .expectNext(mockTx)
                .verifyComplete();
    }
}
