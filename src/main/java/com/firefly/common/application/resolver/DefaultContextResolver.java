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
 *   <li>Extracts <strong>partyId</strong> from Istio-injected HTTP header ({@code X-Party-Id})</li>
 *   <li>Resolves <strong>tenantId</strong> by calling {@code common-platform-config-mgmt} with the partyId</li>
 *   <li>Enriches context with roles and permissions from platform SDKs</li>
 *   <li>Caches results for performance</li>
 * </ul>
 * 
 * <p><strong>Important:</strong> ContractId and ProductId are NOT extracted here.
 * They must be extracted from {@code @PathVariable} in your controllers and passed explicitly.</p>
 * 
 * <h2>Architecture</h2>
 * <ul>
 *   <li><strong>Istio Gateway:</strong> Validates JWT, injects X-Party-Id header (from JWT subject)</li>
 *   <li><strong>This Resolver:</strong> Uses partyId to fetch tenantId from config-mgmt microservice</li>
 *   <li><strong>Controllers:</strong> Extract contractId/productId from {@code @PathVariable} in REST path</li>
 *   <li><strong>SDK Enrichment:</strong> Fetch roles/permissions from platform SDKs</li>
 * </ul>
 * 
 * <h2>Expected HTTP Headers (Injected by Istio)</h2>
 * <ul>
 *   <li><code>X-Party-Id</code> - Party UUID (required) - Extracted from authenticated JWT subject</li>
 * </ul>
 * 
 * <h2>Tenant Resolution</h2>
 * <p>The tenant ID is <strong>NOT</strong> in the JWT or headers. Instead, it is resolved dynamically:</p>
 * <pre>
 * {@code
 * // Call common-platform-config-mgmt microservice
 * GET /api/v1/parties/{partyId}/tenant
 * Response: { "tenantId": "uuid", "tenantName": "...", ... }
 * }
 * </pre>
 * 
 * <h2>Role & Permission Resolution (FireflySessionManager)</h2>
 * <p>Roles and permissions are <strong>NOT</strong> fetched from individual platform services.
 * Instead, they come from the <strong>FireflySessionManager</strong> in Security Center:</p>
 * <ul>
 *   <li><strong>Session Management:</strong> Tracks which contracts a party has access to</li>
 *   <li><strong>Role Mapping:</strong> Provides party roles in each contract/product</li>
 *   <li><strong>Role Scopes:</strong> Supports party-level, contract-level, and product-level roles</li>
 *   <li><strong>Permission Derivation:</strong> Converts roles to permissions using role mappings</li>
 * </ul>
 * <pre>
 * {@code
 * // Call FireflySessionManager from Security Center
 * PartySession session = sessionManager.getPartySession(partyId, tenantId);
 * 
 * // Get contract-specific roles
 * Set<String> roles = session.getContractRoles(contractId, productId);
 * // e.g., ["owner", "account:viewer", "transaction:creator"]
 * 
 * // Derive permissions from roles
 * Set<String> permissions = session.getPermissionsForRoles(roles);
 * // e.g., ["account:read", "account:update", "transaction:create"]
 * }
 * </pre>
 * 
 * <h2>Controller Responsibility</h2>
 * <p>Controllers must extract contractId and productId from path variables:</p>
 * <pre>
 * {@code
 * @GetMapping("/contracts/{contractId}/accounts")
 * public Mono<List<Account>> getAccounts(@PathVariable UUID contractId, ServerWebExchange exchange) {
 *     // Controller extracts contractId from path, passes to service
 * }
 * }
 * </pre>
 * 
 * @author Firefly Development Team
 * @since 1.0.0
 */
@Component
@Slf4j
public class DefaultContextResolver extends AbstractContextResolver {
    
    // TODO: Inject platform SDK clients when available
    // private final ConfigManagementClient configMgmtClient;  // For tenant resolution
    // private final FireflySessionManager sessionManager;      // For roles/permissions/contract access (Security Center)
    
    @Override
    public Mono<UUID> resolvePartyId(ServerWebExchange exchange) {
        log.debug("Resolving party ID from Istio-injected header");
        
        // Party ID is injected by Istio as X-Party-Id header
        return extractUUID(exchange, "partyId", "X-Party-Id")
                .doOnNext(id -> log.debug("Resolved party ID from Istio header: {}", id))
                .switchIfEmpty(Mono.error(new IllegalStateException(
                    "X-Party-Id header not found. Ensure request passes through Istio gateway.")));
    }
    
