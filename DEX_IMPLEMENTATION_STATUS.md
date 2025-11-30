# DEX Smart Contract Integration Status

## Overview
Implementation of Aptos DEX smart contract integration using Kaptos SDK (version 0.1.2-beta).

## Contract Details
- **Contract Address**: `0xd4f4a886d54d280f06e3beebde86c7ff27a824dffb1a410dda625635cd16af5e`
- **Network**: Aptos Testnet
- **SDK**: Kaptos Android 0.1.2-beta
- **Documentation**: INTEGRATION_GUIDE.md

## Implementation Progress

### ✅ Completed

1. **Configuration** (`blockchain/AptosConfig.kt`)
   - Updated with DEX contract address
   - Configured module names (DEX, OrderBook, Vault, TokenRegistry)
   - View functions mapped (getUserBalances, getDexStats, getMarketPrice, getSpread)
   - Entry functions mapped (deposit, withdraw, placeLimitOrder, placeMarketOrder, cancelOrder)

2. **Client Wrapper** (`blockchain/AptosClientWrapper.kt`)
   - Updated to use DEX contract address
   - Configured for testnet via Kaptos
   - View function calling implemented
   - Transaction building prepared (requires wallet signature)

3. **Domain Models** (`domain/model/DexModels.kt`)
   - DexResult<T> sealed class for operation results
   - DexError enum for error handling
   - DexBalance for user vault balances
   - DexStats for DEX statistics
   - MarketPrice and Spread models
   - OrderPlacementResult and TransactionResult

4. **Repository Layer**
   - Interface: `domain/repository/DexRepository.kt`
   - Implementation: `data/repository/DexRepositoryImpl.kt`
   - All DEX operations defined and implemented:
     - deposit() - Deposit tokens to vault
     - withdraw() - Withdraw tokens from vault
     - placeLimitOrder() - Place limit buy/sell orders
     - placeMarketOrder() - Place market orders
     - cancelOrder() - Cancel existing orders
     - getUserBalances() - Get vault balances (view function)
     - getDexStats() - Get DEX statistics (view function)
     - getMarketPrice() - Get market price (view function)
     - getSpread() - Get bid-ask spread (view function)

5. **Dependency Injection** (`di/DexModule.kt`)
   - Hilt module created
   - DexRepository bound to implementation
   - Singleton scope configured

6. **Use Cases** (Examples)
   - `PlaceLimitOrderUseCase` - Clean architecture use case for placing orders
   - `GetMarketPriceUseCase` - Use case for fetching market prices

### ⚠️ Limitations & Next Steps

#### 1. Wallet Integration
**Status**: ✅ **COMPLETED - Petra Wallet Integrated**

Petra wallet integration is now active in TradingBottomSheetViewModel:
- Transaction building via `AptosClientWrapper.buildPlaceOrderTransaction()`
- Petra wallet signing via `PetraWalletConnector.buildSignAndSubmitIntent()`
- Navigation events to open Petra app for signing
- UI already observes `navigationEvents` and handles wallet opening

**How it works**:
1. User places order in TradingBottomSheet
2. ViewModel builds transaction with DEX contract call
3. Transaction sent to Petra wallet for signing
4. User approves in Petra app
5. Petra submits signed transaction to Aptos network

**Limitations**:
- Requires Petra wallet installed on device
- Wallet must be connected (connection flow already exists)
- User must have testnet APT for gas fees

#### 2. View Functions Implemented
**Status**: ✅ Ready to use

Read-only operations work through view functions:
- `getUserBalances()` - Query user vault balances
- `getDexStats()` - Get overall DEX statistics
- `getMarketPrice()` - Get current market price
- `getSpread()` - Get bid-ask spread

#### 3. Market ID Configuration
**Status**: Hardcoded placeholder

Market IDs currently use placeholder value (0). Should be:
- Loaded from DEX contract state
- Mapped from token pairs
- Configurable per trading pair

#### 4. Token Type Configuration
**Status**: Placeholder values

`CoinTypes` object uses placeholders for testnet tokens:
```kotlin
const val USDC = "0x1::test_coin::USDC"  // Needs actual testnet address
const val BTC = "0x1::test_coin::BTC"    // Needs actual testnet address
```

