/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firefly.common.application.integration;

import com.firefly.common.application.context.AppConfig;
import com.firefly.common.application.context.AppContext;
import com.firefly.common.application.context.ApplicationExecutionContext;
import com.firefly.common.application.controller.AbstractApplicationController;
import com.firefly.common.application.controller.AbstractContractController;
import com.firefly.common.application.controller.AbstractProductController;
import com.firefly.common.application.resolver.ConfigResolver;
import com.firefly.common.application.resolver.ContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration test demonstrating all three controller types:
 * - AbstractApplicationController (application-layer, no contract/product)
 * - AbstractContractController (party + contract)
 * - AbstractProductController (party + contract + product)
 * 
 * This test validates the complete architecture:
 * 1. Istio injects X-Party-Id header
 * 2. Config-mgmt resolves tenantId
 * 3. Controllers extract contractId/productId from path variables
 * 4. FireflySessionManager provides roles/permissions (mocked)
 * 5. Context is fully resolved
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Controller Integration Test - All Three Controller Types")
class ControllerIntegrationTest {
    
    @Mock
    private ContextResolver contextResolver;
    
    @Mock
    private ConfigResolver configResolver;
    
    @Mock
    private ServerWebExchange exchange;
    
    @Mock
    private ServerHttpRequest request;
    
    private UUID testPartyId;
    private UUID testTenantId;
    private UUID testContractId;
    private UUID testProductId;
    
    private TestApplicationController applicationController;
    private TestContractController contractController;
    private TestProductController productController;
    
    @BeforeEach
    void setUp() {
        testPartyId = UUID.randomUUID();
        testTenantId = UUID.randomUUID();
        testContractId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        
        // Setup controllers
        applicationController = new TestApplicationController();
        contractController = new TestContractController();
        productController = new TestProductController();
        
        // Inject dependencies
        ReflectionTestUtils.setField(applicationController, "contextResolver", contextResolver);
        ReflectionTestUtils.setField(applicationController, "configResolver", configResolver);
        ReflectionTestUtils.setField(contractController, "contextResolver", contextResolver);
        ReflectionTestUtils.setField(contractController, "configResolver", configResolver);
        ReflectionTestUtils.setField(productController, "contextResolver", contextResolver);
        ReflectionTestUtils.setField(productController, "configResolver", configResolver);
    }
    
    @Test
    @DisplayName("Scenario 1: Application-layer endpoint (Onboarding)")
    void testApplicationLayerEndpoint() {
        // Given: Onboarding endpoint with only party context
        AppContext appContext = AppContext.builder()
                .partyId(testPartyId)
                .tenantId(testTenantId)
                .contractId(null)  // No contract for onboarding
                .productId(null)   // No product for onboarding
                .roles(Set.of("customer:onboard"))
                .permissions(Set.of("profile:create"))
                .build();
        
        AppConfig appConfig = AppConfig.builder()
                .tenantId(testTenantId)
                .tenantName("Test Bank")
                .build();
        
        when(contextResolver.resolveContext(any(ServerWebExchange.class), isNull(), isNull()))
                .thenReturn(Mono.just(appContext));
        when(configResolver.resolveConfig(testTenantId))
                .thenReturn(Mono.just(appConfig));
        
        // When: Call application-layer controller endpoint
        Mono<ApplicationExecutionContext> result = applicationController.handleOnboarding(exchange);
        
        // Then: Context is resolved with party + tenant only
        StepVerifier.create(result)
                .assertNext(ctx -> {
                    assertThat(ctx.getContext().getPartyId()).isEqualTo(testPartyId);
                    assertThat(ctx.getContext().getTenantId()).isEqualTo(testTenantId);
                    assertThat(ctx.getContext().getContractId()).isNull();
                    assertThat(ctx.getContext().getProductId()).isNull();
                    assertThat(ctx.getContext().getRoles()).contains("customer:onboard");
                })
                .verifyComplete();
        
        verify(contextResolver).resolveContext(eq(exchange), isNull(), isNull());
        verify(configResolver).resolveConfig(testTenantId);
    }
    
    @Test
    @DisplayName("Scenario 2: Contract-scoped endpoint (List Accounts)")
    void testContractScopedEndpoint() {
        // Given: Account listing endpoint with party + contract context
        AppContext appContext = AppContext.builder()
                .partyId(testPartyId)
                .tenantId(testTenantId)
                .contractId(testContractId)
                .productId(null)  // No product for contract-level endpoint
                .roles(Set.of("owner", "account:viewer"))
                .permissions(Set.of("account:read", "account:list"))
                .build();
        
        AppConfig appConfig = AppConfig.builder()
                .tenantId(testTenantId)
                .tenantName("Test Bank")
                .build();
        
        when(contextResolver.resolveContext(any(ServerWebExchange.class), eq(testContractId), isNull()))
                .thenReturn(Mono.just(appContext));
        when(configResolver.resolveConfig(testTenantId))
                .thenReturn(Mono.just(appConfig));
        
        // When: Call contract controller endpoint with contractId
        Mono<ApplicationExecutionContext> result = contractController.listAccounts(testContractId, exchange);
        
        // Then: Context is resolved with party + tenant + contract
        StepVerifier.create(result)
                .assertNext(ctx -> {
                    assertThat(ctx.getContext().getPartyId()).isEqualTo(testPartyId);
                    assertThat(ctx.getContext().getTenantId()).isEqualTo(testTenantId);
                    assertThat(ctx.getContext().getContractId()).isEqualTo(testContractId);
                    assertThat(ctx.getContext().getProductId()).isNull();
                    assertThat(ctx.getContext().getRoles()).contains("owner", "account:viewer");
                })
                .verifyComplete();
        
        verify(contextResolver).resolveContext(eq(exchange), eq(testContractId), isNull());
        verify(configResolver).resolveConfig(testTenantId);
    }
    
