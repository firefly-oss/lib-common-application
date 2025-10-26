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

package com.firefly.common.application.util;

import com.firefly.security.center.interfaces.dtos.ContractInfoDTO;
import com.firefly.security.center.interfaces.dtos.RoleScopeInfoDTO;
import com.firefly.security.center.interfaces.dtos.SessionContextDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class for mapping SessionContextDTO to roles and permissions.
 * 
 * <p>This mapper extracts roles and permissions from the session based on context:</p>
 * <ul>
 *   <li><strong>Party-level:</strong> All roles across all contracts (when no contractId/productId specified)</li>
 *   <li><strong>Contract-level:</strong> Roles specific to a contract (when contractId specified, no productId)</li>
 *   <li><strong>Product-level:</strong> Roles specific to a contract+product (when both specified)</li>
 * </ul>
 * 
 * <p><strong>Permission Format:</strong> {roleCode}:{actionType}:{resourceType}</p>
 * <p>Examples:</p>
 * <ul>
 *   <li>owner:READ:BALANCE</li>
 *   <li>account_viewer:READ:TRANSACTION</li>
 *   <li>transaction_creator:WRITE:TRANSACTION</li>
 * </ul>
 * 
 * @author Firefly Development Team
 * @since 1.0.0
 */
@Slf4j
public final class SessionContextMapper {
    
    private SessionContextMapper() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Extracts roles for the given context (party, contract, product).
     * 
     * <p><strong>Scoping Rules:</strong></p>
     * <ul>
     *   <li>If both contractId and productId are null → Return all roles from all active contracts</li>
     *   <li>If contractId is present → Return roles from that specific contract</li>
     *   <li>If both contractId and productId are present → Return roles from matching contract+product</li>
     * </ul>
     * 
     * @param sessionContext The session context from FireflySessionManager
     * @param contractId Optional contract ID for scoping (null = party-level)
     * @param productId Optional product ID for additional scoping (null = contract-level)
     * @return Set of role codes (e.g., "owner", "account:viewer", "transaction:creator")
     */
    public static Set<String> extractRoles(SessionContextDTO sessionContext, UUID contractId, UUID productId) {
        if (sessionContext == null || sessionContext.getActiveContracts() == null) {
            log.debug("Session or active contracts is null, returning empty roles");
            return Collections.emptySet();
        }
        
        Set<String> roles = new HashSet<>();
        
        for (ContractInfoDTO contract : sessionContext.getActiveContracts()) {
            // Skip inactive contracts
            if (!Boolean.TRUE.equals(contract.getIsActive())) {
                continue;
            }
            
            // Apply scoping filters
            if (contractId != null && !contractId.equals(contract.getContractId())) {
                continue; // Skip contracts that don't match
            }
            
            if (productId != null && (contract.getProduct() == null || 
                    !productId.equals(contract.getProduct().getProductId()))) {
                continue; // Skip contracts whose product doesn't match
            }
            
            // Extract role code from the contract
            if (contract.getRoleInContract() != null && 
                    Boolean.TRUE.equals(contract.getRoleInContract().getIsActive())) {
                String roleCode = contract.getRoleInContract().getRoleCode();
                if (roleCode != null && !roleCode.isBlank()) {
                    roles.add(roleCode);
                    log.debug("Extracted role: {} for contract: {}, product: {}", 
                            roleCode, contract.getContractId(), 
                            contract.getProduct() != null ? contract.getProduct().getProductId() : null);
                }
            }
        }
        
        log.debug("Extracted {} roles for contractId={}, productId={}: {}", 
                roles.size(), contractId, productId, roles);
        
        return roles;
    }
    