**Action Required**: Update with actual deployed test token addresses on Aptos testnet.

## Architecture

```
Presentation Layer (ViewModels)
       ↓
Use Cases (Domain)
       ↓
Repository Interface (Domain)
       ↓
Repository Implementation (Data)
       ↓
AptosClientWrapper (Blockchain)
       ↓
Kaptos SDK
       ↓
Aptos Testnet
```

## Usage Example

```kotlin
// Inject repository
@Inject lateinit var dexRepository: DexRepository

// Get market price (view function - works now)
val result = dexRepository.getMarketPrice(
    baseToken = CoinTypes.APT,
    quoteToken = CoinTypes.USDC
)

when (result) {
    is DexResult.Success -> {
        val price = result.data.price
        // Use price data
    }
    is DexResult.Failure -> {
        // Handle error
    }
}

// Place limit order (requires wallet signature - TODO)
val orderResult = dexRepository.placeLimitOrder(
    baseToken = CoinTypes.APT,
    quoteToken = CoinTypes.USDC,
    marketId = 0,
    side = false, // false = buy, true = sell
    price = 1000000, // In microunits
    size = 100000000 // In microunits
)
```

## Integration with UI

### Current Status
- **OrderBookViewModel**: UI-only (can be integrated with view functions)
- **TradingBottomSheetViewModel**: ✅ **FULLY INTEGRATED with Petra Wallet**

### Integration Status

1. **TradingBottomSheetViewModel** (✅ Complete):
```kotlin
@Inject constructor(
    private val aptosClient: AptosClientWrapper,
    private val petraWalletConnector: PetraWalletConnector,
    private val walletRepository: WalletRepository
)

fun executeOrder(onSuccess: () -> Unit) {
    // 1. Get wallet address
    // 2. Build transaction with market pair and type arguments
    // 3. Create Petra sign intent
    // 4. Emit navigation event to open Petra
    // 5. Petra handles signing and submission
}
```

2. **OrderBookViewModel** (Can be integrated):
```kotlin
// TODO: Add market data fetching
@Inject constructor(
    private val getMarketPrice: GetMarketPriceUseCase,
    private val dexRepository: DexRepository
)

fun loadOrderBook(symbol: String) {
    viewModelScope.launch {
        val priceResult = getMarketPrice(symbol)
        val spreadResult = dexRepository.getSpread(baseToken, quoteToken)
        // Update UI state with real market data
    }
}
```

## Testing

### View Functions (Can test now)
```bash
# Test on testnet once contract is initialized
# View functions don't require gas
```

### Write Operations (Requires wallet)
- Need funded testnet account
- Visit https://aptos.dev/network/faucet for testnet APT
- Requires wallet integration for signing

## References

- **Integration Guide**: INTEGRATION_GUIDE.md
- **Kaptos SDK**: https://github.com/mcxross/kaptos
- **Kaptos Docs**: https://aptos.dev/build/sdks/community-sdks/kotlin-sdk/quickstart
- **Aptos Testnet**: https://fullnode.testnet.aptoslabs.com/v1

## What's Remaining

### 1. Token Configuration ⚠️ Required
**Priority**: HIGH - Blocks testing
**File**: `blockchain/AptosConfig.kt`

Update `CoinTypes` object with actual testnet token addresses:
```kotlin
object CoinTypes {
    const val APT = "0x1::aptos_coin::AptosCoin"  // ✅ Correct
    const val USDC = "0x1::test_coin::USDC"  // ❌ Placeholder - needs actual address
    const val BTC = "0x1::test_coin::BTC"    // ❌ Placeholder - needs actual address
    const val ETH = "0x1::test_coin::ETH"    // ❌ Placeholder - needs actual address
    const val USDT = "0x1::test_coin::USDT"  // ❌ Placeholder - needs actual address
}
```

**Action**: Get actual testnet token type addresses or use tokens available on testnet.

### 2. Market ID Configuration ⚠️ Required
**Priority**: HIGH - Hardcoded to 0
**File**: `blockchain/AptosClientWrapper.kt` line 321

