package com.firefly.common.application.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AbstractProductController Tests")
class AbstractProductControllerTest {
    
    private TestProductController controller;
    
    @BeforeEach
    void setUp() {
        controller = new TestProductController();
    }
    
    @Test
    @DisplayName("Should validate valid product ID")
    void shouldValidateValidProductId() {
        UUID productId = UUID.randomUUID();
        
        assertDoesNotThrow(() -> controller.testRequireProductId(productId));
    }
    
    @Test
    @DisplayName("Should throw exception for null product ID")
    void shouldThrowExceptionForNullProductId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.testRequireProductId(null)
        );
        
        assertTrue(exception.getMessage().contains("productId is required"));
    }
    
    @Test
    @DisplayName("Should log operation with product ID")
    void shouldLogOperationWithProductId() {
        UUID contractId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        
        assertDoesNotThrow(() -> controller.testLogOperation(contractId, productId, "testOperation"));
    }
    
    @Test
    @DisplayName("Should handle null operation name in logging")
    void shouldHandleNullOperationNameInLogging() {
        UUID contractId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        
        assertDoesNotThrow(() -> controller.testLogOperation(contractId, productId, null));
    }
    
    /**
     * Concrete test implementation of AbstractProductController
     * to expose protected methods for testing
     */
    static class TestProductController extends AbstractProductController {
        
        public void testRequireProductId(UUID productId) {
            requireProductId(productId);
        }
        
        public void testLogOperation(UUID contractId, UUID productId, String operation) {
            logOperation(contractId, productId, operation);
        }
    }
}
