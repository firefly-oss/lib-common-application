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

package com.firefly.common.application.resolver;

import com.firefly.common.application.context.AppContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;

/**
 * Default implementation of ContextResolver.
 * 
 * <p><strong>This is provided by the library - microservices don't need to implement anything.</strong></p>
 * 
 * <p>This resolver automatically:</p>
 * <ul>
 *   <li>Extracts party/tenant/contract/product IDs from JWT tokens</li>
 *   <li>Falls back to HTTP headers if JWT claims are not present</li>
 *   <li>Enriches context with roles and permissions from platform SDKs</li>
 *   <li>Caches results for performance</li>
 * </ul>
 * 
 * <h2>What Microservices Need to Do</h2>
 * <p><strong>Nothing.</strong> This works automatically once you add the library dependency.</p>
 * 
 * <h2>JWT Token Expected Claims</h2>
 * <pre>
 * {
 *   "sub": "party-uuid",           // Maps to partyId
 *   "tenantId": "tenant-uuid",     // Maps to tenantId
 *   "contractId": "contract-uuid", // Optional
 *   "productId": "product-uuid"    // Optional
 * }
 * </pre>
 * 
 * <h2>HTTP Header Fallbacks</h2>
 * <ul>
 *   <li><code>X-Party-Id</code> - Party UUID</li>
 *   <li><code>X-Tenant-Id</code> - Tenant UUID</li>
 *   <li><code>X-Contract-Id</code> - Contract UUID (optional)</li>
 *   <li><code>X-Product-Id</code> - Product UUID (optional)</li>
 * </ul>
 * 
 * @author Firefly Development Team
 * @since 1.0.0
 */
@Component
@Slf4j
public class DefaultContextResolver extends AbstractContextResolver {
    
    // TODO: Inject platform SDK clients when available
    // private final CustomerManagementClient customerMgmtClient;
    // private final ContractManagementClient contractMgmtClient;
    // private final ProductManagementClient productMgmtClient;
    
    @Override
    public Mono<UUID> resolvePartyId(ServerWebExchange exchange) {
        log.debug("Resolving party ID from request");
        
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .filter(auth -> auth.getPrincipal() instanceof Jwt)
                .map(auth -> (Jwt) auth.getPrincipal())
                .flatMap(jwt -> {
                    // Try "sub" claim first (standard JWT subject)
                    String subject = jwt.getSubject();
                    if (subject != null && !subject.isEmpty()) {
                        try {
                            return Mono.just(UUID.fromString(subject));
                        } catch (IllegalArgumentException e) {
                            log.debug("JWT subject is not a valid UUID: {}", subject);
                        }
                    }
                    
                    // Try explicit "partyId" claim
                    String partyId = jwt.getClaimAsString("partyId");
                    if (partyId != null && !partyId.isEmpty()) {
                        try {
                            return Mono.just(UUID.fromString(partyId));
                        } catch (IllegalArgumentException e) {
                            log.debug("JWT partyId claim is not a valid UUID: {}", partyId);
                        }
                    }
                    
                    return Mono.empty();
                })
                // Fallback to header
                .switchIfEmpty(extractUUID(exchange, "partyId", "X-Party-Id"))
                .doOnNext(id -> log.debug("Resolved party ID: {}", id))
                .doOnError(error -> log.error("Failed to resolve party ID", error));
    }
    
    @Override
    public Mono<UUID> resolveTenantId(ServerWebExchange exchange) {
        log.debug("Resolving tenant ID from request");
        
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .filter(auth -> auth.getPrincipal() instanceof Jwt)
                .map(auth -> (Jwt) auth.getPrincipal())
                .flatMap(jwt -> {
                    String tenantId = jwt.getClaimAsString("tenantId");
                    if (tenantId != null && !tenantId.isEmpty()) {
                        try {
                            return Mono.just(UUID.fromString(tenantId));
                        } catch (IllegalArgumentException e) {
                            log.debug("JWT tenantId claim is not a valid UUID: {}", tenantId);
                        }
                    }
                    return Mono.empty();
                })
                // Fallback to header
                .switchIfEmpty(extractUUID(exchange, "tenantId", "X-Tenant-Id"))
                .doOnNext(id -> log.debug("Resolved tenant ID: {}", id))
                .doOnError(error -> log.error("Failed to resolve tenant ID", error));
    }
    
    @Override
    public Mono<UUID> resolveContractId(ServerWebExchange exchange) {
        log.debug("Resolving contract ID from request");
        
        // Contract ID is typically in path variable or header, not JWT
        return extractUUIDFromPath(exchange, "contractId")
                .switchIfEmpty(extractUUID(exchange, "contractId", "X-Contract-Id"))
                .doOnNext(id -> log.debug("Resolved contract ID: {}", id));
    }
    
    @Override
    public Mono<UUID> resolveProductId(ServerWebExchange exchange) {
        log.debug("Resolving product ID from request");
        
        // Product ID is typically in path variable or header, not JWT
        return extractUUIDFromPath(exchange, "productId")
                .switchIfEmpty(extractUUID(exchange, "productId", "X-Product-Id"))
                .doOnNext(id -> log.debug("Resolved product ID: {}", id));
    }
    
    @Override
    protected Mono<Set<String>> resolveRoles(AppContext context, ServerWebExchange exchange) {
        log.debug("Resolving roles for party: {} in contract: {}", 
                context.getPartyId(), context.getContractId());
        
        // TODO: Implement using common-platform-customer-mgmt-sdk
        // When SDK is available, call:
        /*
        if (context.getContractId() != null) {
            return customerMgmtClient.getPartyRolesInContract(
                context.getPartyId(), 
                context.getContractId()
            ).map(response -> response.getRoles());
        } else {
            return customerMgmtClient.getPartyGlobalRoles(
                context.getPartyId()
            ).map(response -> response.getRoles());
        }
        */
        
        // Temporary: Return empty set until SDK integration is complete
        log.warn("SDK integration pending: returning empty roles set");
        return Mono.just(Set.of());
    }
    
    @Override
    protected Mono<Set<String>> resolvePermissions(AppContext context, ServerWebExchange exchange) {
        log.debug("Resolving permissions for party: {} in contract: {}, product: {}", 
                context.getPartyId(), context.getContractId(), context.getProductId());
        
        // TODO: Implement using common-platform-contract-mgmt-sdk
        // When SDK is available, call:
        /*
        if (context.getContractId() != null) {
            return contractMgmtClient.getPartyPermissions(
                context.getPartyId(),
                context.getContractId(),
                context.getProductId()
            ).map(response -> response.getPermissions());
        }
        */
        
        // Temporary: Return empty set until SDK integration is complete
        log.warn("SDK integration pending: returning empty permissions set");
        return Mono.just(Set.of());
    }
    
    @Override
    public boolean supports(ServerWebExchange exchange) {
        // This default resolver supports all requests
        return true;
    }
    
    @Override
    public int getPriority() {
        // Default priority
        return 0;
    }
}
