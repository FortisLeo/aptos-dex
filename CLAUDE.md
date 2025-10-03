# Web3 Android App — Engineering Guidelines (Kotlin + Compose + Aptos)

> Purpose: a compact, actionable engineering handbook for building a production-ready web3 android app (kotlin + compose) that targets the Aptos blockchain. This file is written for engineers and focuses on architecture, coding principles, UI guidelines, security, testing, and operational best practices.

---

## Table of contents

1. Project summary
2. Tech stack & links
3. High-level architecture
4. Folder & module layout (recommended)
5. Coding principles and conventions
6. Use-case and domain layer guidance
7. UI / Compose guidelines
8. Blockchain (Aptos) integration patterns
9. Security and secrets handling
10. Testing strategy
11. CI / CD and release notes
12. Observability, logging and error handling
13. Performance, profiling & optimization
14. Developer checklist
15. Appendix: small examples & patterns

---

## 1. Project summary

* Goal: Build a modern, scalable, maintainable Android dApp using Kotlin + Jetpack Compose and Aptos as the blockchain backend.
* Non-functional requirements to prioritize: correctness, security, testability, maintainability, and a clean UX consistent with modern web3 aesthetics.

## 2. Tech stack & links

* Kotlin (latest stable)
* Jetpack Compose
* Android Gradle Plugin (compatible stable version)
* Coroutines + Flow for concurrency
* Hilt (or Koin) for DI
* Retrofit/OkHttp for any off-chain APIs
* Room or Proto DataStore for local persistent data
* Aptos Kotlin SDK: [https://aptos.dev/build/sdks/community-sdks/kotlin-sdk](https://aptos.dev/build/sdks/community-sdks/kotlin-sdk)
* Aptos docs: [https://aptos.dev/](https://aptos.dev/)

## 3. High-level architecture

Follow **Clean Architecture** + **MVVM** with strict boundaries:

* **Presentation**: Compose UI + ViewModels (androidx.lifecycle.ViewModel)
* **Domain**: UseCases / Interactors — pure Kotlin, unit-testable, no Android or platform dependencies
* **Data**: Repositories, mappers, network clients, and blockchain adapters
* **Platform / Integration**: Aptos SDK wrappers, key-store integration, wallet adapters

Principles to enforce:

* Single Responsibility Principle for classes and for *each method* (including boolean-returning methods)
* Dependency inversion: domain never depends on data or platform
* DRY: extract common logic to shared utilities or base classes
* Keep functions short (preferred: < 40 lines) and give priority to expressive names

## 4. Folder & module layout (recommended)

Prefer multiple modules for clear boundaries in larger projects. Example:

```
/app (android app module)
/core (generic utilities, logging, common models)
:domain (usecases, domain models, interfaces)
:data (repositories, network, local db, adapters)
:blockchain-aptos (aptos wrappers, signing, transaction builders)
:ui-common (shared compose components, themes)
```

Small projects may combine some modules but keep clear package separation.

## 5. Coding principles and conventions

* Follow SOLID and Clean Architecture strictly.
* **Single responsibility for methods** — boolean methods should do one check and be named clearly (`isUserAuthorized`, `hasEnoughBalance`). Avoid combining checks.
* Method length: keep it short and readable. If a method needs more than one comment to explain, split it.
* Prefer immutable data (`val`) and data classes for models.
* Minimize side-effects; functions that mutate should be clearly named and isolated.
* Use Kotlin idioms: sealed classes for typed results, `Result` wrappers or `Either` for error handling in domain layer.
* Avoid repetitive null-checks — model properly with non-null types and explicit error flows.
* Use explicit interface boundaries for repositories and services; implementation details belong in data modules.
* Keep public functions well-documented with KDoc for edge cases, inputs, and outputs.

Naming and readability:

* `getX` for retrieval, `fetchX` for network-bound fetch, `observeX` for flows/streams, `saveX` for persistence.
* Boolean methods should start with `is`, `has`, `should`, `can`.

Refactoring & duplication:

* If two methods share logic, extract a descriptive helper and place it in a utilities package or domain-common service.
* Prefer composition over inheritance for reuse.

## 6. Use-case and domain layer guidance

* Create small, focused use-case classes that execute a single domain action (e.g. `SendTransactionUseCase`, `FetchAccountBalanceUseCase`).
* Each use-case should accept simple inputs and return a typed result (sealed class) or `Result<T>`.
* Business validation belongs to domain layer; data layer only performs persistence/transport.
* Use-case example pattern:

```kotlin
class SendTransactionUseCase(
  private val walletRepository: WalletRepository,
  private val txBuilder: TransactionBuilder
) {
  suspend operator fun invoke(request: SendTxRequest): Result<TxHash> {
    // validate
    // build
    // sign & submit
    // return Result
  }
}
```

* Keep side effects explicit — network or DB operations happen via repository interfaces injected into use-cases.

## 7. UI / Compose guidelines

* Compose-only UI layer. ViewModels expose `StateFlow`/`SharedFlow`/`UiState` immutable objects.
* Avoid heavy logic in Composables — they should be declarative and small.
* Use a single source of truth for UI state (ViewModel), surface minimal ephemeral state via `remember` or `mutableStateOf` when needed.
* Follow atomic component design: small composables (Button, Input, Card) combined into larger screens.
* Theming: provide a scalable theme (colors, typography, shapes) and support dark mode.
* Accessibility: contentDescription, minimum touch targets, scalable fonts.
* Navigation: use Jetpack Navigation for Compose; keep navigation events at ViewModel level exposed as events.
* UI animations: lightweight, purposeful; keep them performant and cancellable.
* Keep Compose previews for critical components to speed up visual iteration.

Design & aesthetics:

* Follow common web3 app patterns: onboarding flow with wallet connect/creation, dashboard with balances and recent activity, token/collectible detail views, transaction flow with clear confirmations and gas/fee visibility.
* **Consistency & theming**: Always examine existing screens before creating new ones. Use `MaterialTheme.colorScheme`, `MaterialTheme.typography`, and `MaterialTheme.shapes` exclusively—never hardcode colors or text styles. Maintain consistent spacing tokens (8.dp, 16.dp, 24.dp, 32.dp, 48.dp). Extract repeated UI patterns to reusable composables in `ui/components/`. Screen composables live in `presentation/{feature}/` with their ViewModels.

## 8. Blockchain (Aptos) integration patterns

* Encapsulate SDK interactions behind a `BlockchainClient` / `AptosGateway` interface in `blockchain-aptos` module.
* Keep raw SDK calls in the integration module. Map SDK models to domain models at the boundary.
* Handle retries, backoff, and idempotency at the data/platform layer — domain remains pure.
* Signing strategies:

    * For custodial wallets: server-signed or secure enclave approaches (see Security below).
    * For non-custodial wallets: integrate wallet connectors and store keys in secure storage (Android Keystore or StrongBox).
* Always verify transaction finality and confirm statuses with polling or webhook-backed notifications (off-chain service) rather than assuming immediate success.
* Expose abstractions for:

    * `getAccount(address)`, `getBalance(address, token)`, `buildTransaction(...)`, `signTransaction(...)`, `submitTransaction(...)`, `getTransactionStatus(txHash)`.
* Keep gas/fee estimating logic centralized and exposed through a single use-case.

## 9. Security and secrets handling

* Never hardcode secrets, keys, or RPC endpoints. Use build-time config and secure storage.
* Private keys: prefer Android Keystore / StrongBox. If keys must be exported to server, use end-to-end encryption and explicit user consent.
* Use secure network defaults: TLS, certificate pinning for critical endpoints where feasible.
* Validate all inputs before sending to blockchain to avoid malformed transactions.
* Protect against replay and double-submission: guard use-cases with idempotency tokens.
* Protect third-party libraries and review SDKs for security posture.


## 12. Observability, logging and error handling

* Centralized logging with structured logs. Use levels (DEBUG/INFO/WARN/ERROR).
* Avoid logging private keys and sensitive PII.
* Instrument critical flows with metrics (transaction submission latency, failure rates).
* Use Sentry/Datadog or similar for crash reporting; attach contextual metadata (user id hash, app version, network).
* Error handling strategy:

    * Domain: return typed errors (sealed classes) so UI can map to user-friendly messages.
    * Data layer: translate SDK/network errors to domain-level errors.
    * UI: show actionable messages and provide retry paths.

## 13. Performance, profiling & optimization

* Keep network calls off the main thread (use Coroutines + Dispatchers.IO).
* Use paging for lists (transactions, tokens) to avoid large UI renders.
* Leverage Compose lazy components for large collections.
* Profile with Android Profiler for memory leaks and frame drops; watch for long recompositions.

## 14. Developer checklist (pre-merge / pre-release)

* [ ] All public interfaces documented (KDoc)
* [ ] No hard-coded secrets or endpoints
* [ ] DI graph review for singleton / scope errors
* [ ] App permissions minimal and justified
* [ ] Logging does not leak secrets
* [ ] Compose previews for main screens
* [ ] Performance tests for transaction-heavy flows
* [ ] Security review of key storage and signing

## 15. Appendix: small examples & patterns

### 15.1 Use-case result pattern

```kotlin
sealed class SendTxResult {
  data class Success(val hash: String): SendTxResult()
  data class Failure(val reason: SendTxError): SendTxResult()
}

suspend fun sendTx(...): SendTxResult { /* ... */ }
```

### 15.2 Repository interface (domain boundary)

```kotlin
interface WalletRepository {
  suspend fun getBalance(address: String, token: String): Result<BigDecimal>
  suspend fun submitTransaction(signedTx: ByteArray): Result<String> // txHash
}
```

### 15.3 ViewModel exposure

```kotlin
class WalletViewModel(
  private val getBalance: GetBalanceUseCase
): ViewModel() {
  private val _uiState = MutableStateFlow(WalletUiState())
  val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

  fun refresh() {
    viewModelScope.launch {
      when(val res = getBalance(address)){
        is Result.Success -> _uiState.update { it.copy(balance = res.value) }
        is Result.Failure -> _uiState.update { it.copy(error = res.error) }
      }
    }
  }
}
```

---

## Final notes & suggestions

* Keep contracts and on-chain interactions isolated, tested, and auditable.
* If you expect heavy on-chain interactions, consider an off-chain service to batch/relay transactions when appropriate.
* When in doubt, prefer explicitness over cleverness: clear code is always easier to audit and secure.
