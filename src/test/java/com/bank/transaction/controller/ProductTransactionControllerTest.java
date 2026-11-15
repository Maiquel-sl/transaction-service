package com.bank.transaction.controller;

import com.bank.transaction.exception.GlobalExceptionHandler;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@WebFluxTest(ProductTransactionController.class)
@Import(GlobalExceptionHandler.class)
class ProductTransactionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProductTransactionService service;

    private ProductTransaction mockTx;

    @BeforeEach
    void setUp() {
        mockTx = new ProductTransaction();
        mockTx.setId("db-id-001");
        mockTx.setTransactionId("TXN-9A8B7C6D5E4F");
        mockTx.setCustomerId("cust-12345");
        mockTx.setProduct(new BankProduct(
                "prod-99",
                "BANK_ACCOUNT",
                "SAVINGS",
                "ACC-778899",
                "4558111122223333"
        ));
        mockTx.setOperation(new TransactionOperation(
                "DEBIT_CARD_PAYMENT",
                new BigDecimal("120.50"),
                "PEN",
                "Pago POS Supermercado"
        ));
        mockTx.setAudit(new AuditData("account-service", LocalDateTime.of(2026, 11, 14, 15, 30)));
    }

    @Test
    @DisplayName("POST /api/transactions - Debe registrar una transacción y devolver estado 201 Created")
    void testRegisterTransaction() {
        Mockito.when(service.registerTransaction(any(ProductTransaction.class)))
                .thenReturn(Mono.just(mockTx));

        webTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mockTx)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.transactionId").isEqualTo("TXN-9A8B7C6D5E4F")
                .jsonPath("$.product.cardNumber").isEqualTo("4558111122223333");
    }

    @Test
    @DisplayName("GET /api/transactions/customer/{customerId}/product/{referenceNumber} - Debe retornar historial del producto")
    void testGetProductHistory() {
        Mockito.when(service.getProductHistory("cust-12345", "ACC-778899"))
                .thenReturn(Flux.just(mockTx));

        webTestClient.get()
                .uri("/api/transactions/customer/cust-12345/product/ACC-778899")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].customerId").isEqualTo("cust-12345")
                .jsonPath("$[0].product.referenceNumber").isEqualTo("ACC-778899");
    }

    @Test
    @DisplayName("GET /api/transactions/customer/{customerId} - Debe retornar todas las transacciones del cliente")
    void testGetAllCustomerTransactions() {
        Mockito.when(service.getAllCustomerTransactions("cust-12345"))
                .thenReturn(Flux.just(mockTx));

        webTestClient.get()
                .uri("/api/transactions/customer/cust-12345")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].transactionId").isEqualTo("TXN-9A8B7C6D5E4F");
    }

    @Test
    @DisplayName("GET /api/transactions/reports/product - Debe generar reporte por producto en intervalo de fechas")
    void testGetProductReportByDateRange() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 30, 23, 59, 59);

        Mockito.when(service.getProductReportByDateRange(eq("BANK_ACCOUNT"), eq(start), eq(end)))
                .thenReturn(Flux.just(mockTx));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/transactions/reports/product")
                        .queryParam("productType", "BANK_ACCOUNT")
                        .queryParam("start", "2026-11-01T00:00:00")
                        .queryParam("end", "2026-11-30T23:59:59")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].product.type").isEqualTo("BANK_ACCOUNT")
                .jsonPath("$[0].operation.amount").isEqualTo(120.50);
    }

    @Test
    @DisplayName("GET /api/transactions/reports/card/{cardNumber}/last10 - Debe retornar los últimos 10 movimientos de la tarjeta")
    void testGetLast10CardMovements() {
        String cardNumber = "4558111122223333";

        Mockito.when(service.getLast10CardMovements(cardNumber))
                .thenReturn(Flux.just(mockTx));

        webTestClient.get()
                .uri("/api/transactions/reports/card/{cardNumber}/last10", cardNumber)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].product.cardNumber").isEqualTo(cardNumber)
                .jsonPath("$[0].operation.type").isEqualTo("DEBIT_CARD_PAYMENT");
    }
}
