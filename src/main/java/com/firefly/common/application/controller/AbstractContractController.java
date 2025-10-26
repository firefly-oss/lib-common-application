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
 * <h1>Abstract Base Controller for Contract-Scoped Endpoints</h1>
 * 
 * <p>This <strong>optional</strong> base class helps you build controllers that follow Firefly's
 * contract-scoping pattern. It provides utility methods for validation and logging,
 * making it easier to maintain consistent API design across microservices.</p>
 * 
 * <h2>When to Use</h2>
 * <p>Extend this class when building REST endpoints that operate on resources scoped to a
 * <strong>contract</strong>. Your endpoints should follow this path pattern:</p>
 * <pre>
 * /api/v1/contracts/{contractId}/...
 * </pre>
 * 
 * <h2>Quick Example</h2>
 * <pre>
 * {@code
 * @RestController
 * @RequestMapping("/api/v1/contracts/{contractId}/accounts")
 * public class AccountController extends AbstractContractController {
 *     
 *     @Autowired
 *     private AccountApplicationService accountService;
 *     
 *     @GetMapping
 *     @Secure(roles = "ACCOUNT_VIEWER")
 *     public Mono<List<AccountDto>> listAccounts(
 *             @PathVariable UUID contractId,
 *             ServerWebExchange exchange) {
 *         // Validate contractId is present
 *         requireContractId(contractId);
 *         
 *         return accountService.listAccountsByContract(exchange, contractId);
 *     }
 * }
 * }
 * </pre>
 * 
 * <h2>What You Get</h2>
 * <ul>
 *   <li><strong>Validation:</strong> {@link #requireContractId(UUID)} ensures contractId is not null</li>
 *   <li><strong>Logging:</strong> {@link #logOperation(UUID, String)} for consistent debug logs</li>
 *   <li><strong>Clarity:</strong> Makes contract-scoping explicit in your controller hierarchy</li>
 * </ul>
 * 
 * <h2>Important Notes</h2>
 * <ul>
 *   <li>This class is <strong>completely optional</strong> - use it only if it helps</li>
 *   <li>It does NOT enforce URL patterns - that's your responsibility</li>
 *   <li>It only provides helper methods for common tasks</li>
 *   <li>You can create your own base controller with your own helpers</li>
 * </ul>
 * 
 * @author Firefly Development Team
 * @since 1.0.0
 * @see AbstractProductController For product-scoped endpoints
 */
@Slf4j
public abstract class AbstractContractController {
    
    /**
     * Validates that contractId is not null.
     * 
     * <p>Call this method at the beginning of your endpoint handlers to ensure
     * the contractId path variable is present. This is a simple null check that
     * throws an exception if the contractId is missing.</p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>
     * {@code
     * @GetMapping
     * public Mono<AccountDto> getAccount(
     *         @PathVariable UUID contractId,
 *         @PathVariable UUID accountId) {
     *     requireContractId(contractId);  // Validates contractId is present
     *     // ... rest of your logic
     * }
     * }
     * </pre>
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
     * Logs the current operation with contract context.
     * 
     * <p>This is a convenience method for adding consistent, structured logging
     * to your endpoints. It logs at INFO level with the contract ID and operation name.</p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>
     * {@code
     * @PostMapping
     * public Mono<AccountDto> createAccount(
     *         @PathVariable UUID contractId,
     *         @RequestBody CreateAccountRequest request) {
     *     requireContractId(contractId);
     *     logOperation(contractId, "createAccount");
     *     // ... rest of your logic
     * }
     * }
     * </pre>
     * 
     * @param contractId the contract ID
     * @param operation a short description of the operation (e.g., "createAccount", "deleteCard")
     */
    protected final void logOperation(UUID contractId, String operation) {
        log.info("[Contract: {}] Operation: {}", contractId, operation);
    }
}
