# picpay-desafio - API Guide

## UserController

Manages users of the platform: registration of new users, lookup of a user by identifier, and deposits into a user's balance. All paths are relative to `/api/users`.

### POST /api/users
Creates a new user from the provided registration data.

**Request body**: `UserCreateRequestDTO`

| Field | Type | Constraints |
|---|---|---|
| fullName | `String` | required, 3-100 chars |
| taxId | `String` | required, regex `\d{11}\|\d{14}` (11 digits CPF or 14 digits CNPJ) |
| email | `String` | required, valid email format |
| password | `String` | required, 8-64 chars |

**Response**: `200` — `UserResponseDTO`

| Field | Type |
|---|---|
| id | `UUID` |
| fullName | `String` |
| taxId | `String` |
| email | `String` |
| balance | `BigDecimal` |

### GET /api/users/{id}
Returns the user identified by the given id.

**Parameters**

| Name | In | Type | Required |
|---|---|---|---|
| id | path | `UUID` | yes |

**Response**: `200` — `UserResponseDTO` (see above)

### POST /api/users/deposit
Deposits the given amount into the balance of the specified user.

**Request body**: `UserDepositRequestDTO`

| Field | Type | Constraints |
|---|---|---|
| userId | `UUID` | required |
| amount | `BigDecimal` | required, min `0.01`, at most 10 integer digits and 2 decimal places |

**Response**: `200` — `UserResponseDTO` (see above)

## TransferController

Handles money transfers between two users. The controller exposes a single endpoint under `/api/transfers` that accepts a transfer request identifying the payer, the payee and the amount.

### POST /api/transfers

Creates a transfer from the payer to the payee with the given amount. Returns a summary of the transfer together with a list of side effect results.

**Request body**: `TransferRequestDTO`

| Field | Type | Constraints |
|---|---|---|
| value | `BigDecimal` | required, min 0.01, at most 10 integer digits and 2 decimal places |
| payer | `UUID` | required |
| payee | `UUID` | required |

**Response**: `200` — `TransferResponseDTO`

| Field | Type |
|---|---|
| status | `String` |
| transferId | `UUID` |
| payerName | `String` |
| payeeName | `String` |
| amount | `BigDecimal` |
| timestamp | `LocalDateTime` |
| sideEffects | `List<SideEffectResult>` |

Each element of `sideEffects` is a `SideEffectResult`:

| Field | Type |
|---|---|
| operation | `String` |
| status | `String` |
| message | `String` |

