# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start required infrastructure (PostgreSQL + Minio)
docker-compose up -d

# Build
./gradlew clean build

# Run
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.attestry.dpp.application.service.SomeServiceTest"
```

## Architecture

Clean Architecture with 3 layers:

- **`domain/`** — Entities, repository interfaces, exceptions, utilities. Zero framework dependencies.
- **`application/`** — Service classes (use cases), DTOs. Depends only on domain.
- **`infrastructure/`** — Spring Web controllers, JPA repository implementations, JWT security, config. Depends on both layers above.

Package root: `com.attestry.dpp`

## Key Patterns

### Immutable Ledger (append-only hash chain)
`LedgerEntry` records every business event (MINTED, CLAIMED, TRANSFER_COMPLETED, SERVICE_CONFIRMED). Each entry stores a SHA-256 hash of itself (`hash`) and the previous entry's hash (`prevHash`), forming a tamper-evident chain. The `seq` field is sequential per passport (starts at 1). **Never delete or update ledger entries.**

When appending a new entry:
1. Fetch the latest entry via `findFirstByPassportIdOrderBySeqDesc()`
2. Pass `prevHash` from that entry into `LedgerEntry.create()`
3. `LedgerEntry.create()` computes the new hash via `HashUtil.sha256()`

### Ownership Projection
`Ownership` is a separate read-model table for fast "who owns this passport?" lookups, avoiding full ledger scans. It is updated in sync with ledger writes (not eventually consistent).

### Domain Factory Methods
Entities use static factory methods for creation:
- `Asset.mint(brandId, modelName, serialNumber)`
- `DigitalPassport.issue(assetId)`
- `LedgerEntry.create(passport, seq, action, actorRole, actorId, dataJson, prevHash)`
- `TransferToken.accept(nonce)` — validates expiry, attempt limits, nonce

### Repository Pattern
`domain/repository/` — interfaces only (no Spring annotations).
`infrastructure/persistence/` — JPA implementations using Spring Data.
Services depend on domain interfaces, not JPA directly.

### JWT Principal
Use `@AuthenticationPrincipal JwtUserDetails principal` in controllers to extract `userId` and `role` from the JWT. Never accept userId from the request body for ownership-sensitive operations.

## Domain Entities Quick Reference

| Entity | Key Fields | State Machine |
|--------|-----------|---------------|
| `User` | email, role, status | PENDING → ACTIVE / REJECTED |
| `Asset` | brandId, modelName, serialNumber, status | MINTED → RELEASED |
| `DigitalPassport` | qrPublicCode, assetId, status | ACTIVE / REVOKED |
| `LedgerEntry` | seq, eventAction, hash, prevHash, dataJson, actorRole | append-only |
| `Ownership` | passportId (PK), owner, version, sinceAt | incremented on each transfer |
| `TransferToken` | state, acceptMethod, code, expiresAt, failedAttempts, version | INITIATED → COMPLETED / CANCELLED |
| `ServiceCase` | kind (REPAIR/AUTHENTICATION), state | REQUESTED → COMPLETED → APPROVED / REJECTED |
| `RegistrationRequest` | modelName, serialNumber, evidenceUrls, status | PENDING → APPROVED / REJECTED |

## Dev Seed Accounts

| Role | Email | Password |
|------|-------|----------|
| ADMIN | kimsunwook@naver.com | adminsw00@ |
| BRAND | brand@test.com | brand123! |
| OWNER | owner@test.com | owner123! |
| PROVIDER | provider@test.com | provider123! |

## Infrastructure

| Service | Local URL | Credentials |
|---------|-----------|-------------|
| PostgreSQL 15 | `localhost:5432` db: `attestry_dpp` | `postgres / password` |
| Minio (S3) | `localhost:9000` (API) `localhost:9001` (console) | `minioadmin / minioadmin` |
| Spring Boot API | `localhost:8080` | — |

- JPA `ddl-auto: update` — schema auto-managed via Hibernate.
- Swagger UI: `http://localhost:8080/swagger-ui.html` (no auth required)
- JWT secret: env var `JWT_SECRET` (falls back to dev default in application.yml)
- CORS origin: env var `CORS_ALLOWED_ORIGINS` (falls back to `http://localhost:5174`)
- Minio is used for evidence file storage. Backend returns pre-signed upload URLs (15-min expiry) via `MinioService.getPresignedUploadUrl()`. Files never pass through the backend.
- `DevDataSeeder` seeds ADMIN / BRAND / OWNER / PROVIDER accounts on startup.
- `TransferToken` uses `@Version` for optimistic locking on concurrent transfer attempts.

## Security

- JWT claims: `userId`, `role`, subject = email. Expiry: 1 hour. No refresh token.
- Spring Security CORS configured in `SecurityConfig` — do **not** add `@CrossOrigin` on controllers.
- CORS allowed origin: configured via `cors.allowed-origins` in application.yml (supports comma-separated multiple origins)
- Roles: `BRAND`, `RETAIL`, `OWNER`, `PROVIDER`, `ADMIN`
- `BRAND` and `RETAIL` start with `PENDING` status until admin approval.

## Error Handling

Domain exceptions in `domain/exception/` map to HTTP codes via `GlobalExceptionHandler`:
- `NotFoundException` → 404
- `BadRequestException` → 400
- `UnauthorizedException` → 401
- `ConflictException` → 409

Throw domain exceptions from services, not controllers.

## Reference Documents

- `Attestry_DPP_Spec.md` — Full business and API specification (45 KB). Check here for authoritative business rules before implementing features.
- `REFACTOR_V1.md`, `REFACTOR_V2.md` — Completed refactoring checklists. Useful for understanding past design decisions.
