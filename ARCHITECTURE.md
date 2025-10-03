# AptosDEX Architecture

## Overview
This project follows **Clean Architecture** principles with clear separation between Domain, Data, Blockchain Integration, and Presentation layers as specified in CLAUDE.md.

## Architecture Layers

### 1. Domain Layer (`domain/`)
Pure Kotlin business logic with no Android dependencies.

#### Models
- `WalletConnection` - Domain model for wallet connection state
- `ConnectionKeys` - Cryptographic key pairs
- `ConnectionResult` - Sealed class for connection outcomes
- `BalanceResult` - Sealed class for balance fetch outcomes

#### Repository Interfaces
- `WalletRepository` - Interface for wallet data operations
- `CryptoRepository` - Interface for cryptographic operations

#### Use Cases
- `ConnectWalletUseCase` - Initiates wallet connection flow
- `HandleConnectionApprovalUseCase` - Processes Petra's connection approval
- `FetchBalanceUseCase` - Retrieves wallet balance from Aptos
- `SaveConnectionStateUseCase` - Persists connection state
- `RestoreConnectionStateUseCase` - Restores connection state on app restart

### 2. Data Layer (`data/`)
Implements repository interfaces and handles data operations.

#### Repository Implementations
- `WalletRepositoryImpl` - Implements wallet data operations
  - Coordinates between API client and local storage
  - Maps network responses to domain models
  - Handles error cases gracefully

- `CryptoRepositoryImpl` - Delegates to blockchain layer

#### Remote Data Source
- `AptosApiClient` - Handles Aptos testnet API calls
  - Balance fetching using official endpoint
  - Custom exceptions for error handling
  - Coroutines for async operations

#### Local Data Source
- `ConnectionStateStorage` - SharedPreferences wrapper
  - Persists wallet connection state
  - Stores encrypted keys as hex strings
  - Provides restore functionality

### 3. Blockchain-Aptos Layer (`blockchain/`)
Encapsulates blockchain-specific logic and external SDK interactions.

#### Crypto Module
- `CryptoKeyManager` - Handles NaCl Box encryption
  - Key pair generation (Ed25519)
  - Shared key computation using Curve25519
  - Hex conversion utilities
  - Uses LazySodium library

#### Petra Integration
- `PetraWalletConnector` - Manages Petra wallet communication
  - Builds connection intents with deep links
  - Formats connection data per Petra spec
  - Checks Petra app availability

### 4. Presentation Layer (`presentation/`)
UI and ViewModel following MVVM pattern with feature-based modules.

#### Features

##### Wallet Feature (`presentation/wallet/`)
- `WalletViewModel` - Coordinates wallet connection use cases
  - Exposes `StateFlow<WalletUiState>` for UI
  - Handles deep link processing
  - Manages navigation events via `SharedFlow`
  - Orchestrates wallet connection flow
  - Fetches and updates balance

- `WalletUiState` - Immutable UI state data class
  - Connection status
  - Wallet address
  - Balance display
  - Loading and error states

- `WalletConnectScreen` - Composable UI
  - Displays connection status
  - Shows wallet address and balance
  - Connect button with state management
  - Pure declarative UI

##### Swap Feature (`presentation/swap/`)
- `SwapViewModel` - Manages swap functionality
  - Exposes `StateFlow<SwapUiState>` for UI
  - Handles reset action
  - Coordinates swap use cases (future)
  
- `SwapUiState` - Immutable UI state data class
  - Loading state
  - Error messages
  - Swap form data (future)

- `SwapScreen` - Composable UI
  - Displays "Swap tokens" interface
  - Reset button for clearing state
  - Token input/output fields (future)
  - Pure declarative UI

##### DEX Feature (`presentation/dex/`)
- `DexScreen` - Composable UI for DEX trading view
  - Placeholder for future DEX functionality
  - Will show trading pairs and liquidity pools

##### Home Feature (`presentation/home/`)
- `HomeScreen` - Main navigation container
  - Bottom navigation bar with tabs
  - Routes to Swap and DEX screens
  - Material3 NavigationBar component
  - Tab state management

