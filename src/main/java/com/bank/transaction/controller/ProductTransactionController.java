package com.bank.transaction.controller;

import com.bank.transaction.model.ProductTransaction;
import com.bank.transaction.service.ProductTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/transactions")
public class ProductTransactionController {

    private final ProductTransactionService service;

    public ProductTransactionController(ProductTransactionService service) {
        this.service = service;
    }

    // Ingesta masiva o registro de transacciones
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductTransaction> registerTransaction(@RequestBody ProductTransaction transaction) {
        return service.registerTransaction(transaction);
    }

    // Consulta específica de movimientos por producto y cliente
    @GetMapping("/customer/{customerId}/product/{referenceNumber}")
    public Flux<ProductTransaction> getProductHistory(
            @PathVariable String customerId,
            @PathVariable String referenceNumber) {
        return service.getProductHistory(customerId, referenceNumber);
    }

    // Consulta global de auditoría por cliente
    @GetMapping("/customer/{customerId}")
    public Flux<ProductTransaction> getAllCustomerTransactions(@PathVariable String customerId) {
        return service.getAllCustomerTransactions(customerId);
    }
}
