# Firefly Common Application Library
A comprehensive Spring Boot library for building Application Layer microservices
Business process orchestration • Multi-domain coordination • Context management • Security & Authorization

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
    owners = {"john.doe@getfirefly.io", "jane.smith@getfirefly.io"},
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
- `common-platform-customer-mgmt-sdk` - Party information, roles
- `common-platform-contract-mgmt-sdk` - Contract information, permissions
- `common-platform-product-mgmt-sdk` - Product information, product-specific config
- `common-platform-config-mgmt-sdk` - Tenant configuration, providers, feature flags

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

The library provides **abstract base classes** that you must extend in your microservice. Each abstracts focuses on a specific responsibility:

#### AbstractContextResolver - Extract Request Information

**Purpose:** Extract party/tenant/contract/product IDs from the HTTP request.

**You must implement:**
- `resolvePartyId(ServerWebExchange)` - Extract from JWT, header, or session
- `resolveTenantId(ServerWebExchange)` - Extract from JWT, header, or subdomain
- `resolveContractId(ServerWebExchange)` - Extract from path variable or header (optional)
- `resolveProductId(ServerWebExchange)` - Extract from path variable or header (optional)

**You should override (optional):**
- `resolveRoles(AppContext, ServerWebExchange)` - Call `customer-mgmt-sdk` to get party roles
- `resolvePermissions(AppContext, ServerWebExchange)` - Call `contract-mgmt-sdk` to get permissions

```java
@Component
public class CustomContextResolver extends AbstractContextResolver {
    
    @Override
    public Mono<UUID> resolvePartyId(ServerWebExchange exchange) {
        // Extract from JWT, header, or your auth mechanism
        return extractUUID(exchange, "partyId", "X-Party-Id");
    }
    
    @Override
    public Mono<UUID> resolveTenantId(ServerWebExchange exchange) {
        // Extract from JWT, header, or subdomain
        return extractUUID(exchange, "tenantId", "X-Tenant-Id");
    }
    
    @Override
    public Mono<UUID> resolveContractId(ServerWebExchange exchange) {
        // Extract from path variable or header (if applicable)
        return extractUUIDFromPath(exchange, "contractId")
            .switchIfEmpty(extractUUID(exchange, "contractId", "X-Contract-Id"));
    }
    
    @Override
    public Mono<UUID> resolveProductId(ServerWebExchange exchange) {
        // Extract from path variable or header (if applicable)
        return extractUUIDFromPath(exchange, "productId")
            .switchIfEmpty(extractUUID(exchange, "productId", "X-Product-Id"));
    }
    
    // Optional: Enrich with roles from platform service
    @Override
    protected Mono<Set<String>> resolveRoles(AppContext context, ServerWebExchange exchange) {
        // TODO: Call common-platform-customer-mgmt-sdk
        // return customerMgmtClient.getPartyRoles(context.getPartyId(), context.getContractId());
        return Mono.just(Set.of()); // Return empty set until SDK is integrated
    }
    
    // Optional: Enrich with permissions from platform service
    @Override
    protected Mono<Set<String>> resolvePermissions(AppContext context, ServerWebExchange exchange) {
        // TODO: Call common-platform-contract-mgmt-sdk
        // return contractMgmtClient.getPartyPermissions(context.getPartyId(), context.getContractId());
        return Mono.just(Set.of()); // Return empty set until SDK is integrated
    }
}
```

---

#### AbstractConfigResolver - Fetch Tenant Configuration

**Purpose:** Load tenant-specific configuration (providers, feature flags, settings).

**You must implement:**
- `fetchConfigFromPlatform(UUID tenantId)` - Fetch config from your config service

```java
@Component
public class CustomConfigResolver extends AbstractConfigResolver {
    
    // TODO: Inject your config management SDK
    // private final ConfigManagementClient configClient;
    
    @Override
    protected Mono<AppConfig> fetchConfigFromPlatform(UUID tenantId) {
        // TODO: Call common-platform-config-mgmt-sdk
        /*
        return configClient.getTenantConfig(tenantId)
            .map(response -> AppConfig.builder()
                .tenantId(response.getTenantId())
                .tenantName(response.getName())
                .providers(response.getProviders())
                .featureFlags(response.getFeatureFlags())
                .settings(response.getSettings())
                .build());
        */
        
        // Placeholder until SDK is integrated
        return Mono.just(AppConfig.builder()
            .tenantId(tenantId)
            .build());
    }
}
```

---

#### AbstractSecurityAuthorizationService - Authorization Decision

**Purpose:** Decide if a request should be authorized based on roles/permissions.

**Default behavior:** Checks if user's roles/permissions match required roles/permissions.

**You can override:**
- `authorizeWithSecurityCenter(...)` - Integrate with SecurityCenter for complex policies

```java
@Service
public class CustomAuthorizationService extends AbstractSecurityAuthorizationService {
    
    // OPTION 1: Use default role/permission matching (no code needed)
    // Just declare the class and Spring will use the default implementation
    
    // OPTION 2: Integrate with SecurityCenter (optional)
    /*
    @Override
    protected Mono<AppSecurityContext> authorizeWithSecurityCenter(
            AppContext context, AppSecurityContext securityContext) {
        return securityCenterClient.evaluate(context, securityContext);
    }
    */
}
```

---

#### AbstractApplicationService - Business Process Orchestration

**Purpose:** Base class for application services that orchestrate business processes.