    /**
     * Extracts permissions for the given context (party, contract, product).
     * 
     * <p>Permissions are derived from role scopes with format: {roleCode}:{actionType}:{resourceType}</p>
     * 
     * <p><strong>Scoping Rules:</strong></p>
     * <ul>
     *   <li>If both contractId and productId are null → Return all permissions from all active contracts</li>
     *   <li>If contractId is present → Return permissions from that specific contract</li>
     *   <li>If both contractId and productId are present → Return permissions from matching contract+product</li>
     * </ul>
     * 
     * @param sessionContext The session context from FireflySessionManager
     * @param contractId Optional contract ID for scoping (null = party-level)
     * @param productId Optional product ID for additional scoping (null = contract-level)
     * @return Set of permission strings (e.g., "owner:READ:BALANCE", "account_viewer:READ:TRANSACTION")
     */
    public static Set<String> extractPermissions(SessionContextDTO sessionContext, UUID contractId, UUID productId) {
        if (sessionContext == null || sessionContext.getActiveContracts() == null) {
            log.debug("Session or active contracts is null, returning empty permissions");
            return Collections.emptySet();
        }
        
        Set<String> permissions = new HashSet<>();
        
        for (ContractInfoDTO contract : sessionContext.getActiveContracts()) {
            // Skip inactive contracts
            if (!Boolean.TRUE.equals(contract.getIsActive())) {
                continue;
            }
            
            // Apply scoping filters
            if (contractId != null && !contractId.equals(contract.getContractId())) {
                continue; // Skip contracts that don't match
            }
            
            if (productId != null && (contract.getProduct() == null || 
                    !productId.equals(contract.getProduct().getProductId()))) {
                continue; // Skip contracts whose product doesn't match
            }
            
            // Extract permissions from role scopes
            if (contract.getRoleInContract() != null && 
                    Boolean.TRUE.equals(contract.getRoleInContract().getIsActive()) &&
                    contract.getRoleInContract().getScopes() != null) {
                
                String roleCode = contract.getRoleInContract().getRoleCode();
                
                for (RoleScopeInfoDTO scope : contract.getRoleInContract().getScopes()) {
                    if (Boolean.TRUE.equals(scope.getIsActive()) && 
                            scope.getActionType() != null && 
                            scope.getResourceType() != null) {
                        
                        // Format: {roleCode}:{actionType}:{resourceType}
                        String permission = String.format("%s:%s:%s", 
                                roleCode != null ? roleCode : "unknown",
                                scope.getActionType(), 
                                scope.getResourceType());
                        
                        permissions.add(permission);
                        log.debug("Extracted permission: {} for contract: {}, product: {}", 
                                permission, contract.getContractId(), 
                                contract.getProduct() != null ? contract.getProduct().getProductId() : null);
                    }
                }
            }
        }
        
        log.debug("Extracted {} permissions for contractId={}, productId={}: {}", 
                permissions.size(), contractId, productId, permissions);
        
        return permissions;
    }
    
    /**
     * Checks if a party has access to a specific product through any active contract.
     * 
     * @param sessionContext The session context from FireflySessionManager
     * @param productId The product ID to check access for
     * @return true if party has access to the product, false otherwise
     */
    public static boolean hasAccessToProduct(SessionContextDTO sessionContext, UUID productId) {
        if (sessionContext == null || sessionContext.getActiveContracts() == null || productId == null) {
            return false;
        }
        
        boolean hasAccess = sessionContext.getActiveContracts().stream()
                .filter(contract -> Boolean.TRUE.equals(contract.getIsActive()))
                .anyMatch(contract -> contract.getProduct() != null && 
                                     productId.equals(contract.getProduct().getProductId()));
        
        log.debug("Access check for product {}: {}", productId, hasAccess);
        return hasAccess;
    }
    
    /**
     * Checks if a party has a specific permission (action on resource) for a product.
     * 
     * @param sessionContext The session context from FireflySessionManager
     * @param productId The product ID to check
     * @param actionType The action type (e.g., READ, WRITE, DELETE)
     * @param resourceType The resource type (e.g., BALANCE, TRANSACTION, ACCOUNT)
     * @return true if party has the permission, false otherwise
     */
    public static boolean hasPermission(SessionContextDTO sessionContext, UUID productId, 
                                       String actionType, String resourceType) {
        if (sessionContext == null || sessionContext.getActiveContracts() == null || 
                productId == null || actionType == null) {
            return false;
        }
        
        boolean hasPermission = sessionContext.getActiveContracts().stream()
                .filter(contract -> Boolean.TRUE.equals(contract.getIsActive()))
                .filter(contract -> contract.getProduct() != null && 
                                   productId.equals(contract.getProduct().getProductId()))
                .flatMap(contract -> contract.getRoleInContract() != null && 
                                    contract.getRoleInContract().getScopes() != null ?
                        contract.getRoleInContract().getScopes().stream() : 
                        Collections.<RoleScopeInfoDTO>emptySet().stream())
                .anyMatch(scope -> Boolean.TRUE.equals(scope.getIsActive()) &&
                                  actionType.equalsIgnoreCase(scope.getActionType()) &&
                                  (resourceType == null || resourceType.equalsIgnoreCase(scope.getResourceType())));
        
        log.debug("Permission check for product {}, action={}, resource={}: {}", 
                productId, actionType, resourceType, hasPermission);
        
        return hasPermission;
    }
}