    @Override
    public Mono<UUID> resolveTenantId(ServerWebExchange exchange) {
        log.debug("Resolving tenant ID from config-mgmt using party ID");
        
        // Tenant ID is NOT in headers - must be resolved from config-mgmt microservice
        // First, get the party ID from the header
        return resolvePartyId(exchange)
                .flatMap(partyId -> {
                    log.debug("Fetching tenant ID for party: {} from config-mgmt", partyId);
                    
                    // TODO: Implement using common-platform-config-mgmt-sdk
                    // When SDK is available, call:
                    /*
                    return configMgmtClient.getPartyTenant(partyId)
                        .map(response -> response.getTenantId())
                        .doOnNext(tenantId -> log.debug("Resolved tenant ID: {} for party: {}", tenantId, partyId));
                    */
                    
                    // Temporary: Try to get from header first (for backwards compatibility during migration)
                    // Then fallback to error if not available
                    return extractUUID(exchange, "tenantId", "X-Tenant-Id")
                            .doOnNext(id -> log.warn("Using X-Tenant-Id header (deprecated) - should fetch from config-mgmt: {}", id))
                            .switchIfEmpty(Mono.error(new IllegalStateException(
                                "Tenant resolution not implemented. Need to integrate common-platform-config-mgmt-sdk. "
                                + "SDK should call: GET /api/v1/parties/" + partyId + "/tenant")));
                })
                .doOnError(error -> log.error("Failed to resolve tenant ID", error));
    }
    
    @Override
    public Mono<UUID> resolveContractId(ServerWebExchange exchange) {
        // Contract ID is not extracted here - it must be passed explicitly by controllers
        // Controllers extract contractId from @PathVariable and pass it to services
        log.debug("Contract ID resolution delegated to controller layer");
        return Mono.empty();
    }
    
    @Override
    public Mono<UUID> resolveProductId(ServerWebExchange exchange) {
        // Product ID is not extracted here - it must be passed explicitly by controllers
        // Controllers extract productId from @PathVariable and pass it to services
        log.debug("Product ID resolution delegated to controller layer");
        return Mono.empty();
    }
    
    @Override
    protected Mono<Set<String>> resolveRoles(AppContext context, ServerWebExchange exchange) {
        log.debug("Resolving roles for party: {} in contract: {}, product: {}", 
                context.getPartyId(), context.getContractId(), context.getProductId());
        
        // TODO: Implement using FireflySessionManager from Security Center
        // The session manager knows:
        // - Which contracts the party has access to
        // - Which roles the party has in each contract
        // - Role scopes (contract-level, product-level)
        /*
        return sessionManager.getPartySession(context.getPartyId(), context.getTenantId())
            .flatMap(session -> {
                // If contract is specified, get contract-specific roles
                if (context.getContractId() != null) {
                    return Mono.justOrEmpty(
                        session.getContractRoles(context.getContractId(), context.getProductId())
                    );
                }
                // Otherwise, return party-level roles (for party-only endpoints)
                return Mono.just(session.getPartyRoles());
            });
        */
        
        // Temporary: Return empty set until FireflySessionManager integration is complete
        log.warn("FireflySessionManager integration pending: returning empty roles set");
        return Mono.just(Set.of());
    }
    
    @Override
    protected Mono<Set<String>> resolvePermissions(AppContext context, ServerWebExchange exchange) {
        log.debug("Resolving permissions for party: {} in contract: {}, product: {}", 
                context.getPartyId(), context.getContractId(), context.getProductId());
        
        // TODO: Implement using FireflySessionManager from Security Center
        // Permissions are derived from roles defined in the session
        // The session manager provides role-to-permission mappings
        /*
        return sessionManager.getPartySession(context.getPartyId(), context.getTenantId())
            .flatMap(session -> {
                Set<String> roles;
                
                // Get roles based on scope
                if (context.getContractId() != null) {
                    roles = session.getContractRoles(context.getContractId(), context.getProductId());
                } else {
                    roles = session.getPartyRoles();
                }
                
                // Convert roles to permissions using session manager's mapping
                return Mono.just(session.getPermissionsForRoles(roles));
            });
        */
        
        // Temporary: Return empty set until FireflySessionManager integration is complete
        log.warn("FireflySessionManager integration pending: returning empty permissions set");
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
