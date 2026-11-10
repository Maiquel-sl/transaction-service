package com.bank.transaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/transaction-service-db"
})
class TransactionServiceApplicationTests {

    @Test
    @DisplayName("Should verify that the transaction-service application context hooks up cleanly")
    void contextLoads() {
        // Assures no bean conflicts or reactive dependency topology crashes occur on startup.
    }
}
