package com.firefly.common.application.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AbstractContractController Tests")
class AbstractContractControllerTest {
    
    private TestContractController controller;
    
    @BeforeEach
    void setUp() {
        controller = new TestContractController();
    }
    
    @Test
    @DisplayName("Should validate valid contract ID")
    void shouldValidateValidContractId() {
        UUID contractId = UUID.randomUUID();
        
        assertDoesNotThrow(() -> controller.testRequireContractId(contractId));
    }
    
    @Test
    @DisplayName("Should throw exception for null contract ID")
    void shouldThrowExceptionForNullContractId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.testRequireContractId(null)
        );
        
        assertTrue(exception.getMessage().contains("contractId is required"));
    }
    
    
    @Test
    @DisplayName("Should log operation with contract ID")
    void shouldLogOperationWithContractId() {
        UUID contractId = UUID.randomUUID();
        
        // Should not throw exception
        assertDoesNotThrow(() -> controller.testLogOperation(contractId, "testOperation"));
    }
    
    @Test
    @DisplayName("Should handle null operation name in logging")
    void shouldHandleNullOperationNameInLogging() {
        UUID contractId = UUID.randomUUID();
        
        // Should not throw exception even with null operation
        assertDoesNotThrow(() -> controller.testLogOperation(contractId, null));
    }
    
    /**
     * Concrete test implementation of AbstractContractController
     * to expose protected methods for testing
     */
    static class TestContractController extends AbstractContractController {
        
        public void testRequireContractId(UUID contractId) {
            requireContractId(contractId);
        }
        
        public void testLogOperation(UUID contractId, String operation) {
            logOperation(contractId, operation);
        }
    }
}
