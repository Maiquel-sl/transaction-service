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
import org.springframework.data.domain.PageRequest;
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

    private ProductTransaction mockCardTx;
    private ProductTransaction mockAccountTx;

    @BeforeEach
    void setUp() {
        // Transacción ejecutada mediante tarjeta de débito o crédito
        mockCardTx = new ProductTransaction();
        mockCardTx.setId("db-id-001");
        mockCardTx.setCustomerId("cust-12345");
        mockCardTx.setProduct(new BankProduct(
                "prod-99",
                "BANK_ACCOUNT",
                "SAVINGS",
                "ACC-778899",
                "4558111122223333"
        ));
        mockCardTx.setOperation(new TransactionOperation(
                "DEBIT_CARD_PAYMENT",
                new BigDecimal("120.50"),
                "PEN",
                "Pago POS Supermercado"
        ));
        mockCardTx.setAudit(new AuditData("account-service", LocalDateTime.now()));

        // Transacción directa en cuenta (sin tarjeta asociada)
        mockAccountTx = new ProductTransaction();
        mockAccountTx.setId("db-id-002");
        mockAccountTx.setCustomerId("cust-12345");
        mockAccountTx.setProduct(new BankProduct(
                "prod-99",
                "BANK_ACCOUNT",
                "SAVINGS",
                "ACC-778899",
                null
        ));
        mockAccountTx.setOperation(new TransactionOperation(
                "DEPOSIT",
                new BigDecimal("500.00"),
                "PEN",
                "Depósito en ventanilla"
        ));
        mockAccountTx.setAudit(new AuditData("account-service", LocalDateTime.now()));
    }

    @Test
    @DisplayName("Debe registrar la transacción asignando un identificador único con prefijo TXN-")
    void testRegisterTransactionSuccess() {
        Mockito.when(repository.save(any(ProductTransaction.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<ProductTransaction> result = service.registerTransaction(mockCardTx);

        StepVerifier.create(result)
                .expectNextMatches(tx ->
                        tx.getTransactionId() != null &&
                        tx.getTransactionId().startsWith("TXN-") &&
                        "4558111122223333".equals(tx.getProduct().cardNumber())
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar el historial cronológico de un producto específico limpiando espacios")
    void testGetProductHistoryChronological() {
        Mockito.when(repository.findByCustomerIdAndProduct_ReferenceNumber(
                        eq("cust-12345"),
                        eq("ACC-778899"),
                        any(Sort.class)))
                .thenReturn(Flux.just(mockCardTx, mockAccountTx));

        // Se envían espacios intencionales para validar el .trim() del servicio
        Flux<ProductTransaction> result = service.getProductHistory(" cust-12345 ", " ACC-778899 ");

        StepVerifier.create(result)
                .expectNext(mockCardTx)
                .expectNext(mockAccountTx)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar todas las transacciones de un cliente ordenadas cronológicamente")
    void testGetAllCustomerTransactions() {
        Mockito.when(repository.findByCustomerId(eq("cust-12345"), any(Sort.class)))
                .thenReturn(Flux.just(mockCardTx, mockAccountTx));

        Flux<ProductTransaction> result = service.getAllCustomerTransactions(" cust-12345 ");

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Reporte 1: Debe retornar movimientos filtrados por tipo de producto en un intervalo de fechas")
    void testGetProductReportByDateRange() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 30, 23, 59);

        Mockito.when(repository.findByProduct_TypeAndAudit_TimestampBetween(
                        eq("BANK_ACCOUNT"),
                        eq(start),
                        eq(end),
                        any(Sort.class)))
                .thenReturn(Flux.just(mockCardTx, mockAccountTx));

        Flux<ProductTransaction> result = service.getProductReportByDateRange(" BANK_ACCOUNT ", start, end);

        StepVerifier.create(result)
                .expectNextMatches(tx -> "BANK_ACCOUNT".equals(tx.getProduct().type()))
                .expectNextMatches(tx -> "DEPOSIT".equals(tx.getOperation().type()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Reporte 2: Debe consultar los últimos 10 movimientos de una tarjeta de débito o crédito con paginación")
    void testGetLast10CardMovements() {
        String cardNumber = "4558111122223333";
        PageRequest expectedPageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "audit.timestamp"));

        Mockito.when(repository.findByProduct_CardNumber(
                        eq(cardNumber),
                        eq(expectedPageRequest)))
                .thenReturn(Flux.just(mockCardTx));

        Flux<ProductTransaction> result = service.getLast10CardMovements(" " + cardNumber + " ");

        StepVerifier.create(result)
                .expectNextMatches(tx ->
                        cardNumber.equals(tx.getProduct().cardNumber()) &&
                        "DEBIT_CARD_PAYMENT".equals(tx.getOperation().type())
                )
                .verifyComplete();
    }
}
