package com.firefly.common.application.controller;

import com.firefly.common.application.context.AppConfig;
import com.firefly.common.application.context.AppContext;
import com.firefly.common.application.context.ApplicationExecutionContext;
import com.firefly.common.application.resolver.ConfigResolver;
import com.firefly.common.application.resolver.ContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AbstractContractController Tests")
@ExtendWith(MockitoExtension.class)
class AbstractContractControllerTest {
    
    @Mock
    private ContextResolver contextResolver;
    
    @Mock
    private ConfigResolver configResolver;
    
    @Mock
    private ServerWebExchange exchange;
    
    private TestContractController controller;
    
    private UUID testPartyId;
    private UUID testTenantId;
    private UUID testContractId;
    
    @BeforeEach
    void setUp() {
        controller = new TestContractController();
        ReflectionTestUtils.setField(controller, "contextResolver", contextResolver);
        ReflectionTestUtils.setField(controller, "configResolver", configResolver);
        
        testPartyId = UUID.randomUUID();
        testTenantId = UUID.randomUUID();
        testContractId = UUID.randomUUID();
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
    
    @Test
    @DisplayName("Should resolve contract-scoped context")
    void shouldResolveContractScopedContext() {
        // Given
        AppContext appContext = AppContext.builder()
                .partyId(testPartyId)
                .tenantId(testTenantId)
                .contractId(testContractId)
                .productId(null)  // No product for contract-only
                .roles(Set.of("account:viewer"))
                .permissions(Set.of("account:read"))
                .build();
        
        AppConfig appConfig = AppConfig.builder()
                .tenantId(testTenantId)
                .tenantName("Test Tenant")
                .build();
        
        when(contextResolver.resolveContext(any(ServerWebExchange.class), eq(testContractId), isNull()))
                .thenReturn(Mono.just(appContext));
        when(configResolver.resolveConfig(testTenantId))
                .thenReturn(Mono.just(appConfig));
        
        // When
        Mono<ApplicationExecutionContext> result = controller.testResolveExecutionContext(exchange, testContractId);
        
        // Then
        StepVerifier.create(result)
                .assertNext(ctx -> {
                    assertThat(ctx).isNotNull();
                    assertThat(ctx.getContext().getPartyId()).isEqualTo(testPartyId);
                    assertThat(ctx.getContext().getTenantId()).isEqualTo(testTenantId);
                    assertThat(ctx.getContext().getContractId()).isEqualTo(testContractId);
                    assertThat(ctx.getContext().getProductId()).isNull();
                })
                .verifyComplete();
        
        verify(contextResolver).resolveContext(eq(exchange), eq(testContractId), isNull());
        verify(configResolver).resolveConfig(testTenantId);
    }
    
    @Test
    @DisplayName("Should throw exception when contract ID is null in context resolution")
    void shouldThrowExceptionWhenContractIdIsNullInContextResolution() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            controller.testResolveExecutionContext(exchange, null).block();
        });
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
        
        public Mono<ApplicationExecutionContext> testResolveExecutionContext(
                ServerWebExchange exchange, UUID contractId) {
            return resolveExecutionContext(exchange, contractId);
        }
    }
}
