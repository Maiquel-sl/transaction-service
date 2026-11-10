package com.bank.transaction.controller;

import com.bank.transaction.model.AuditData;
import com.bank.transaction.model.BankProduct;
import com.bank.transaction.model.ProductTransaction;
import com.bank.transaction.model.TransactionOperation;
import com.bank.transaction.service.ProductTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(ProductTransactionController.class)
class ProductTransactionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProductTransactionService service;

    private ProductTransaction mockTx;

    @BeforeEach
    void setUp() {
        mockTx = new ProductTransaction();
        mockTx.setTransactionId("TXN-TEST-12345");
        mockTx.setCustomerId("cust-99");
        mockTx.setProduct(new BankProduct("p-1", "CREDIT_CARD", "PERSONAL_CREDIT", "4557000011112222"));
        mockTx.setOperation(new TransactionOperation("CARD_CONSUMPTION", new BigDecimal("12.50"), "USD", "Coffee Shop"));
        mockTx.setAudit(new AuditData("credit-service", LocalDateTime.now()));
    }

    @Test
    @DisplayName("POST /api/transactions - Should register audit record and return HTTP 201 Created")
    void testRegisterTransactionEndpoint() {
        Mockito.when(service.registerTransaction(any(ProductTransaction.class))).thenReturn(Mono.just(mockTx));

        webTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mockTx)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.transactionId").isEqualTo("TXN-TEST-12345")
                .jsonPath("$.operation.type").isEqualTo("CARD_CONSUMPTION");
    }

    @Test
    @DisplayName("GET /api/transactions/customer/{cId}/product/{pId} - Should stream portfolio history lines")
    void testGetProductHistoryEndpoint() {
        Mockito.when(service.getProductHistory("cust-99", "4557000011112222"))
                .thenReturn(Flux.just(mockTx));

        webTestClient.get()
                .uri("/api/transactions/customer/cust-99/product/4557000011112222")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductTransaction.class)
                .hasSize(1);
    }
}
