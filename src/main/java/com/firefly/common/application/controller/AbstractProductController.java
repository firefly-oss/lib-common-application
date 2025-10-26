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

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * <h1>Abstract Base Controller for Product-Scoped Endpoints</h1>
 * 
 * <p>This <strong>optional</strong> base class helps you build controllers that follow Firefly's
 * hierarchical contract + product scoping pattern. It provides utility methods for validation
 * and logging, making it easier to work with nested resources.</p>
 * 
 * <h2>When to Use</h2>
 * <p>Extend this class when building REST endpoints that operate on resources scoped to a
 * <strong>product within a contract</strong>. Your endpoints should follow this path pattern:</p>
 * <pre>
 * /api/v1/contracts/{contractId}/products/{productId}/...
 * </pre>
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
 *     @Secure(roles = "TRANSACTION_VIEWER")
 *     public Mono<List<TransactionDto>> listTransactions(
 *             @PathVariable UUID contractId,
 *             @PathVariable UUID productId,
 *             ServerWebExchange exchange) {
 *         // Validate both IDs are present
 *         requireContext(contractId, productId);
 *         
 *         return transactionService.listTransactions(exchange, contractId, productId);
 *     }
 * }
 * }
 * </pre>
 * 
 * <h2>What You Get</h2>
 * <ul>
 *   <li><strong>Context Validation:</strong> {@link #requireContext(UUID, UUID)} validates both IDs</li>
 *   <li><strong>Individual Validation:</strong> {@link #requireContractId(UUID)} and {@link #requireProductId(UUID)}</li>
 *   <li><strong>Logging:</strong> {@link #logOperation(UUID, UUID, String)} for consistent debug logs</li>
 *   <li><strong>Hierarchy Clarity:</strong> Makes contract-product relationship explicit</li>
 * </ul>
 * 
 * <h2>Important Notes</h2>
 * <ul>
 *   <li>This class is <strong>completely optional</strong> - use it only if it helps</li>
 *   <li>It does NOT enforce URL patterns - that's your responsibility</li>
 *   <li>It only provides helper methods for common validation tasks</li>
 *   <li>You can create your own base controller with your own helpers</li>
 * </ul>
 * 
 * @author Firefly Development Team
 * @since 1.0.0
 * @see AbstractContractController For contract-only scoped endpoints
 */
@Slf4j
public abstract class AbstractProductController {
    
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
