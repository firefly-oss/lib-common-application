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

import com.firefly.common.application.context.AppConfig;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base implementation of ConfigResolver with caching support.
 * Integrates with common-platform-config-mgmt-sdk to fetch tenant configuration.
 * 
 * <p>This implementation provides a simple in-memory cache for configuration data.
 * Subclasses can override caching behavior or use external cache implementations.</p>
 * 
 * @author Firefly Development Team
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractConfigResolver implements ConfigResolver {
    
    private final Map<UUID, AppConfig> configCache = new ConcurrentHashMap<>();
    
    @Override
    public Mono<AppConfig> resolveConfig(UUID tenantId) {
        log.debug("Resolving configuration for tenant: {}", tenantId);
        
        // Check cache first
        AppConfig cached = configCache.get(tenantId);
        if (cached != null) {
            log.debug("Configuration found in cache for tenant: {}", tenantId);
            return Mono.just(cached);
        }
        
        // Fetch from platform
        return fetchConfigFromPlatform(tenantId)
                .doOnSuccess(config -> {
                    configCache.put(tenantId, config);
                    log.debug("Cached configuration for tenant: {}", tenantId);
                })
                .doOnError(error -> log.error("Failed to resolve config for tenant: {}", tenantId, error));
    }
    
    @Override
    public Mono<AppConfig> refreshConfig(UUID tenantId) {
        log.debug("Refreshing configuration for tenant: {}", tenantId);
        
        // Remove from cache and fetch fresh
        configCache.remove(tenantId);
        return resolveConfig(tenantId);
    }
    
    @Override
    public boolean isCached(UUID tenantId) {
        return configCache.containsKey(tenantId);
    }
    
    /**
     * Fetches tenant configuration from the platform config management service.
     * 
     * <p>TODO: Implementation should use common-platform-config-mgmt-sdk to fetch
     * the tenant configuration including provider settings, feature flags, and
     * tenant-specific settings.</p>
     * 
     * @param tenantId the tenant ID
     * @return Mono of AppConfig
     */
    protected Mono<AppConfig> fetchConfigFromPlatform(UUID tenantId) {
        // TODO: Implement configuration fetching using platform SDK
        // Example:
        // return configManagementClient.getTenantConfig(tenantId)
        //     .map(response -> AppConfig.builder()
        //         .tenantId(response.getTenantId())
        //         .tenantName(response.getName())
        //         .providers(convertProviders(response.getProviders()))
        //         .featureFlags(response.getFeatureFlags())
        //         .settings(response.getSettings())
        //         .environment(response.getEnvironment())
        //         .active(response.isActive())
        //         .build());
        
        log.warn("fetchConfigFromPlatform not implemented, returning empty config for tenant: {}", tenantId);
        return Mono.just(AppConfig.builder()
                .tenantId(tenantId)
                .active(true)
                .build());
    }
    
    /**
     * Clears the entire configuration cache.
     * Useful for testing or when a full refresh is needed.
     */
    protected void clearCache() {
        configCache.clear();
        log.info("Configuration cache cleared");
    }
    
    /**
     * Clears cache for a specific tenant.
     * 
     * @param tenantId the tenant ID
     */
    protected void clearCacheForTenant(UUID tenantId) {
        configCache.remove(tenantId);
        log.debug("Configuration cache cleared for tenant: {}", tenantId);
    }
}
