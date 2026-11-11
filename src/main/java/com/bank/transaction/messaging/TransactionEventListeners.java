package com.bank.transaction.messaging;

import com.bank.transaction.events.AccountTransactionEvent;
import com.bank.transaction.events.CreditTransactionEvent;
import com.bank.transaction.model.*;
import com.bank.transaction.service.ProductTransactionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.function.Function;

@Configuration
public class TransactionEventListeners {

    //El uso de Function<Flux<T>, Mono<Void>> mantiene el flujo 100% reactivo de extremo a extremo 
    //(aprovechando backpressure nativo de Project Reactor) sin bloquear los hilos consumidores de Kafka

    @Bean
    public Function<Flux<AccountTransactionEvent>, Mono<Void>> accountEventConsumer(ProductTransactionService service) {
        return flux -> flux.flatMap(event -> {
            ProductTransaction tx = new ProductTransaction();
            tx.setCustomerId(event.customerId());
            tx.setProduct(new BankProduct(
                    event.accountId(),
                    "BANK_ACCOUNT",
                    event.accountSubType(),
                    event.accountNumber()
            ));
            tx.setOperation(new TransactionOperation(
                    event.operationType(),
                    event.amount(),
                    event.currency(),
                    event.description()
            ));
            tx.setAudit(new AuditData("account-service", event.timestamp()));

            return service.registerTransaction(tx);
        }).then();
    }

    @Bean
    public Function<Flux<CreditTransactionEvent>, Mono<Void>> creditEventConsumer(ProductTransactionService service) {
        return flux -> flux.flatMap(event -> {
            ProductTransaction tx = new ProductTransaction();
            tx.setCustomerId(event.customerId());
            tx.setProduct(new BankProduct(
                    event.productId(),
                    event.productType(),
                    event.productSubType(),
                    event.productIdentifier()
            ));
            tx.setOperation(new TransactionOperation(
                    event.operationType(),
                    event.amount(),
                    event.currency(),
                    event.description()
            ));
            tx.setAudit(new AuditData("credit-service", event.timestamp()));

            return service.registerTransaction(tx);
        }).then();
    }
}
