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

package com.firefly.common.application.security;

import com.firefly.common.application.context.AppContext;
import com.firefly.common.application.context.AppSecurityContext;
import reactor.core.publisher.Mono;

/**
 * Service for authorization decisions.
 * Integrates with Firefly SecurityCenter to determine access rights.
 * 
 * @author Firefly Development Team
 * @since 1.0.0
 */
public interface SecurityAuthorizationService {
    
    /**
     * Authorizes an operation based on the application context and security requirements.
     * 
     * @param context the application context
     * @param securityContext the security context with requirements
     * @return Mono of updated AppSecurityContext with authorization result
     */
    Mono<AppSecurityContext> authorize(AppContext context, AppSecurityContext securityContext);
    
    /**
     * Checks if a party has a specific role in a contract/product context.
     * 
     * @param context the application context
     * @param role the role to check
     * @return Mono of boolean indicating if role is present
     */
    Mono<Boolean> hasRole(AppContext context, String role);
    
    /**
     * Checks if a party has a specific permission in a contract/product context.
     * 
     * @param context the application context
     * @param permission the permission to check
     * @return Mono of boolean indicating if permission is granted
     */
    Mono<Boolean> hasPermission(AppContext context, String permission);
    
    /**
     * Evaluates a custom security expression.
     * 
     * @param context the application context
     * @param expression the SpEL expression to evaluate
     * @return Mono of boolean result
     */
    Mono<Boolean> evaluateExpression(AppContext context, String expression);
}