##### Biometric Feature (`presentation/biometric/`)
- `BiometricViewModel` - Manages biometric authentication
- `BiometricPermissionScreen` - Biometric setup UI
- `BiometricAuthManager` - Biometric hardware integration

##### Notification Feature (`presentation/notification/`)
- `NotificationViewModel` - Manages notification permissions
- `NotificationPermissionScreen` - Permission request UI

#### Navigation
- `OnboardingNavigationManager` - Coordinates onboarding flow
- `AppScreen` - Sealed class defining app screens
- Deep link handling for Petra wallet callbacks

#### Main Entry Point
- `MainActivity` - FragmentActivity hosting Compose UI
  - Annotated with `@AndroidEntryPoint` for Hilt
  - Handles deep link intents
  - Manages onboarding flow
  - Minimal, thin Activity following best practices

### 5. Dependency Injection (`di/`)
Hilt modules for dependency graph.

#### Modules
- `AppModule` - Provides app-level dependencies
  - Gson instance
  - OkHttpClient with timeouts

- `NetworkModule` - Provides network dependencies
  - Aptos testnet base URL
  - AptosApiClient instance

- `RepositoryModule` - Binds repository implementations
  - WalletRepository binding
  - CryptoRepository binding

## Data Flow

### Connection Flow
```
User clicks "Connect"
  → ViewModel.connectWallet()
  → ConnectWalletUseCase generates keys
  → PetraWalletConnector builds intent
  → MainActivity opens Petra app
  → Petra returns via deep link
  → ViewModel.handleDeepLink()
  → HandleConnectionApprovalUseCase processes response
  → SaveConnectionStateUseCase persists state
  → FetchBalanceUseCase retrieves balance
  → UI updates via StateFlow
```

### Balance Fetch Flow
```
Address available
  → FetchBalanceUseCase.invoke(address)
  → WalletRepository.fetchBalance(address)
  → AptosApiClient.getBalance(address)
  → API: /v1/accounts/{address}/balance/0x1::aptos_coin::AptosCoin
  → Parse response (octas)
  → Convert to APT (divide by 100M)
  → Format string
  → Return BalanceResult.Success
  → ViewModel updates UI state
  → Compose recomposes with new balance
```

### State Restoration Flow
```
App starts
  → ViewModel.init
  → RestoreConnectionStateUseCase.invoke()
  → WalletRepository.restoreConnectionState()
  → ConnectionStateStorage reads SharedPreferences
  → Returns WalletConnection or null
  → ViewModel updates UI state
  → If connected, fetch fresh balance
```

## Key Design Decisions

### 1. Separation of Concerns
- Domain layer is pure Kotlin - no Android framework dependencies
- Use cases are single-responsibility and testable
- Repository interfaces defined in domain, implemented in data

### 2. Dependency Inversion
- Domain defines interfaces
- Data and blockchain layers implement interfaces
- Presentation depends on domain abstractions
- Hilt provides concrete implementations at runtime

### 3. Unidirectional Data Flow
- UI emits events (button clicks)
- ViewModel processes via use cases
- State flows down via StateFlow
- UI observes and renders state

### 4. Error Handling
- Sealed classes for typed errors (ConnectionResult, BalanceResult)
- Custom exceptions at data layer (AccountNotFoundException, NetworkException)
- User-friendly messages at presentation layer

### 5. State Management
- Single source of truth: WalletUiState in ViewModel
- Immutable state objects
- StateFlow for state observation
- SharedFlow for one-time events (navigation)

### 6. Cryptography Isolation
- All crypto operations in CryptoKeyManager
- Keys never exposed outside blockchain layer (except as hex strings for storage)
- Shared key computation encapsulated

### 7. Network Configuration
- Base URL injected via Hilt
- Easy to switch between testnet/mainnet
- Centralized timeout configuration

## Testing Strategy

### Unit Tests
- Domain layer: Test use cases in isolation
- Repository implementations: Mock API clients and storage
- ViewModels: Test state transitions and use case coordination

### Integration Tests
- Repository + API client interaction
- Storage persistence and restoration

### UI Tests
- Compose UI testing for WalletConnectScreen
- State-driven UI rendering verification

## Security Considerations