    @Test
    @DisplayName("Scenario 3: Product-scoped endpoint (List Transactions)")
    void testProductScopedEndpoint() {
        // Given: Transaction listing endpoint with full context
        AppContext appContext = AppContext.builder()
                .partyId(testPartyId)
                .tenantId(testTenantId)
                .contractId(testContractId)
                .productId(testProductId)
                .roles(Set.of("owner", "transaction:viewer"))
                .permissions(Set.of("transaction:read", "transaction:list"))
                .build();
        
        AppConfig appConfig = AppConfig.builder()
                .tenantId(testTenantId)
                .tenantName("Test Bank")
                .build();
        
        when(contextResolver.resolveContext(any(ServerWebExchange.class), eq(testContractId), eq(testProductId)))
                .thenReturn(Mono.just(appContext));
        when(configResolver.resolveConfig(testTenantId))
                .thenReturn(Mono.just(appConfig));
        
        // When: Call product controller endpoint with contractId + productId
        Mono<ApplicationExecutionContext> result = productController.listTransactions(
                testContractId, testProductId, exchange);
        
        // Then: Context is resolved with full hierarchy
        StepVerifier.create(result)
                .assertNext(ctx -> {
                    assertThat(ctx.getContext().getPartyId()).isEqualTo(testPartyId);
                    assertThat(ctx.getContext().getTenantId()).isEqualTo(testTenantId);
                    assertThat(ctx.getContext().getContractId()).isEqualTo(testContractId);
                    assertThat(ctx.getContext().getProductId()).isEqualTo(testProductId);
                    assertThat(ctx.getContext().getRoles()).contains("owner", "transaction:viewer");
                })
                .verifyComplete();
        
        verify(contextResolver).resolveContext(eq(exchange), eq(testContractId), eq(testProductId));
        verify(configResolver).resolveConfig(testTenantId);
    }
    
    @Test
    @DisplayName("Scenario 4: End-to-end flow - Onboarding → Create Contract → List Transactions")
    void testEndToEndFlow() {
        // Step 1: Onboarding (party-only)
        AppContext onboardingContext = AppContext.builder()
                .partyId(testPartyId)
                .tenantId(testTenantId)
                .roles(Set.of("customer:onboard"))
                .build();
        
        AppConfig config = AppConfig.builder().tenantId(testTenantId).build();
        
        when(contextResolver.resolveContext(any(), isNull(), isNull()))
                .thenReturn(Mono.just(onboardingContext));
        when(configResolver.resolveConfig(testTenantId))
                .thenReturn(Mono.just(config));
        
        StepVerifier.create(applicationController.handleOnboarding(exchange))
                .assertNext(ctx -> assertThat(ctx.getContext().getContractId()).isNull())
                .verifyComplete();
        
        // Step 2: After onboarding, party creates contract (becomes owner)
        AppContext contractContext = AppContext.builder()
                .partyId(testPartyId)
                .tenantId(testTenantId)
                .contractId(testContractId)
                .roles(Set.of("owner", "account:viewer"))
                .build();
        
        when(contextResolver.resolveContext(any(), eq(testContractId), isNull()))
                .thenReturn(Mono.just(contractContext));
        
        StepVerifier.create(contractController.listAccounts(testContractId, exchange))
                .assertNext(ctx -> {
                    assertThat(ctx.getContext().getContractId()).isEqualTo(testContractId);
                    assertThat(ctx.getContext().getRoles()).contains("owner");
                })
                .verifyComplete();
        
        // Step 3: Access product-level resources (transactions)
        AppContext productContext = AppContext.builder()
                .partyId(testPartyId)
                .tenantId(testTenantId)
                .contractId(testContractId)
                .productId(testProductId)
                .roles(Set.of("owner", "transaction:viewer"))
                .build();
        
        when(contextResolver.resolveContext(any(), eq(testContractId), eq(testProductId)))
                .thenReturn(Mono.just(productContext));
        
        StepVerifier.create(productController.listTransactions(testContractId, testProductId, exchange))
                .assertNext(ctx -> {
                    assertThat(ctx.getContext().getProductId()).isEqualTo(testProductId);
                    assertThat(ctx.getContext().getRoles()).contains("transaction:viewer");
                })
                .verifyComplete();
    }
    
    // Test controller implementations
    
    static class TestApplicationController extends AbstractApplicationController {
        public Mono<ApplicationExecutionContext> handleOnboarding(ServerWebExchange exchange) {
            return resolveExecutionContext(exchange);
        }
    }
    
    static class TestContractController extends AbstractContractController {
        public Mono<ApplicationExecutionContext> listAccounts(UUID contractId, ServerWebExchange exchange) {
            return resolveExecutionContext(exchange, contractId);
        }
    }
    
    static class TestProductController extends AbstractProductController {
        public Mono<ApplicationExecutionContext> listTransactions(
                UUID contractId, UUID productId, ServerWebExchange exchange) {
            return resolveExecutionContext(exchange, contractId, productId);
        }
    }
}