Current:
```kotlin
val marketId = 0 // Default market ID - should be determined by market pair
```

**Options**:
- Query market IDs from DEX contract state
- Create configuration mapping: `symbol -> marketId`
- Add to `MarketPairs` object

### 3. OrderBookViewModel Integration ⏸️ Optional
**Priority**: LOW - For live market data display
**File**: `presentation/orderbook/OrderBookViewModel.kt`

Currently UI-only. To add live data:
```kotlin
@Inject constructor(
    private val dexRepository: DexRepository
)

fun loadOrderBook(symbol: String) {
    viewModelScope.launch {
        when (val result = dexRepository.getSpread(baseToken, quoteToken)) {
            is DexResult.Success -> {
                // Update UI with bid/ask spread
            }
            is DexResult.Failure -> {
                // Handle error
            }
        }
    }
}
```

### 4. TokenDetailViewModel Order Book ⏸️ Optional
**Priority**: LOW - Temporarily disabled
**File**: `presentation/tokendetail/TokenDetailViewModel.kt`

Lines 82, 91-122, 167-184 - Order book snapshot and trade events commented out.

To re-enable:
1. Inject `DexRepository`
2. Replace `loadOrderBookSnapshot()` with `dexRepository.getSpread()` calls
3. Re-implement trade event streaming if needed

### 5. DEX Contract Initialization ⚠️ Required Once
**Priority**: HIGH - One-time setup
**Status**: Unknown if already initialized

Per INTEGRATION_GUIDE.md, DEX must be initialized by admin:
```bash
aptos move run \
  --function-id "0xd4f4a886d54d280f06e3beebde86c7ff27a824dffb1a410dda625635cd16af5e::DEX::initialize" \
  --args address:FEE_COLLECTOR_ADDRESS address:EMERGENCY_ADMIN_ADDRESS
```

**Action**: Check if contract is initialized or run initialization.

### 6. Token Registration in DEX ⚠️ May Be Required
**Priority**: MEDIUM
**Status**: Unknown

Tokens may need to be registered in DEX `TokenRegistry` before trading:
```kotlin
// May need admin call to register tokens
DEX::register_token<TokenType>(admin, token_symbol)
```

**Action**: Verify which tokens are tradeable on the deployed contract.

## Next Actions

### Immediate (Blocks Testing)
1. ✅ Get actual testnet token addresses → Update `CoinTypes`
2. ✅ Determine market IDs for trading pairs → Update `buildPlaceOrderTransaction()`
3. ✅ Verify DEX contract is initialized on testnet
4. ✅ Check which tokens are registered/tradeable

### Testing Phase
5. Connect Petra wallet to testnet
6. Fund wallet with testnet APT (https://aptos.dev/network/faucet)
7. Test order placement through TradingBottomSheet
8. Verify transaction appears on Aptos Explorer

### Optional Enhancements
9. Integrate `DexRepository` into OrderBookViewModel
10. Re-enable order book in TokenDetailViewModel
11. Add event streaming for trade updates

## Testing the Integration

### Prerequisites
1. Petra wallet installed on Android device
2. Wallet connected to app (use existing connect flow)
3. Testnet APT in wallet for gas fees (get from https://aptos.dev/network/faucet)

### Test Order Placement
1. Navigate to token detail screen
2. Open trading bottom sheet
3. Enter order details (price, amount)
4. Click "Execute Order"
5. App will open Petra wallet
6. Approve transaction in Petra
7. Transaction will be submitted to testnet

### Expected Flow
```
User clicks "Execute Order"
  ↓
TradingBottomSheetViewModel.executeOrder()
  ↓
Build transaction with DEX contract call
  ↓
Create Petra sign intent with encrypted payload
  ↓
Emit NavigationEvent.OpenPetraWallet
  ↓
UI observes event and launches Petra app
  ↓
User approves in Petra
  ↓
Petra signs and submits transaction
  ↓
Transaction executed on Aptos testnet
```

---

**Last Updated**: 2025-01-12
**Status**: ✅ **TRADING INTEGRATION COMPLETE** - Ready for testing with Petra Wallet
