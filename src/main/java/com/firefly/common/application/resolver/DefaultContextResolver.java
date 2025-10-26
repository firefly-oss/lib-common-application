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
 *   <li>Extracts party/tenant/contract/product IDs from HTTP headers (injected by Istio)</li>
 *   <li>Enriches context with roles and permissions from platform SDKs</li>
 *   <li>Caches results for performance</li>
 * </ul>
 * 
 * <h2>What Microservices Need to Do</h2>
 * <p><strong>Nothing.</strong> This works automatically once you add the library dependency.</p>
 * 
 * <h2>Expected HTTP Headers (Injected by Istio)</h2>
 * <ul>
 *   <li><code>X-Party-Id</code> - Party UUID (required)</li>
 *   <li><code>X-Tenant-Id</code> - Tenant UUID (required)</li>
 *   <li><code>X-Contract-Id</code> - Contract UUID (optional, from path variable or header)</li>
 *   <li><code>X-Product-Id</code> - Product UUID (optional, from path variable or header)</li>
 * </ul>
 * 
 * <p><strong>Note:</strong> Istio service mesh automatically injects X-Party-Id and X-Tenant-Id headers
 * based on authentication. Microservices don't need to handle authentication - it's done at the gateway.</p>
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
        log.debug("Resolving party ID from Istio-injected header");
        
        // Party ID is injected by Istio as X-Party-Id header
        return extractUUID(exchange, "partyId", "X-Party-Id")
                .doOnNext(id -> log.debug("Resolved party ID from Istio header: {}", id))
                .switchIfEmpty(Mono.error(new IllegalStateException(
                    "X-Party-Id header not found. Ensure request passes through Istio gateway.")));
    }
    
    @Override
    public Mono<UUID> resolveTenantId(ServerWebExchange exchange) {
        log.debug("Resolving tenant ID from Istio-injected header");
        
        // Tenant ID is injected by Istio as X-Tenant-Id header
        return extractUUID(exchange, "tenantId", "X-Tenant-Id")
                .doOnNext(id -> log.debug("Resolved tenant ID from Istio header: {}", id))
                .switchIfEmpty(Mono.error(new IllegalStateException(
                    "X-Tenant-Id header not found. Ensure request passes through Istio gateway.")));
    }
    
    @Override
    public Mono<UUID> resolveContractId(ServerWebExchange exchange) {
        log.debug("Resolving contract ID from path variable or header");
        
        // Contract ID comes from path variable (e.g., /contracts/{contractId}/...) or header
        return extractUUIDFromPath(exchange, "contractId")
                .switchIfEmpty(extractUUID(exchange, "contractId", "X-Contract-Id"))
                .doOnNext(id -> log.debug("Resolved contract ID: {}", id));
    }
    
    @Override
    public Mono<UUID> resolveProductId(ServerWebExchange exchange) {
        log.debug("Resolving product ID from path variable or header");
        
        // Product ID comes from path variable (e.g., /products/{productId}/...) or header
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
