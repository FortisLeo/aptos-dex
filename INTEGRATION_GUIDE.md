# DEX Integration Guide

## Overview

This guide covers how to integrate with the deployed DEX smart contracts on Aptos testnet. The DEX provides a complete decentralized exchange with order books, market making, and settlement functionality.

## Contract Address

**DEX Contract Address**: `0xd4f4a886d54d280f06e3beebde86c7ff27a824dffb1a410dda625635cd16af5e`

## Architecture

The DEX consists of 7 core modules:

1. **DEX** - Main entry point for trading operations
2. **TokenRegistry** - Manages tradeable tokens
3. **MarketFactory** - Creates and manages trading pairs
4. **OrderBook** - Handles limit and market orders
5. **MatchingEngine** - Matches buy/sell orders
6. **Settlement** - Processes completed trades
7. **Vault** - Manages user token deposits/withdrawals

## Setup Instructions

### 1. Install Dependencies

```bash
# Install Aptos CLI
curl -fsSL "https://aptos.dev/scripts/install_cli.py" | python3

# Initialize your account
aptos init --network testnet
```

### 2. Fund Your Account

Visit the [Aptos Testnet Faucet](https://aptos.dev/network/faucet) to get test APT tokens.

### 3. Initialize the DEX (Admin Only)

```bash
# Initialize DEX with admin account
aptos move run \
  --function-id "0xd4f4a886d54d280f06e3beebde86c7ff27a824dffb1a410dda625635cd16af5e::DEX::initialize" \
  --args address:YOUR_FEE_COLLECTOR_ADDRESS address:YOUR_EMERGENCY_ADMIN_ADDRESS
```

## Integration Examples

### TypeScript/JavaScript SDK

```typescript
import { AptosClient, AptosAccount, HexString } from "aptos";

const client = new AptosClient("https://fullnode.testnet.aptoslabs.com/v1");
const dexAddress = "0xd4f4a886d54d280f06e3beebde86c7ff27a824dffb1a410dda625635cd16af5e";

class DEXIntegration {
  constructor(private account: AptosAccount) {}

  // Deposit tokens to DEX
  async deposit(tokenType: string, amount: number, symbol: string) {
    const payload = {
      type: "entry_function_payload",
      function: `${dexAddress}::DEX::deposit`,
      type_arguments: [tokenType],
      arguments: [dexAddress, amount.toString(), symbol]
    };

    const txnRequest = await client.generateTransaction(
      this.account.address(),
      payload
    );

    const signedTxn = await client.signTransaction(this.account, txnRequest);
    return await client.submitTransaction(signedTxn);
  }

  // Place limit order
  async placeLimitOrder(
    baseToken: string,
    quoteToken: string,
    marketId: number,
    side: boolean, // false = buy, true = sell
    price: number,
    size: number
  ) {
    const payload = {
      type: "entry_function_payload",
      function: `${dexAddress}::DEX::place_limit_order`,
      type_arguments: [baseToken, quoteToken],
      arguments: [
        dexAddress,
        marketId.toString(),
        side,
        price.toString(),
        size.toString()
      ]
    };

    const txnRequest = await client.generateTransaction(
      this.account.address(),
      payload
    );

    const signedTxn = await client.signTransaction(this.account, txnRequest);
    return await client.submitTransaction(signedTxn);
  }

  // Place market order
  async placeMarketOrder(
    baseToken: string,
    quoteToken: string,
    marketId: number,
    side: boolean,
    size: number
  ) {
    const payload = {
      type: "entry_function_payload",
      function: `${dexAddress}::DEX::place_market_order`,
      type_arguments: [baseToken, quoteToken],
      arguments: [
        dexAddress,
        marketId.toString(),
        side,
        size.toString()
      ]
    };

    const txnRequest = await client.generateTransaction(
      this.account.address(),
      payload
    );

    const signedTxn = await client.signTransaction(this.account, txnRequest);
    return await client.submitTransaction(signedTxn);
  }

  // Cancel order
  async cancelOrder(
    baseToken: string,
    quoteToken: string,
    orderId: number
  ) {
    const payload = {
      type: "entry_function_payload",
      function: `${dexAddress}::DEX::cancel_order`,
      type_arguments: [baseToken, quoteToken],
      arguments: [dexAddress, orderId.toString()]
    };

    const txnRequest = await client.generateTransaction(
      this.account.address(),
      payload
    );

    const signedTxn = await client.signTransaction(this.account, txnRequest);
    return await client.submitTransaction(signedTxn);
  }

  // Withdraw tokens
  async withdraw(tokenType: string, amount: number, symbol: string) {
    const payload = {
      type: "entry_function_payload",
      function: `${dexAddress}::DEX::withdraw`,
      type_arguments: [tokenType],
      arguments: [dexAddress, amount.toString(), symbol]
    };

    const txnRequest = await client.generateTransaction(
      this.account.address(),
      payload
    );

    const signedTxn = await client.signTransaction(this.account, txnRequest);
    return await client.submitTransaction(signedTxn);
  }

  // Get user balances
  async getUserBalances(tokenType: string, userAddress: string) {
    const resource = await client.getAccountResource(
      dexAddress,
      `${dexAddress}::Vault::UserBalance<${tokenType}>`
    );
    return resource.data;
  }

  // Get DEX statistics
  async getDEXStats() {
    const resource = await client.getAccountResource(
      dexAddress,
      `${dexAddress}::DEX::DEXStats`
    );
    return resource.data;
  }
}
```

### Python SDK

```python
from aptos_sdk.client import RestClient
from aptos_sdk.account import Account
from aptos_sdk.transactions import EntryFunction, TransactionArgument
from aptos_sdk.type_tag import StructTag, TypeTag

class DEXIntegration:
    def __init__(self, client: RestClient, account: Account):
        self.client = client
        self.account = account
        self.dex_address = "0xd4f4a886d54d280f06e3beebde86c7ff27a824dffb1a410dda625635cd16af5e"

    def deposit(self, token_type: str, amount: int, symbol: str):
        payload = EntryFunction.natural(
            f"{self.dex_address}::DEX",
            "deposit",
            [TypeTag(StructTag.from_str(token_type))],
            [
                TransactionArgument(self.dex_address, "address"),
                TransactionArgument(amount, "u64"),
                TransactionArgument(symbol, "string")
            ]
        )

        signed_txn = self.client.create_bcs_signed_transaction(
            self.account, payload
        )
        return self.client.submit_bcs_transaction(signed_txn)

    def place_limit_order(self, base_token: str, quote_token: str,
                         market_id: int, side: bool, price: int, size: int):
        payload = EntryFunction.natural(
            f"{self.dex_address}::DEX",
            "place_limit_order",
            [
                TypeTag(StructTag.from_str(base_token)),
                TypeTag(StructTag.from_str(quote_token))
            ],
            [
                TransactionArgument(self.dex_address, "address"),
                TransactionArgument(market_id, "u64"),
                TransactionArgument(side, "bool"),
                TransactionArgument(price, "u64"),
                TransactionArgument(size, "u64")
            ]
        )

        signed_txn = self.client.create_bcs_signed_transaction(
            self.account, payload
        )
        return self.client.submit_bcs_transaction(signed_txn)
```

### Move Script Examples

```move
script {
    use std::signer;
    use aptos_framework::coin;

    fun place_order<BaseToken, QuoteToken>(
        user: &signer,
        dex_address: address,
        market_id: u64,
        side: bool,
        price: u64,
        size: u64
    ) {
        // Place a limit order
        liquidity_pool::DEX::place_limit_order<BaseToken, QuoteToken>(
            user,
            dex_address,
            market_id,
            side,
            price,
            size
        );
    }
}
```

## API Reference

### Core Functions

#### `initialize(admin, fee_collector, emergency_admin)`
- **Description**: Initialize the DEX (admin only)
- **Parameters**:
  - `admin`: DEX administrator address
  - `fee_collector`: Address to collect trading fees
  - `emergency_admin`: Emergency pause authority

#### `deposit<TokenType>(user, admin_addr, tokens, token_symbol)`
- **Description**: Deposit tokens into DEX vault
- **Parameters**:
  - `user`: User account signer
  - `admin_addr`: DEX admin address
  - `tokens`: Coin<TokenType> to deposit
  - `token_symbol`: Token symbol string

#### `place_limit_order<BaseToken, QuoteToken>(user, admin_addr, market_id, side, price, size)`
- **Description**: Place a limit order
- **Parameters**:
  - `user`: User account signer
  - `admin_addr`: DEX admin address
  - `market_id`: Trading pair market ID
  - `side`: Order side (false=buy, true=sell)
  - `price`: Order price
  - `size`: Order quantity
- **Returns**: Order ID (u64)

#### `place_market_order<BaseToken, QuoteToken>(user, admin_addr, market_id, side, size)`
- **Description**: Place a market order
- **Parameters**: Similar to limit order except no price
- **Returns**: Vector of matched order IDs

#### `cancel_order<BaseToken, QuoteToken>(user, admin_addr, order_id)`
- **Description**: Cancel an existing order
- **Returns**: Remaining order size

#### `withdraw<TokenType>(user, admin_addr, amount, token_symbol)`
- **Description**: Withdraw tokens from DEX vault
- **Returns**: Coin<TokenType>

### View Functions

#### `get_user_balances<TokenType>(admin_addr, user_addr)`
- **Returns**: (available_balance, locked_balance)

#### `get_dex_stats(admin_addr)`
- **Returns**: (total_trades, total_volume, total_fees, active_users)

#### `get_market_price<BaseToken, QuoteToken>(admin_addr)`
- **Returns**: Current market price

#### `get_spread<BaseToken, QuoteToken>(admin_addr)`
- **Returns**: Bid-ask spread

## Events

The DEX emits the following events:

- **TradeEvent**: When orders are matched
- **OrderEvent**: When orders are placed/cancelled
- **DepositEvent**: When tokens are deposited
- **WithdrawalEvent**: When tokens are withdrawn

## Error Codes

- `E_NOT_INITIALIZED (1)`: DEX not initialized
- `E_ALREADY_INITIALIZED (2)`: DEX already initialized
- `E_MARKET_NOT_ACTIVE (3)`: Trading pair not active
- `E_TOKEN_NOT_TRADEABLE (4)`: Token not allowed for trading
- `E_INSUFFICIENT_BALANCE (5)`: Insufficient token balance
- `E_INVALID_ORDER (6)`: Invalid order parameters
- `E_UNAUTHORIZED (7)`: Unauthorized access
- `E_DEX_PAUSED (8)`: DEX operations paused

## Testing

You need to fund your account and complete the deployment to fully test the integration. Visit https://aptos.dev/network/faucet to get testnet APT.

## Security Considerations

1. **Authorization**: Ensure proper signer verification for all operations
2. **Balance Checks**: Verify sufficient balances before placing orders
3. **Slippage**: Consider market impact for large orders
4. **Emergency Controls**: DEX can be paused by admin/emergency admin
5. **Reentrancy**: Smart contracts use Move's native protection

## Support

For technical support or questions about the DEX integration, please refer to the Move documentation or Aptos developer resources.