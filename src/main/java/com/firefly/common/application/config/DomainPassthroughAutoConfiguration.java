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

package com.firefly.common.application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.aop.support.AopUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Auto-configuration that scans beans annotated with {@link DomainPassthrough}
 * (including repeatable usage) and registers Spring Cloud Gateway routes accordingly.
 */
@AutoConfiguration
@ConditionalOnClass(RouteLocatorBuilder.class)
@Slf4j
public class DomainPassthroughAutoConfiguration {

    @Autowired
    private ApplicationContext context;

    @Bean
    public RouteLocator domainPassthroughRoutes(RouteLocatorBuilder builder) {
        var routes = builder.routes();

        // Collect beans annotated with single or container annotation
        Map<String, Object> beans = new LinkedHashMap<>();
        beans.putAll(context.getBeansWithAnnotation(DomainPassthrough.class));
        beans.putAll(context.getBeansWithAnnotation(DomainPassthroughs.class));

        var detectedRoutes = new java.util.ArrayList<String>();

        beans.forEach((name, bean) -> {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            DomainPassthrough[] annotations = targetClass.getAnnotationsByType(DomainPassthrough.class);
            for (int i = 0; i < annotations.length; i++) {
                var ann = annotations[i];
                String routeId = ann.routeId().isEmpty()
                        ? name + "-passthrough" + (annotations.length > 1 ? "-" + (i + 1) : "")
                        : ann.routeId();

                routes.route(routeId, r -> r
                        .path(ann.path())
                        .uri(ann.target()));

                detectedRoutes.add(String.format("id=%s, bean=%s, path=%s, target=%s",
                        routeId, name, ann.path(), ann.target()));
            }
        });

        if (detectedRoutes.isEmpty()) {
            log.info("DomainPassthrough: no routes detected.");
        } else {
            var sb = new StringBuilder();
            detectedRoutes.forEach(r -> sb.append(" - ").append(r).append(System.lineSeparator()));
            log.info("DomainPassthrough: {} routes detected:\n{}", detectedRoutes.size(), sb);
        }

        return routes.build();
    }
}
