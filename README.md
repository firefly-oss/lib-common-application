# 🔥 Firefly Common Application Library

<p align="center">
  <strong>A comprehensive Spring Boot library for building Application Layer microservices</strong><br>
  Business process orchestration • Multi-domain coordination • Context management • Security & Authorization
</p>

---

## 📖 Table of Contents

- [Overview](#overview)
- [Understanding the Three-Layer Architecture](#understanding-the-three-layer-architecture)
- [When to Use This Library](#when-to-use-this-library)
- [Key Features](#key-features)
- [Quick Start](#quick-start)
- [Core Components](#core-components)
- [Architecture Patterns](#architecture-patterns)
- [📚 Complete Documentation](#-complete-documentation)
- [Examples](#examples)
- [Configuration](#configuration)
- [Testing](#testing)
- [Performance & Monitoring](#performance--monitoring)
- [Contributing](#contributing)
- [License](#license)

---

A comprehensive Spring Boot library that enables application layer architecture for business process-oriented microservices with context management, security, and authorization support.

## Overview

**The Application Layer is where business process orchestration microservices live.** This layer exposes REST/GraphQL APIs to channels (web, mobile, third-party applications) and orchestrates multi-domain business processes.

### Understanding Firefly's Three Layers

Firefly implements a clean, layered microservice architecture:

```
Channels (Web/Mobile/Apps)
         ↓
┌─────────────────────────────────────────┐
│   APPLICATION LAYER (THIS LIBRARY)      │  ← Exposes APIs to Channels
│   Business Process Orchestration        │  ← Orchestrates Multiple Domains
│   - customer-application-onboarding     │  ← Uses lib-common-application
│   - lending-application-loan-origin     │
└─────────────────┬───────────────────────┘
                  │ Uses ApplicationExecutionContext
                  ↓
┌─────────────────────────────────────────┐
│   DOMAIN LAYER                          │  ← Domain-Driven Design
│   Business Logic & Rules                │  ← Single-Domain Focus
│   - customer-domain-people              │  ← Uses lib-common-domain
│   - lending-domain-loan                 │
└─────────────────┬───────────────────────┘
                  │ Uses Repositories & SDKs
                  ↓
┌─────────────────────────────────────────┐
│   INFRASTRUCTURE/PLATFORM LAYER         │  ← Master Data Management
│   Data Persistence & Platform Services  │  ← External Integrations
│   - common-platform-customer-mgmt       │  ← Uses lib-common-core
│   - common-platform-contract-mgmt       │
└─────────────────────────────────────────┘
```

**This library (lib-common-application) is for Application Layer microservices that:**
- **Expose REST/GraphQL APIs** to channels (web, mobile, third-party)
- **Orchestrate business processes** across multiple domains
- **Coordinate multi-domain operations** (customer + contract + account)
- **Manage complete application context** (party, contract, product, tenant, security)
- **Handle security & authorization** at the API level

**NOT for:**
- Domain layer microservices (use `lib-common-domain`)
- Platform/infrastructure services (use `lib-common-core`)
- Single-domain operations (use `lib-common-domain`)

### Key Responsibilities

- **Context Management**: Resolving and managing party, contract, product, and tenant information
- **Security & Authorization**: Declarative security with @Secure annotation and SecurityCenter integration  
- **Configuration**: Multi-tenant configuration management with provider settings
- **Business Process Orchestration**: Coordinating domain services to fulfill business operations

## Architecture Complete

This library provides a fully structured application layer with:
- ✅ **@FireflyApplication** annotation for application metadata and service discovery
- ✅ **Context Architecture** (AppContext, AppConfig, AppSecurityContext)
- ✅ **@Secure Annotation** system for declarative security
- ✅ **Abstract Resolvers** for context and configuration
- ✅ **Security Authorization** framework with SecurityCenter integration points
- ✅ **Abstract Application Service** base class
- ✅ **AOP Interceptors** for annotation processing
- ✅ **Endpoint Security Registry** for explicit mappings
- ✅ **Spring Boot Auto-configuration**
- ✅ **Actuator Integration** with application metadata exposed in /actuator/info

### 🏗️ Infrastructure Components Included

- ✅ **Banner** - Firefly Application Layer banner (banner.txt)
- ✅ **Health Checks** - ApplicationLayerHealthIndicator for /actuator/health
- ✅ **Structured Logging** - JSON logging via logback-spring.xml with MDC support
- ✅ **Caching** - Integration with lib-common-cache (Caffeine/Redis)
- ✅ **CQRS Support** - Command/Query separation via lib-common-cqrs
- ✅ **Event-Driven Architecture** - Event publishing via lib-common-eda
- ✅ **Actuator Integration** - Full Spring Boot Actuator support

### 🔥 Application Metadata with @FireflyApplication

Declare metadata about your microservice for service discovery, monitoring, and governance:

```java
@FireflyApplication(
    name = "customer-onboarding",
    displayName = "Customer Onboarding Service",
    description = "Orchestrates customer onboarding: KYC verification, document upload, and account setup",
    domain = "customer",
    team = "customer-experience",
    owners = {"john.doe@firefly.com", "jane.smith@firefly.com"},
    apiBasePath = "/api/v1/onboarding",
    usesServices = {"customer-domain-people", "common-platform-customer-mgmt", "kyc-provider-api"},
    capabilities = {"Customer Identity Verification", "Document Management", "Account Creation"}
)
@SpringBootApplication
public class CustomerOnboardingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerOnboardingApplication.class, args);
    }
}
```

**Benefits:**
- 📊 **Service Discovery** - Automatic catalog of all microservices
- 👥 **Ownership Tracking** - Know who owns what
- 🔗 **Dependency Mapping** - Visualize service dependencies
- 📈 **Monitoring** - Metadata exposed via `/actuator/info`
- 📖 **Self-Documentation** - Services document themselves

### 📐 Context Architecture

The library provides a comprehensive context model for application requests:

```java
ApplicationExecutionContext {
    AppContext context;          // Business context (partyId, contractId, productId, roles)
    AppConfig config;            // Tenant configuration (providers, feature flags)
    AppSecurityContext security; // Security requirements and authorization
}
```

**AppContext** - Business domain context (party, contract, product) from platform services
**AppConfig** - Multi-tenant configuration and provider settings
**AppSecurityContext** - Security requirements and authorization results

All TODO placeholders are marked for SDK integration with:
- common-platform-customer-mgmt-sdk (party information, roles)
- common-platform-contract-mgmt-sdk (contract information, permissions)
- common-platform-config-mgmt-sdk (tenant configuration, providers)

See the comprehensive architecture documentation below.

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.firefly</groupId>
    <artifactId>lib-common-application</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Declare Application Metadata

```java
@FireflyApplication(
    name = "my-service",
    displayName = "My Service",
    description = "Description of what this service does",
    domain = "my-domain",
    team = "my-team",
    owners = {"dev@company.com"},
    apiBasePath = "/api/v1/my-service",
    usesServices = {"domain-service-1", "platform-service-2"}
)
@SpringBootApplication
public class MyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyServiceApplication.class, args);
    }
}
```

### 3. Implement Required Components

```java
// 1. Context Resolver
@Component
public class MyContextResolver extends AbstractContextResolver {
    // Implement resolvePartyId, resolveTenantId, etc.
}

// 2. Config Resolver  
@Component
public class MyConfigResolver extends AbstractConfigResolver {
    // Override fetchConfigFromPlatform()
}

// 3. Authorization Service
@Service
public class MyAuthorizationService extends AbstractSecurityAuthorizationService {
    // Use default implementation or override authorizeWithSecurityCenter()
}

// 4. Application Service
@Service
public class MyApplicationService extends AbstractApplicationService {
    // Implement business process orchestration
}
```

### 4. Use Security Annotations

```java
@RestController
@RequestMapping("/api/v1/accounts")
@Secure(roles = {"ACCOUNT_HOLDER"})
public class AccountController {
    
    @PostMapping("/{id}/transfer")
    @Secure(roles = "ACCOUNT_OWNER", permissions = "TRANSFER_FUNDS")
    public Mono<Transfer> transfer(@PathVariable UUID id, 
                                   @RequestBody TransferRequest request,
                                   ServerWebExchange exchange) {
        return applicationService.transferFunds(exchange, request);
    }
}
```

## Key Features

### 🎯 Context Management
- **Automatic Resolution**: Extract party, contract, product, tenant from requests
- **Enrichment**: Fetch roles and permissions from platform services
- **Caching**: Built-in caching for performance optimization
- **Immutability**: Thread-safe context objects

### 🔒 Security & Authorization
- **Declarative**: `@Secure` annotation for clean, self-documenting code
- **Programmatic**: `EndpointSecurityRegistry` for dynamic configuration
- **SecurityCenter Integration**: Ready for complex authorization policies
- **Role & Permission Based**: Fine-grained access control

### ⚙️ Configuration Management
- **Multi-tenant**: Per-tenant configuration and provider settings
- **Feature Flags**: A/B testing and gradual rollouts
- **Provider Configs**: Payment gateways, KYC providers, etc.
- **Caching**: Configurable TTL for performance

### 🎭 Business Process Orchestration
- **Cross-domain Coordination**: Orchestrate multiple domain services
- **Transaction Management**: Clear transaction boundaries
- **Error Handling**: Compensating transactions support
- **Reactive**: Fully non-blocking with Project Reactor

## Architecture Patterns

This library implements several industry-standard patterns:

1. **Application Service Pattern** - Business process orchestration
2. **Context Object Pattern** - Immutable request context
3. **Strategy Pattern** - Pluggable context resolution
4. **Decorator Pattern** - AOP-based security
5. **Registry Pattern** - Endpoint security configuration
6. **Template Method Pattern** - Consistent resolver structure
7. **Facade Pattern** - Simplified domain access

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed explanations.

## 📚 Complete Documentation

### 🔥 Essential Reading

| Document | Description | When to Read |
|----------|-------------|-------------|
| **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** | Complete architectural overview, three-layer explanation, patterns, ADRs | ⭐ **START HERE** - Understanding the architecture |
| **[EXAMPLE_MICROSERVICE_ARCHITECTURE.md](docs/EXAMPLE_MICROSERVICE_ARCHITECTURE.md)** | Complete production-ready microservice example with code | 🔨 **Building your first service** |
| **[SECURITY_GUIDE.md](docs/SECURITY_GUIDE.md)** | Complete security guide: annotations, registry, SecurityCenter integration | 🔒 Implementing authentication & authorization |
| **[USAGE_GUIDE.md](docs/USAGE_GUIDE.md)** | Step-by-step implementation guide with examples | 🚀 Getting started guide |
| **[API_REFERENCE.md](docs/API_REFERENCE.md)** | Complete API documentation for all classes and methods | 📑 Reference when developing |

### 📖 Architecture Documentation

The [ARCHITECTURE.md](docs/ARCHITECTURE.md) document provides:
- Complete architectural overview with layer responsibilities
- 7 design patterns explained with examples
- Detailed data flow diagrams for request processing
- Integration points with platform services
- 10 Architecture Decision Records (ADRs)
- Best practices and guidelines

### 🚀 Usage Guide

The [USAGE_GUIDE.md](docs/USAGE_GUIDE.md) includes:
- Getting started instructions
- Step-by-step basic implementation
- Advanced patterns (compensating transactions, multi-step processes)
- Testing strategies (unit and integration tests)
- Troubleshooting common issues
- Debug tips and monitoring

### 📋 API Reference

The [API_REFERENCE.md](docs/API_REFERENCE.md) provides:
- Complete class and method documentation
- All annotation attributes explained
- Usage examples for every component
- Configuration properties reference
- Error handling patterns
- Reactive programming patterns

## Integration Points

The library provides clear integration points (marked with TODO) for:

### 1. Customer Management (`common-platform-customer-mgmt-sdk`)
- Resolve party information
- Fetch party roles in contracts
- Validate party status

### 2. Contract Management (`common-platform-contract-mgmt-sdk`)
- Resolve contract information
- Fetch party permissions
- Validate contract status

### 3. Configuration Management (`common-platform-config-mgmt-sdk`)
- Fetch tenant configuration
- Get provider configurations
- Retrieve feature flags

### 4. Security Center (Future)
- Complex authorization policies
- Attribute-Based Access Control (ABAC)
- Audit trail of authorization decisions

## Design Principles

### ✅ SOLID Principles
- **Single Responsibility**: Each class has one clear purpose
- **Open/Closed**: Extensible via abstract classes and interfaces
- **Liskov Substitution**: All abstractions can be safely substituted
- **Interface Segregation**: Focused, cohesive interfaces
- **Dependency Inversion**: Depend on abstractions, not concretions

### ✅ Best Practices
- **Immutability**: All context objects are immutable
- **Reactive First**: Non-blocking, composable operations
- **Fail Fast**: Early validation prevents cascading failures
- **Explicit Context**: No ThreadLocal magic, clear data flow
- **Separation of Concerns**: Security, business logic, infrastructure separated

## Configuration

```yaml
firefly:
  application:
    security:
      enabled: true                    # Enable security features
      use-security-center: true        # Delegate to SecurityCenter
      default-roles: []                # Default roles
      fail-on-missing: false           # Fail on missing config
    context:
      cache-enabled: true              # Enable context caching
      cache-ttl: 300                   # Cache TTL (seconds)
      cache-max-size: 1000            # Maximum cache size
    config:
      cache-enabled: true              # Enable config caching
      cache-ttl: 600                   # Cache TTL (seconds)
      refresh-on-startup: false       # Refresh on startup
```

## Examples

### Simple Transfer Operation

```java
@Service
public class AccountApplicationService extends AbstractApplicationService {
    
    public Mono<Transfer> transferFunds(ServerWebExchange exchange, TransferRequest request) {
        return resolveExecutionContext(exchange)
            .flatMap(context -> validateContext(context, true, true))
            .flatMap(context -> requirePermission(context, "TRANSFER_FUNDS")
                .thenReturn(context))
            .flatMap(context -> accountDomainService.transfer(
                context.getContext(),
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount()
            ));
    }
}
```

### Multi-Step Business Process

```java
public Mono<LoanApplication> processLoan(ServerWebExchange exchange, LoanRequest request) {
    return resolveExecutionContext(exchange)
        .flatMap(ctx -> validateContext(ctx, true, true))
        .flatMap(ctx -> creditCheckService.performCheck(ctx, request)
            .zipWith(documentService.verifyDocuments(ctx, request))
            .flatMap(results -> approvalService.evaluate(ctx, results))
            .filter(ApprovalResult::isApproved)
            .flatMap(approval -> createLoan(ctx, request, approval))
        );
}
```

### Feature Flag Usage

```java
return isFeatureEnabled(context, "NEW_PAYMENT_FLOW")
    .flatMap(enabled -> enabled
        ? newPaymentProcessor.process(context, payment)
        : legacyPaymentProcessor.process(context, payment)
    );
```

## Testing

### ✅ Comprehensive Test Suite

**84 tests - 100% passing**

The library includes extensive test coverage:
- **Context Management**: 47 tests covering AppContext, AppConfig, AppMetadata, ApplicationExecutionContext
- **Security Components**: 27 tests for AppSecurityContext, SecurityEvaluationResult, EndpointSecurityRegistry
- **Configuration**: 10 tests for ApplicationLayerProperties and all nested classes

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AppContextTest

# With coverage report
mvn clean test jacoco:report
```

**Test Metrics:**
- Total Tests: 84
- Success Rate: 100%
- Execution Time: < 0.1s
- Core Coverage: ~85%+

See [TESTING.md](docs/TESTING.md) for detailed documentation.

### Unit Testing Examples

```java
@ExtendWith(MockitoExtension.class)
class MyContextResolverTest {
    @Mock
    private ServerWebExchange exchange;
    
    @InjectMocks
    private MyContextResolver resolver;
    
    @Test
    void shouldResolvePartyId() {
        // Given
        UUID expectedPartyId = UUID.randomUUID();
        mockJwtToken(expectedPartyId);
        
        // When
        StepVerifier.create(resolver.resolvePartyId(exchange))
            // Then
            .expectNext(expectedPartyId)
            .verifyComplete();
    }
}
```

### Integration Testing

```java
@SpringBootTest
@AutoConfigureWebTestClient
class AccountControllerIntegrationTest {
    
    @Autowired
    private WebTestClient webClient;
    
    @Test
    @WithMockJwt(partyId = "...", roles = {"ACCOUNT_HOLDER"})
    void shouldTransferFunds() {
        webClient.post()
            .uri("/api/v1/accounts/{id}/transfer", accountId)
            .bodyValue(transferRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Transfer.class)
            .value(transfer -> {
                assertThat(transfer.getStatus()).isEqualTo(COMPLETED);
            });
    }
}
```

## Performance Considerations

### Caching Strategy
- **Context Caching**: 5-minute TTL for roles/permissions
- **Config Caching**: 10-minute TTL for tenant configuration
- **LRU Eviction**: Configurable max cache size

### Circuit Breakers
```java
@CircuitBreaker(name = "customer-mgmt", fallbackMethod = "fallbackRoles")
protected Mono<Set<String>> resolveRoles(AppContext context) {
    return customerManagementClient.getPartyRoles(...);
}
```

### Timeouts
```java
return resolveExecutionContext(exchange)
    .timeout(Duration.ofSeconds(5))
    .onErrorMap(TimeoutException.class, 
        e -> new ApplicationException("Context resolution timeout"));
```

## Monitoring & Observability

### Metrics
- Context resolution duration
- Authorization success/failure rates
- Cache hit/miss ratios
- SecurityCenter call latencies

### Logging
Enable debug logging for troubleshooting:
```yaml
logging:
  level:
    com.firefly.common.application: DEBUG
    com.firefly.common.application.aop: TRACE
```

### Tracing
Automatic correlation ID propagation for distributed tracing:
- Request ID generation
- Correlation ID from headers or auto-generated
- Trace ID and Span ID support
- MDC integration for structured logging

## Contributing

This library follows strict architectural principles:

1. **No implementation logic** - Provide abstractions with TODO placeholders
2. **Immutability first** - Use @Value and @With from Lombok
3. **Reactive patterns** - All operations return Mono/Flux
4. **Complete JavaDoc** - Every public API must be documented
5. **Architecture decisions** - Document significant decisions in ADRs

## License

Copyright 2025 Firefly Software Solutions Inc

Licensed under the Apache License, Version 2.0

## Support

For questions or issues:
- Review the [documentation](docs/)
- Check [troubleshooting guide](docs/USAGE_GUIDE.md#troubleshooting)
- Enable debug logging for diagnostics

---

**Built with ❤️ by the Firefly Development Team**