1. **Key Storage**: Private keys stored in SharedPreferences as hex
   - Future: Migrate to Android Keystore for hardware-backed security

2. **Network Security**: Using HTTPS for Aptos API
   - Future: Consider certificate pinning

3. **Input Validation**: All inputs validated before blockchain submission

4. **No Logging of Secrets**: Logs only show public keys and addresses

## Future Enhancements

1. **Transaction Signing**: Add use cases for signing and submitting transactions
2. **Multi-wallet Support**: Support multiple connected wallets
3. **Token Management**: Track and display multiple token types
4. **Swap Functionality**: Implement DEX trading features
5. **Testing**: Comprehensive unit and integration tests
6. **Error Recovery**: Better error handling and retry mechanisms
7. **Offline Support**: Cache balance and transaction history
8. **Mainnet Support**: Configuration for mainnet deployment

## Dependencies

### Core
- Kotlin Coroutines - Async operations
- Hilt - Dependency injection
- Jetpack Compose - UI framework
- ViewModel - State management

### Networking
- OkHttp - HTTP client
- Gson - JSON parsing

### Blockchain
- LazySodium - NaCl cryptography (Curve25519, XSalsa20, Poly1305)
- JNA - Native library access

### Aptos Integration
- Direct REST API calls (no SDK currently)
- Petra Wallet deep link protocol

## Compliance with CLAUDE.md

✅ Clean Architecture with clear boundaries
✅ SOLID principles enforced
✅ Single Responsibility for methods and classes
✅ Short, readable functions (< 40 lines preferred)
✅ Immutable data classes
✅ Explicit interface boundaries
✅ Use cases for domain logic
✅ Repository pattern
✅ Sealed classes for typed results
✅ Compose-only UI
✅ StateFlow for UI state
✅ ViewModels for state management
✅ Hilt for DI
✅ Coroutines for concurrency
✅ Structured logging
✅ Error handling with typed errors
✅ No hardcoded secrets or endpoints
✅ Security considerations documented

## File Structure
```
app/src/main/java/com/rishitgoklani/aptosdex/
├── AptosDexApplication.kt          # Hilt application class
├── MainActivity.kt                  # Main entry point (thin)
├── blockchain/
│   ├── crypto/
│   │   └── CryptoKeyManager.kt     # Encryption/key management
│   └── petra/
│       └── PetraWalletConnector.kt # Petra integration
├── data/
│   ├── local/
│   │   └── ConnectionStateStorage.kt # SharedPreferences wrapper
│   ├── remote/
│   │   └── AptosApiClient.kt       # API client
│   └── repository/
│       ├── CryptoRepositoryImpl.kt
│       └── WalletRepositoryImpl.kt
├── di/
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
├── domain/
│   ├── model/
│   │   ├── BalanceResult.kt
│   │   ├── ConnectionKeys.kt
│   │   ├── ConnectionResult.kt
│   │   └── WalletConnection.kt
│   ├── repository/
│   │   ├── CryptoRepository.kt     # Interface
│   │   └── WalletRepository.kt     # Interface
│   └── usecase/
│       ├── ConnectWalletUseCase.kt
│       ├── FetchBalanceUseCase.kt
│       ├── HandleConnectionApprovalUseCase.kt
│       ├── RestoreConnectionStateUseCase.kt
│       └── SaveConnectionStateUseCase.kt
└── presentation/
    ├── biometric/
    │   ├── BiometricAuthManager.kt
    │   ├── BiometricPermissionScreen.kt
    │   └── BiometricViewModel.kt
    ├── dex/
    │   └── DexScreen.kt
    ├── home/
    │   └── HomeScreen.kt           # Main navigation container
    ├── navigation/
    │   ├── AppScreen.kt
    │   ├── ConnectionStateTracker.kt
    │   └── OnboardingNavigationManager.kt
    ├── notification/
    │   ├── NotificationPermissionScreen.kt
    │   └── NotificationViewModel.kt
    ├── swap/
    │   ├── SwapScreen.kt
    │   ├── SwapUiState.kt
    │   └── SwapViewModel.kt
    └── wallet/
        ├── WalletConnectScreen.kt
        ├── WalletUiState.kt
        └── WalletViewModel.kt
```