**Provides helper methods:**
- `resolveExecutionContext(ServerWebExchange)` - Resolves full ApplicationExecutionContext
- `validateContext(context, requireContract, requireProduct)` - Validates required IDs  
- `requireRole(context, role)` - Throws AccessDeniedException if role missing
- `requirePermission(context, permission)` - Throws AccessDeniedException if permission missing
- `getProviderConfig(context, providerType)` - Gets provider configuration (KYC, payment gateway, etc.)
- `isFeatureEnabled(context, feature)` - Checks if feature flag is enabled

```java
@Service
public class CustomerOnboardingService extends AbstractApplicationService {
    
    private final CustomerDomainService customerDomain;
    private final KycProviderService kycProvider;
    
    // Constructor with required AbstractApplicationService dependencies
    public CustomerOnboardingService(
            ContextResolver contextResolver,
            ConfigResolver configResolver,
            SecurityAuthorizationService authorizationService,
            CustomerDomainService customerDomain,
            KycProviderService kycProvider) {
        super(contextResolver, configResolver, authorizationService);
        this.customerDomain = customerDomain;
        this.kycProvider = kycProvider;
    }
    
    public Mono<Customer> onboardCustomer(ServerWebExchange exchange, OnboardingRequest request) {
        return resolveExecutionContext(exchange)
            // Validate we have partyId and tenantId (no contract/product required)
            .flatMap(ctx -> validateContext(ctx, false, false))
            
            // Check permission
            .flatMap(ctx -> requirePermission(ctx, "CREATE_CUSTOMER")
                .thenReturn(ctx))
            
            // Orchestrate business process: KYC → Domain Service
            .flatMap(ctx -> 
                kycProvider.verify(ctx, request)
                    .flatMap(kycResult -> customerDomain.createCustomer(ctx, request, kycResult))
            );
    }
}
```

---

#### Optional: AbstractContractController / AbstractProductController

**Purpose:** Base controllers that provide helper methods for validating contract/product IDs.

**What they provide:**
- `AbstractContractController`: Validation and logging for contract-scoped endpoints
- `AbstractProductController`: Validation and logging for contract + product endpoints

**Example with AbstractContractController:**

```java
@RestController
@RequestMapping("/api/v1/contracts/{contractId}/accounts")
public class AccountController extends AbstractContractController {
    
    @Autowired
    private AccountApplicationService accountService;
    
    @GetMapping
    @Secure(roles = "ACCOUNT_VIEWER")
    public Mono<List<AccountDto>> listAccounts(
            @PathVariable UUID contractId,
            ServerWebExchange exchange) {
        
        // Validate contractId is present (throws exception if null)
        requireContractId(contractId);
        
        // Optional: Log the operation for debugging
        logOperation(contractId, "listAccounts");
        
        return accountService.listAccountsByContract(exchange, contractId);
    }
}
```

**Example with AbstractProductController:**

```java
@RestController
@RequestMapping("/api/v1/contracts/{contractId}/products/{productId}/transactions")
public class TransactionController extends AbstractProductController {
    
    @Autowired
    private TransactionApplicationService transactionService;
    
    @GetMapping
    @Secure(roles = "TRANSACTION_VIEWER")
    public Mono<List<TransactionDto>> listTransactions(
            @PathVariable UUID contractId,
            @PathVariable UUID productId,
            ServerWebExchange exchange) {
        
        // Validate both IDs are present
        requireContext(contractId, productId);
        
        // Optional: Log the operation
        logOperation(contractId, productId, "listTransactions");
        
        return transactionService.listTransactions(exchange, contractId, productId);
    }
}
```

**Available methods:**
- `requireContractId(UUID)` - Validates contractId is not null
- `requireProductId(UUID)` - Validates productId is not null  
- `requireContext(UUID, UUID)` - Validates both contractId and productId
- `logOperation(...)` - Logs operation with contract/product context

**Note:** These are **completely optional** helper classes. Use them only if they help you.

---

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
- Fetch party permissions in contract
- Validate contract status

### 3. Product Management (`common-platform-product-mgmt-sdk`)
- Resolve product information
- Fetch product-specific configuration
- Validate product status and availability
- Get product features and limits

### 4. Configuration Management (`common-platform-config-mgmt-sdk`)
- Fetch tenant configuration
- Get provider configurations (KYC, payment gateways, etc.)
- Retrieve feature flags
- Manage tenant-specific settings

### 5. Security Center (Future)
- Complex authorization policies
- Attribute-Based Access Control (ABAC)
- Audit trail of authorization decisions
- Policy evaluation and enforcement

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

**180 tests - 100% passing**

The library includes extensive test coverage:
- **Context Management**: 63 tests covering AppContext, AppConfig, AppMetadata, ApplicationExecutionContext
- **Security Components**: 30 tests for AppSecurityContext, SecurityEvaluationResult, EndpointSecurityRegistry, Authorization
- **Infrastructure**: 37 tests for Configuration, Resolvers, Controllers, Services
- **Integration Tests**: 25 tests for AOP Security and Metadata Provider
- **Monitoring**: 11 tests for Health and Actuator
- **Configuration**: 14 tests for ApplicationLayerProperties and Banner

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AppContextTest

# With coverage report
mvn clean test jacoco:report
```

**Test Metrics:**
- Total Tests: 180
- Success Rate: 100%
- Execution Time: < 3s
- Core Coverage: ~95%+

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
