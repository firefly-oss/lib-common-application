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
 * <h1>Abstract Base Controller for Contract-Scoped Endpoints</h1>
 * 
 * <p>This base class is for controllers that operate on <strong>contract-scoped resources</strong>.
 * It automatically resolves the full application context including party, tenant, and contract.</p>
 * 
 * <h2>When to Use</h2>
 * <p>Extend this class when building REST endpoints that operate on contract-level resources:</p>
 * <ul>
 *   <li><strong>Accounts:</strong> {@code /contracts/{contractId}/accounts}</li>
 *   <li><strong>Beneficiaries:</strong> {@code /contracts/{contractId}/beneficiaries}</li>
 *   <li><strong>Statements:</strong> {@code /contracts/{contractId}/statements}</li>
 *   <li><strong>Settings:</strong> {@code /contracts/{contractId}/settings}</li>
 * </ul>
 * 
 * <h2>Architecture</h2>
 * <p>This controller automatically resolves:</p>
 * <ul>
 *   <li><strong>Party ID:</strong> From Istio header <code>X-Party-Id</code></li>
 *   <li><strong>Tenant ID:</strong> From Istio header <code>X-Tenant-Id</code></li>
 *   <li><strong>Contract ID:</strong> From {@code @PathVariable UUID contractId} in your endpoint</li>
 *   <li><strong>Roles/Permissions:</strong> Enriched from platform SDKs based on party+contract</li>
 *   <li><strong>Tenant Config:</strong> Loaded from configuration service</li>
 * </ul>
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
 *     @Secure(requireParty = true, requireContract = true, requireRole = "account:viewer")
 *     public Mono<List<AccountDto>> listAccounts(
 *             @PathVariable UUID contractId,
 *             ServerWebExchange exchange) {
 *         
 *         // Automatically resolved context with party + tenant + contract
 *         return resolveExecutionContext(exchange, contractId)
 *             .flatMap(context -> accountService.listAccounts(context));
 *     }
 * }
 * }
 * </pre>
 * 
 * <h2>What You Get</h2>
 * <ul>
 *   <li><strong>Automatic Context Resolution:</strong> {@link #resolveExecutionContext(ServerWebExchange, UUID)}</li>
 *   <li><strong>Party + Tenant + Contract:</strong> Full context with roles/permissions for the contract</li>
 *   <li><strong>Validation:</strong> {@link #requireContractId(UUID)} ensures contractId is not null</li>
 *   <li><strong>Full Config Access:</strong> Tenant configuration, feature flags, providers</li>
 *   <li><strong>Security Ready:</strong> Works seamlessly with {@code @Secure} annotations</li>
 * </ul>
 * 
 * @author Firefly Development Team
 * @since 1.0.0
 * @see AbstractPartyController For party-only endpoints
 * @see AbstractProductController For product-scoped endpoints
 */
@Slf4j
public abstract class AbstractContractController {
    
    @Autowired
    private ContextResolver contextResolver;
    
    @Autowired
    private ConfigResolver configResolver;
    
    /**
     * Resolves the full application execution context for contract-scoped endpoints.
     * 
     * <p>This method:</p>
     * <ol>
     *   <li>Validates contractId is not null</li>
     *   <li>Extracts party ID and tenant ID from Istio headers</li>
     *   <li>Uses the provided contractId from {@code @PathVariable}</li>
     *   <li>Enriches with roles and permissions from platform SDKs (party+contract scope)</li>
     *   <li>Loads tenant configuration</li>
     *   <li>Returns complete {@link ApplicationExecutionContext}</li>
     * </ol>
     * 
     * <p><strong>Note:</strong> Product ID will be <code>null</code> since this is contract-only.</p>
     * 
     * @param exchange the server web exchange
     * @param contractId the contract ID from {@code @PathVariable}
     * @return Mono of ApplicationExecutionContext with party, tenant, and contract context
     * @throws IllegalArgumentException if contractId is null
     */
    protected Mono<ApplicationExecutionContext> resolveExecutionContext(
            ServerWebExchange exchange, UUID contractId) {
        
        requireContractId(contractId);
        log.debug("Resolving contract-scoped execution context for contract: {}", contractId);
        
        // Pass contractId explicitly, productId is null for contract-only endpoints
        return contextResolver.resolveContext(exchange, contractId, null)
                .flatMap(appContext -> {
                    log.debug("Resolved contract context: party={}, tenant={}, contract={}", 
                            appContext.getPartyId(), appContext.getTenantId(), appContext.getContractId());
                    
                    return configResolver.resolveConfig(appContext.getTenantId())
                            .map(appConfig -> ApplicationExecutionContext.builder()
                                    .context(appContext)
                                    .config(appConfig)
                                    .build());
                })
                .doOnSuccess(ctx -> log.debug("Successfully resolved contract-scoped execution context"))
                .doOnError(error -> log.error("Failed to resolve contract-scoped execution context", error));
    }
    
    /**
     * Validates that contractId is not null.
     * 
     * <p>Call this method to validate the contractId before using it.
     * This is automatically called by {@link #resolveExecutionContext(ServerWebExchange, UUID)}.</p>
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
     * @param contractId the contract ID
     * @param operation a short description of the operation (e.g., "listAccounts", "createBeneficiary")
     */
    protected final void logOperation(UUID contractId, String operation) {
        log.info("[Contract: {}] Operation: {}", contractId, operation);
    }
}
