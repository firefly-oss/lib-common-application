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

package com.firefly.common.application.controller;

import com.firefly.common.application.context.ApplicationExecutionContext;
import com.firefly.common.application.resolver.ContextResolver;
import com.firefly.common.application.resolver.ConfigResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * <h1>Abstract Base Controller for Product-Scoped Endpoints</h1>
 * 
 * <p>This base class is for controllers that operate on <strong>product-scoped resources</strong>
 * within a contract. It automatically resolves the full application context including party, 
 * tenant, contract, and product.</p>
 * 
 * <h2>When to Use</h2>
 * <p>Extend this class when building REST endpoints that operate on product-level resources:</p>
 * <ul>
 *   <li><strong>Transactions:</strong> {@code /contracts/{contractId}/products/{productId}/transactions}</li>
 *   <li><strong>Balances:</strong> {@code /contracts/{contractId}/products/{productId}/balances}</li>
 *   <li><strong>Cards:</strong> {@code /contracts/{contractId}/products/{productId}/cards}</li>
 *   <li><strong>Limits:</strong> {@code /contracts/{contractId}/products/{productId}/limits}</li>
 * </ul>
 * 
 * <h2>Architecture</h2>
 * <p>This controller automatically resolves:</p>
 * <ul>
 *   <li><strong>Party ID:</strong> From Istio header <code>X-Party-Id</code></li>
 *   <li><strong>Tenant ID:</strong> From Istio header <code>X-Tenant-Id</code></li>
 *   <li><strong>Contract ID:</strong> From {@code @PathVariable UUID contractId} in your endpoint</li>
 *   <li><strong>Product ID:</strong> From {@code @PathVariable UUID productId} in your endpoint</li>
 *   <li><strong>Roles/Permissions:</strong> Enriched from platform SDKs based on party+contract+product</li>
 *   <li><strong>Tenant Config:</strong> Loaded from configuration service</li>
 * </ul>
 * 
 * <h2>Quick Example</h2>
 * <pre>
 * {@code
 * @RestController
 * @RequestMapping("/api/v1/contracts/{contractId}/products/{productId}/transactions")
 * public class TransactionController extends AbstractProductController {
 *     
 *     @Autowired
 *     private TransactionApplicationService transactionService;
 *     
 *     @GetMapping
 *     @Secure(requireParty = true, requireContract = true, requireProduct = true, requireRole = "transaction:viewer")
 *     public Mono<List<TransactionDto>> listTransactions(
 *             @PathVariable UUID contractId,
 *             @PathVariable UUID productId,
 *             ServerWebExchange exchange) {
 *         
 *         // Automatically resolved context with party + tenant + contract + product
 *         return resolveExecutionContext(exchange, contractId, productId)
 *             .flatMap(context -> transactionService.listTransactions(context));
 *     }
 * }
 * }
 * </pre>
 * 
 * <h2>What You Get</h2>
 * <ul>
 *   <li><strong>Automatic Context Resolution:</strong> {@link #resolveExecutionContext(ServerWebExchange, UUID, UUID)}</li>
 *   <li><strong>Party + Tenant + Contract + Product:</strong> Full context with complete hierarchy</li>
 *   <li><strong>Validation:</strong> {@link #requireContext(UUID, UUID)} ensures both IDs are not null</li>
 *   <li><strong>Full Config Access:</strong> Tenant configuration, feature flags, providers</li>
 *   <li><strong>Security Ready:</strong> Works seamlessly with {@code @Secure} annotations</li>
 * </ul>
 * 
 * @author Firefly Development Team
 * @since 1.0.0
 * @see AbstractApplicationController For application-layer endpoints
 * @see AbstractContractController For contract-only scoped endpoints
 */
@Slf4j
public abstract class AbstractProductController {
    
    @Autowired
    private ContextResolver contextResolver;
    
    @Autowired
    private ConfigResolver configResolver;
    
    /**
     * Resolves the full application execution context for product-scoped endpoints.
     * 
     * <p>This method:</p>
     * <ol>
     *   <li>Validates contractId and productId are not null</li>
     *   <li>Extracts party ID and tenant ID from Istio headers</li>
     *   <li>Uses the provided contractId and productId from {@code @PathVariable}</li>
     *   <li>Enriches with roles and permissions from platform SDKs (party+contract+product scope)</li>
     *   <li>Loads tenant configuration</li>
     *   <li>Returns complete {@link ApplicationExecutionContext}</li>
     * </ol>
     * 
     * @param exchange the server web exchange
     * @param contractId the contract ID from {@code @PathVariable}
     * @param productId the product ID from {@code @PathVariable}
     * @return Mono of ApplicationExecutionContext with party, tenant, contract, and product context
     * @throws IllegalArgumentException if contractId or productId is null
     */
    protected Mono<ApplicationExecutionContext> resolveExecutionContext(
            ServerWebExchange exchange, UUID contractId, UUID productId) {
        
        requireContext(contractId, productId);
        log.debug("Resolving product-scoped execution context for contract: {}, product: {}", 
                contractId, productId);
        
        // Pass both contractId and productId explicitly
        return contextResolver.resolveContext(exchange, contractId, productId)
                .flatMap(appContext -> {
                    log.debug("Resolved product context: party={}, tenant={}, contract={}, product={}", 
                            appContext.getPartyId(), appContext.getTenantId(), 
                            appContext.getContractId(), appContext.getProductId());
                    
                    return configResolver.resolveConfig(appContext.getTenantId())
                            .map(appConfig -> ApplicationExecutionContext.builder()
                                    .context(appContext)
                                    .config(appConfig)
                                    .build());
                })
                .doOnSuccess(ctx -> log.debug("Successfully resolved product-scoped execution context"))
                .doOnError(error -> log.error("Failed to resolve product-scoped execution context", error));
    }
    
    /**
     * Validates that both contractId and productId are not null.
     * 
     * <p>Call this method at the beginning of your endpoint handlers to ensure
     * both path variables are present. This validates the full hierarchical context.</p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>
     * {@code
     * @GetMapping
     * public Mono<TransactionDto> getTransaction(
     *         @PathVariable UUID contractId,
     *         @PathVariable UUID productId,
     *         @PathVariable UUID transactionId) {
     *     requireContext(contractId, productId);  // Validates both IDs
     *     // ... rest of your logic
     * }
     * }
     * </pre>
     * 
     * @param contractId the contract ID from the path variable
     * @param productId the product ID from the path variable
     * @throws IllegalArgumentException if contractId or productId is null
     */
    protected final void requireContext(UUID contractId, UUID productId) {
        if (contractId == null) {
            log.error("Missing required path variable: contractId");
            throw new IllegalArgumentException(
                "contractId is required but was null. Check your @PathVariable mapping."
            );
        }
        if (productId == null) {
            log.error("Missing required path variable: productId");
            throw new IllegalArgumentException(
                "productId is required but was null. Check your @PathVariable mapping."
            );
        }
        log.trace("Context validated - Contract: {}, Product: {}", contractId, productId);
    }
    
    /**
     * Validates that contractId is not null.
     * 
     * <p>Use this when you only need to validate the contract ID (for example, in
     * endpoints that list all products for a contract).</p>
     * 
     * @param contractId the contract ID from the path variable
     * @throws IllegalArgumentException if contractId is null
     */
    protected final void requireContractId(UUID contractId) {
        if (contractId == null) {
            log.error("Missing required path variable: contractId");
            throw new IllegalArgumentException(
                "contractId is required but was null. Check your @PathVariable mapping."
            );
        }
        log.trace("Contract ID validated: {}", contractId);
    }
    
    /**
     * Validates that productId is not null.
     * 
     * <p>Use this when you only need to validate the product ID.</p>
     * 
     * @param productId the product ID from the path variable
     * @throws IllegalArgumentException if productId is null
     */
    protected final void requireProductId(UUID productId) {
        if (productId == null) {
            log.error("Missing required path variable: productId");
            throw new IllegalArgumentException(
                "productId is required but was null. Check your @PathVariable mapping."
            );
        }
        log.trace("Product ID validated: {}", productId);
    }
    
    /**
     * Logs the current operation with full product context.
     * 
     * <p>This is a convenience method for adding consistent, structured logging
     * to your endpoints. It logs at INFO level with both contract and product IDs.</p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>
     * {@code
     * @PostMapping
     * public Mono<TransactionDto> createTransaction(
     *         @PathVariable UUID contractId,
     *         @PathVariable UUID productId,
     *         @RequestBody CreateTransactionRequest request) {
     *     requireContext(contractId, productId);
     *     logOperation(contractId, productId, "createTransaction");
     *     // ... rest of your logic
     * }
     * }
     * </pre>
     * 
     * @param contractId the contract ID
     * @param productId the product ID
     * @param operation a short description of the operation (e.g., "createTransaction", "deleteCard")
     */
    protected final void logOperation(UUID contractId, UUID productId, String operation) {
        log.info("[Contract: {}, Product: {}] Operation: {}", contractId, productId, operation);
    }
}
