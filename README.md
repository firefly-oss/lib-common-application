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

### Architecture Complete – Controller-Based Context Resolution!

This library provides a **fully integrated, controller-based** application layer:
- ✅ **@FireflyApplication** annotation for application metadata and service discovery
- ✅ **Context Architecture** (AppContext, AppConfig, AppSecurityContext, ApplicationExecutionContext)
- ✅ **@Secure Annotation** system for declarative security
- ✅ **🎯 Three Base Controllers** – `AbstractApplicationController`, `AbstractContractController`, `AbstractProductController`
- ✅ **🎯 Automatic Context Resolution** – Party/Tenant from Istio headers + Contract/Product from path variables
- ✅ **🎯 Default Config Resolver** – Fetches tenant configuration automatically
- ✅ **🎯 Default Security Authorization** – Validates roles/permissions automatically
- ✅ **Abstract Application Service** base class for business orchestration
- ✅ **AOP Interceptors** for annotation processing
- ✅ **Spring Boot Auto-configuration**
- ✅ **Actuator Integration** with application metadata exposed in /actuator/info

**✨ Extend the appropriate controller base class and call `resolveExecutionContext()` – full context resolution is automatic!**

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
- `common-platform-config-mgmt-sdk` - Tenant resolution (partyId → tenantId), configuration, providers, feature flags
- `FireflySessionManager` (Security Center) - Party sessions, contract access, roles, permissions, role scopes
- `common-platform-product-mgmt-sdk` - Product information, product-specific config (optional)

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

### 3. Choose Your Controller Base Class

**The library provides three base controller classes** that automatically resolve context based on your endpoint's scope:

#### 🎯 Architecture: How Context Resolution Works

```
┌──────────────────────────────────────────────────┐
│        Istio Gateway (Authentication)            │
│  - Validates JWT                                 │
│  - Injects X-Party-Id header (from JWT sub)      │
└──────────────────────┬───────────────────────────┘
                         │
                         ↓
┌──────────────────────────────────────────────────┐
│        Your Controller (Extracts Path Variables) │
│  - Extends AbstractApplicationController         │
│    OR AbstractContractController                 │
│    OR AbstractProductController                  │
│  - Extracts contractId from @PathVariable        │
│  - Extracts productId from @PathVariable         │
│  - Calls resolveExecutionContext(exchange, ...)  │
└──────────────────────┬───────────────────────────┘
                         │
                         ↓
┌──────────────────────────────────────────────────┐
│        DefaultContextResolver (Library)          │
│  1. Extracts partyId from X-Party-Id header      │
│  2. Calls config-mgmt to get tenantId(by partyId)│
│  3. Uses contractId/productId from controller    │
│  4. Calls FireflySessionManager (Security Center)│
│     - Get party session (contracts,roles,scopes) │
│     - Validate contract access                   │
│     - Get roles for contract/product             │
│     - Derive permissions from roles              │
└──────────────────────────────────────────────────┘
```

**Key Points:**
- ✅ **Istio handles authentication** → Injects `X-Party-Id` header (from JWT)
- ✅ **Config-mgmt resolves tenant** → `GET /api/v1/parties/{partyId}/tenant`
- ✅ **Controllers extract path variables** → `@PathVariable UUID contractId/productId`
- ✅ **FireflySessionManager (Security Center)** → Provides party session: contracts, roles, permissions, scopes
- ✅ **Library resolves full context** → Party + Tenant + Contract + Product + Roles + Permissions + Config
- ✅ **@Secure / EndpointSecurityRegistry** → Validates authorization using resolved context

#### 🎯 Option 1: Application-Layer Endpoints (Onboarding, Product Catalog)

Use `AbstractApplicationController` for endpoints that don't require contract or product context:

```java
@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController extends AbstractApplicationController {
    
    @Autowired
    private OnboardingApplicationService onboardingService;
    
    @PostMapping("/start")
    @Secure(requireParty = true, requireRole = "customer:onboard")
    public Mono<OnboardingResponse> startOnboarding(
            @RequestBody OnboardingRequest request,
            ServerWebExchange exchange) {
        
        // Automatically resolves: party + tenant (no contract/product)
        return resolveExecutionContext(exchange)
            .flatMap(context -> onboardingService.startOnboarding(context, request));
    }
}
```

**Context resolved:** Party ID, Tenant ID, Roles, Permissions, Config

---

#### 🎯 Option 2: Contract-Scoped Endpoints (Accounts, Beneficiaries)

Use `AbstractContractController` for endpoints scoped to a contract:

```java
@RestController
@RequestMapping("/api/v1/contracts/{contractId}/accounts")
public class AccountController extends AbstractContractController {
    
    @Autowired
    private AccountApplicationService accountService;
    
    @GetMapping
    @Secure(requireParty = true, requireContract = true, requireRole = "account:viewer")
    public Mono<List<AccountDTO>> listAccounts(
            @PathVariable UUID contractId,
            ServerWebExchange exchange) {
        
        // Automatically resolves: party + tenant + contract
        return resolveExecutionContext(exchange, contractId)
            .flatMap(context -> accountService.listAccounts(context));
    }
}
```

**Context resolved:** Party ID, Tenant ID, Contract ID, Roles (party+contract), Permissions, Config

---

#### 🎯 Option 3: Product-Scoped Endpoints (Transactions, Cards)

Use `AbstractProductController` for endpoints scoped to a product within a contract:

```java
@RestController
@RequestMapping("/api/v1/contracts/{contractId}/products/{productId}/transactions")
public class TransactionController extends AbstractProductController {
    
    @Autowired
    private TransactionApplicationService transactionService;
    
    @GetMapping
    @Secure(requireParty = true, requireContract = true, requireProduct = true, requireRole = "transaction:viewer")
    public Mono<List<TransactionDTO>> listTransactions(
            @PathVariable UUID contractId,
            @PathVariable UUID productId,
            ServerWebExchange exchange) {
        
        // Automatically resolves: party + tenant + contract + product
        return resolveExecutionContext(exchange, contractId, productId)
            .flatMap(context -> transactionService.listTransactions(context));
    }
}
```

**Context resolved:** Party ID, Tenant ID, Contract ID, Product ID, Roles (party+contract+product), Permissions, Config

---

**What the library handles automatically:**
- ✅ Extracts `partyId` from Istio header (`X-Party-Id`)
- ✅ Resolves `tenantId` by calling `common-platform-config-mgmt` with the partyId
- ✅ Uses `contractId` and `productId` from your `@PathVariable` annotations
- ✅ Enriches context with roles and permissions from platform SDKs
- ✅ Loads tenant configuration (providers, feature flags, settings)
- ✅ Validates security requirements from `@Secure` annotations
- ✅ Returns 401/403 for unauthorized requests

---

#### 🔧 Advanced: Custom Context Resolution (Optional)

If you need custom context resolution logic (e.g., non-Istio environments), you can provide your own implementations:

**Custom Context Resolver:**
```java
@Component
@Primary // Override the default
public class CustomContextResolver extends AbstractContextResolver {
    
    @Override
    public Mono<UUID> resolvePartyId(ServerWebExchange exchange) {
        // Custom logic: extract from JWT, session, etc.
        return extractFromJWT(exchange, "sub");
    }
    
    @Override
    public Mono<UUID> resolveTenantId(ServerWebExchange exchange) {
        // Custom logic: extract from subdomain, header, etc.
        return extractFromSubdomain(exchange);
    }
}
```

**Custom Config Resolver:**
```java
@Component
@Primary // Override the default
public class CustomConfigResolver extends AbstractConfigResolver {
    
    private final ConfigManagementClient configClient;
    
    @Override
    protected Mono<AppConfig> fetchConfigFromPlatform(UUID tenantId) {
        return configClient.getTenantConfig(tenantId)
            .map(response -> AppConfig.builder()
                .tenantId(response.getTenantId())
                .tenantName(response.getName())
                .providers(response.getProviders())
                .featureFlags(response.getFeatureFlags())
                .settings(response.getSettings())
                .build());
    }
}
```


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

## Key Features

### 🎯 Context Management (Controller-Based)
- **Istio Integration**: `X-Party-Id` extracted from header (Istio-injected after JWT validation)
- **Tenant Resolution**: `tenantId` resolved by calling `common-platform-config-mgmt` with partyId
- **Path Variable Extraction**: `contractId` and `productId` extracted from `@PathVariable` in controllers
- **Automatic Enrichment**: Roles and permissions fetched from platform SDKs based on party+contract+product
- **Three Controller Types**: `AbstractApplicationController`, `AbstractContractController`, `AbstractProductController`
- **Flexible Scoping**: Support application-layer, party+contract, and party+contract+product contexts
- **Caching**: Built-in caching for performance optimization
- **Immutability**: Thread-safe context objects

### 🔒 Security & Authorization (Controller-Based)
- **Declarative**: `@Secure` annotation with `requireParty`, `requireContract`, `requireProduct`, `requireRole`, `requirePermission`
- **Context-Aware**: Security validation based on fully resolved ApplicationExecutionContext
- **Default Authorization**: `DefaultSecurityAuthorizationService` validates roles and permissions automatically
- **SecurityCenter Ready**: Integration points for complex authorization policies
- **Role & Permission Based**: Fine-grained access control based on party role in contract/product
- **Flexible**: Works with all three controller types (party, contract, product)

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

### 1. Configuration Management (`common-platform-config-mgmt-sdk`) ⭐
**Purpose:** Tenant resolution and multi-tenant configuration
- **Tenant Resolution:** `GET /api/v1/parties/{partyId}/tenant` → Returns tenantId for a party
- Fetch tenant configuration (providers, settings)
- Get provider configurations (KYC, payment gateways, etc.)
- Retrieve feature flags
- Manage tenant-specific settings

### 2. FireflySessionManager (Security Center) ⭐⭐⭐
**Purpose:** Authorization, session management, role/permission resolution
- **Party Session:** Track which contracts a party has access to
- **Contract Access:** Validate if party can access specific contract/product
- **Role Resolution:** Get party roles in contract (owner, viewer, etc.)
- **Role Scopes:** Support party-level, contract-level, product-level roles
- **Permission Derivation:** Convert roles to permissions using role mappings
- **Session Caching:** Cache party sessions for performance

**Example Flow:**
```java
// 1. Get party session
PartySession session = sessionManager.getPartySession(partyId, tenantId);

// 2. Check contract access
boolean hasAccess = session.hasContractAccess(contractId);

// 3. Get roles for contract
Set<String> roles = session.getContractRoles(contractId, productId);
// Returns: ["owner", "account:viewer", "transaction:creator"]

// 4. Derive permissions
Set<String> permissions = session.getPermissionsForRoles(roles);
// Returns: ["account:read", "account:update", "transaction:create"]
```

### 3. Product Management (`common-platform-product-mgmt-sdk`) (Optional)
**Purpose:** Product-specific information and configuration
- Resolve product information
- Fetch product-specific configuration
- Validate product status and availability
- Get product features and limits

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
