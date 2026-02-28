# Attestry DPP — 백엔드 완전 개발 사양서 v3.0

> **이 문서의 목적**  
> IntelliJ + Java 백엔드 개발 시 AI CLI(Copilot, Claude Code 등)가 읽고 즉시 코드를 생성할 수 있도록 작성된 **기계 친화적(machine-readable) + 사람 친화적(human-readable)** 사양서입니다.  
> 설계 의도, DB 구조, API 엔드포인트, 비즈니스 규칙, 상태 전이, 보안 정책을 모두 포함합니다.

---

## 목차

1. [서비스 개요](#1-서비스-개요)
2. [핵심 개념 (Domain Glossary)](#2-핵심-개념-domain-glossary)
3. [Actor & 권한 매트릭스](#3-actor--권한-매트릭스)
4. [버전 구분 (v1 vs v2)](#4-버전-구분-v1-vs-v2)
5. [전체 흐름 요약](#5-전체-흐름-요약)
6. [도메인 모델 & DB 스키마](#6-도메인-모델--db-스키마)
7. [상태 전이 다이어그램](#7-상태-전이-다이어그램)
8. [API 엔드포인트 전체 목록](#8-api-엔드포인트-전체-목록)
9. [화면별 상세 명세](#9-화면별-상세-명세)
10. [양도(Transfer) 상세 설계](#10-양도transfer-상세-설계)
11. [원장(Ledger) 해시체인 구조](#11-원장ledger-해시체인-구조)
12. [증빙(Evidence) 시스템](#12-증빙evidence-시스템)
13. [Outbox 패턴 & 트랜잭션 설계](#13-outbox-패턴--트랜잭션-설계)
14. [보안 & 권한 정책](#14-보안--권한-정책)
15. [에러 처리 & 응답 코드](#15-에러-처리--응답-코드)
16. [Java 구현 가이드라인](#16-java-구현-가이드라인)

---

## 1. 서비스 개요

### 1.1 무엇을 만드는가

**Attestry DPP (Digital Product Passport)** — 명품/패션 제품의 디지털 이력서 시스템.

- 제품 하나마다 **불변 원장(Immutable Ledger)** 이 존재한다.
- QR 코드 하나로 제품 전체 생애 이력을 확인한다.
- 소유권 이전, 수선 이력, 브랜드 인증을 투명하게 기록한다.

### 1.2 팀 컬러 & 브랜딩

- 메인 컬러: **밝은 초록(연초록, `#4ADE80`)**
- 버전: `1.0` (기획 기준) / 개발 사양서 `v3.0`
- 날짜: `2026-02-25`

### 1.3 설계 3대 원칙

| 원칙 | 내용 |
|------|------|
| **보안은 서버가** | UI 버튼 숨기기는 UX, 보안은 서버 권한 검증이 필수 |
| **원장은 추가만** | Ledger는 append-only. 수정/삭제 API를 절대 만들지 않는다 |
| **확정은 명시적** | QR 스캔 = 조회 시작. 소유권/서비스 확정은 반드시 로그인 + 확정 버튼 클릭 |

---

## 2. 핵심 개념 (Domain Glossary)

### 2.1 핵심 엔티티

| 용어 | 설명 | DB 테이블 |
|------|------|-----------|
| **Asset** | 실물 제품 1개 = DB 1행. 고유 ID 보유 | `assets` |
| **Passport** | QR로 열리는 '디지털 여권'. `qrPublicCode`로 식별 | `passports` |
| **Ledger** | 확정된 사실만 시간순 append-only 기록 | `ledger_entries` |
| **Transfer** | 소유권 이전 요청/완료 기록 | `transfer_tokens` |
| **ServiceCase** | 수선/세탁 등 서비스 케이스 | `service_cases` |
| **Evidence** | 증빙 파일(사진/영수증) 관리 | `evidence_groups`, `evidences` |
| **Ownership Projection** | 현재 소유자 빠른 조회용 Read Model | `ownership_projections` |

### 2.2 상태(Enum) 전체 목록

#### AssetState
```java
public enum AssetState {
    ACTIVE,   // 정상 — 소유권 이전/서비스 가능
    RETIRED,  // 은퇴 — 조회만, 신규 이벤트 불가
    VOIDED    // 무효 — 사유 필수, 신규 이벤트 불가
}
```

#### TransferState
```java
public enum TransferState {
    INITIATED,   // 양도/클레임 수단(QR or 코드)이 생성됨
    PENDING,     // 수락 대기 (정책에 따라 사용)
    COMPLETED,   // 확정 완료 — 소유권 변경 확정됨
    CANCELLED,   // 취소됨
    EXPIRED      // 만료됨
}
```

#### ServiceCaseState
```java
public enum ServiceCaseState {
    SUBMITTED,         // 업체 제출 (초안 — 원장 기록 없음)
    CHECKED_IN,        // 입고 확인됨
    COMPLETED,         // 작업 완료됨
    APPROVED,          // 소유자 승인 — 원장 SERVICE_CONFIRMED 기록
    REJECTED,          // 소유자 거절
    SERVICE_CONFIRMED  // 최종 확정
}
```

#### EventAction (원장 이벤트 타입)
```java
public enum EventAction {
    MINTED,               // 브랜드가 제품을 시스템에 등록
    RELEASED,             // 브랜드가 판매처로 출고
    CLAIMED,              // 구매자가 최초 소유권 등록
    TRANSFER_INITIATED,   // 양도 시작됨
    TRANSFER_COMPLETED,   // 양도 완료 — 소유권 이전 확정
    SERVICE_CONFIRMED,    // 서비스(수선 등) 최종 확정
    VOIDED                // 무효 처리
}
```

---

## 3. Actor & 권한 매트릭스

### 3.1 Actor 정의

| Actor | 설명 | 로그인 필요 |
|-------|------|------------|
| **Public** | 비로그인 사용자 — QR 스캔, 공개 여권 조회만 가능 | ✗ |
| **Owner** | 제품 현재 소유자 — 클레임/양도/수선 승인 | ✓ |
| **Retail** | 판매처 — 클레임 QR 발급 | ✓ |
| **Provider** | 수선/세탁 업체 — 서비스 요청 제출 | ✓ |
| **Brand** | 브랜드사 — 제품 민팅, 판매처 출고 | ✓ |

### 3.2 권한 매트릭스

| 기능 | Public | Owner | Retail | Provider | Brand |
|------|--------|-------|--------|----------|-------|
| 공개 여권 조회 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 클레임 (최초 소유권) | ✗ | ✅ | ✗ | ✗ | ✗ |
| 양도 시작 | ✗ | ✅(본인 소유만) | ✗ | ✗ | ✗ |
| 양도 수락 | ✗ | ✅ | ✗ | ✗ | ✗ |
| 수선 승인/거절 | ✗ | ✅(본인 소유만) | ✗ | ✗ | ✗ |
| 서비스 제출 | ✗ | ✗ | ✗ | ✅ | ✗ |
| 클레임 QR 발급 | ✗ | ✗ | ✅(RELEASED 제품만) | ✗ | ✗ |
| 제품 민팅 | ✗ | ✗ | ✗ | ✗ | ✅ |
| 판매처 출고 | ✗ | ✗ | ✗ | ✗ | ✅ |

> ⚠️ **서버 구현 필수**: 위 권한은 UI로만 제한하지 않고, 모든 API에서 서버 사이드 권한 검증을 수행해야 합니다.

---

## 4. 버전 구분 (v1 vs v2)

### 4.1 v1 — 브랜드 민팅 → 클레임 흐름 (현재 개발 버전)

**가정**: 실물 제품에 QR 스티커가 이미 부착되어 있다.  
**목적**: 브랜드 → 판매처 → 소유자 데이터 흐름 검증.

```
Brand: 화면A(/brand/mint) → MINTED
     → 화면B(/brand/release) → RELEASED
Retail: 화면3(/retail/issue-claim) → 클레임 QR 발급
Owner: 클레임 QR 스캔 → [로그인 필요시 화면2] → 화면4(/claim/{token}) → CLAIMED
     → 화면5(/me/passports) → 이후 동일
```

**v1 클레임 경로 핵심**: 클레임 QR URL은 `/claim/{token}` 이므로 **화면1을 거치지 않고 화면4로 직접** 진입한다.

### 4.2 v2 — 구매자 직접 소유권 등록 (다음 버전)

**가정**: 실물 QR이 없다.  
**목적**: QR 없이도 DPP 소유권 등록 가능한지 UX 검증.

```
Owner: 서비스 접속 → 로그인 → /register
     → 제품 정보 직접 입력 (시리얼/모델/구매일/구매증빙)
     → 소유권 등록 확정 → CLAIMED
     → 화면5(/me/passports) → 이후 v1과 완전 동일
```

### 4.3 v1 vs v2 비교

| 구분 | v1 | v2 |
|------|----|----|
| 실물 QR 필요? | ✅ 필요 | ✗ 불필요 |
| 브랜드 민팅(화면A) | ✅ 필요 | ✗ 없음 |
| 판매처 출고(화면B) | ✅ 필요 | ✗ 없음 |
| 클레임 QR 발급(화면3) | ✅ Retail이 발급 | ✗ 없음 |
| 소유권 등록 방식 | 클레임 QR 스캔 → 화면4 | `/register` 직접 입력 |
| 원장 첫 이벤트 | `MINTED → RELEASED → CLAIMED` | `CLAIMED` |
| CLAIMED 이후 흐름 | **동일** | **동일** |

---

## 5. 전체 흐름 요약

### 5.1 v1 전체 흐름

```
[Brand]
  화면A: /brand/mint
    └─(민팅 실행)→ assets + passports INSERT + ledger MINTED + outbox INSERT [단일 TX]
  화면B: /brand/release
    └─(출고 확정)→ ledger RELEASED + retail ISSUE_CLAIM 권한 부여 [단일 TX]

[Retail]
  화면3: /retail/issue-claim
    └─(클레임 QR 발급)→ transfer_tokens INSERT (type=CLAIM, state=INITIATED)
    └─ QR/코드를 구매자에게 전달

[Owner - 클레임]
  클레임 QR 스캔 → /claim/{token} (화면4로 직접, 화면1 건너뜀)
    ├─ 비로그인이면 → /login?return=/claim/{token} (화면2)
    └─ 로그인 상태 → 화면4 바로 표시
  화면4: /claim/{token}
    └─(클레임 확정)→ ledger CLAIMED + ownership_projections INSERT + transfer_tokens COMPLETED [단일 TX]
  → 화면5: /me/passports

[Owner - 양도]
  화면5 → 양도 시작 버튼 → 화면6: /transfer/start/{passportId}
    └─(IN_PERSON_QR 선택)→ 1회용 QR 생성 (transfer_tokens INSERT, type=TRANSFER, method=IN_PERSON_QR)
    └─(ONE_TIME_CODE 선택)→ 6자리 코드 생성 + SHA-256 해시 저장 (transfer_tokens INSERT, method=ONE_TIME_CODE)
  → 구매자에게 QR/코드 전달
  화면7: /transfer/{token} 또는 /transfer/code
    └─(양도 확정)→ ledger TRANSFER_COMPLETED + ownership_projections UPDATE [단일 TX]

[Provider - 서비스]
  화면8: /provider/service/submit
    └─(체크인 제출)→ service_cases INSERT (state=CHECKED_IN) + evidence_groups/evidences INSERT
    └─(작업완료 제출)→ service_cases UPDATE (state=COMPLETED) + completion evidence INSERT
  → 소유자 알림
  화면9: /service/{caseId}
    └─(소유자 승인)→ service_cases UPDATE (state=APPROVED) + ledger SERVICE_CONFIRMED [단일 TX]
    └─(소유자 거절)→ service_cases UPDATE (state=REJECTED)
```

### 5.2 화면 → 원장 이벤트 매핑

| 화면 | 확정 버튼 | 원장 이벤트 |
|------|-----------|------------|
| 화면4: 클레임 확정 | 클레임 확정 | `CLAIMED` |
| 화면7: 양도 수락 | 양도 확정 | `TRANSFER_COMPLETED` |
| 화면9: 수선 승인 | 승인 | `SERVICE_CONFIRMED` |
| 부록A: 브랜드 민팅 | 민팅 실행 | `MINTED` |
| 부록B: 브랜드 출고 | 출고 확정 | `RELEASED` |

---

## 6. 도메인 모델 & DB 스키마

### 6.1 assets

```sql
CREATE TABLE assets (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    brand_id    UUID NOT NULL,             -- 브랜드 참조
    model_name  VARCHAR(200) NOT NULL,
    serial_no   VARCHAR(100) UNIQUE,       -- 제품 시리얼번호
    manufacture_date DATE,
    state       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- AssetState enum
    voided_reason TEXT,                    -- state=VOIDED일 때 필수
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 6.2 passports

```sql
CREATE TABLE passports (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id        UUID NOT NULL REFERENCES assets(id),
    qr_public_code  VARCHAR(50) UNIQUE NOT NULL, -- QR에 포함되는 공개 코드
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- QR 스캔 조회 최적화
CREATE INDEX idx_passports_qr_public_code ON passports(qr_public_code);
```

### 6.3 ledger_entries (핵심 — append-only)

```sql
CREATE TABLE ledger_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id        UUID NOT NULL REFERENCES assets(id),
    passport_id     UUID NOT NULL REFERENCES passports(id),
    sequence_no     BIGINT NOT NULL,           -- 제품별 순번 (1부터 시작)
    event_action    VARCHAR(50) NOT NULL,      -- EventAction enum
    actor_role      VARCHAR(30) NOT NULL,      -- BRAND / RETAIL / OWNER / PROVIDER / SYSTEM
    actor_id        UUID,                      -- 행위자 user/brand/retail ID
    payload         JSONB NOT NULL DEFAULT '{}', -- 이벤트 상세 데이터
    prev_hash       VARCHAR(64),               -- 이전 entry의 hash (최초는 NULL)
    hash            VARCHAR(64) NOT NULL,      -- SHA-256(prev_hash + event_action + payload + sequence_no)
    recorded_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    
    UNIQUE(asset_id, sequence_no)
);

-- UPDATE/DELETE 방지는 애플리케이션 레벨 + DB 권한으로 이중 보호
-- DB 사용자에게 UPDATE, DELETE 권한 부여 금지
CREATE INDEX idx_ledger_asset_id ON ledger_entries(asset_id, sequence_no);
```

> ⚠️ **절대 규칙**: `ledger_entries`에는 `UPDATE`, `DELETE` SQL을 실행하지 않는다. DB 사용자 권한에서도 해당 권한을 제거한다.

### 6.4 ownership_projections (Read Model)

```sql
CREATE TABLE ownership_projections (
    passport_id         UUID PRIMARY KEY REFERENCES passports(id),
    asset_id            UUID NOT NULL,
    current_owner_id    UUID NOT NULL,         -- 현재 소유자 user ID
    owner_since         TIMESTAMP NOT NULL,    -- 소유 시작 시각
    last_ledger_entry_id UUID NOT NULL,        -- 마지막 원장 entry ID
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 사용자의 소유 제품 목록 조회 최적화
CREATE INDEX idx_ownership_owner_id ON ownership_projections(current_owner_id);
```

### 6.5 transfer_tokens

```sql
CREATE TABLE transfer_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    passport_id     UUID NOT NULL REFERENCES passports(id),
    initiator_id    UUID NOT NULL,             -- 양도 시작자 (판매자 or Retail)
    transfer_type   VARCHAR(20) NOT NULL,      -- CLAIM | TRANSFER
    accept_method   VARCHAR(30) NOT NULL,      -- IN_PERSON_QR | ONE_TIME_CODE
    token_hash      VARCHAR(64) NOT NULL,      -- SHA-256(원본 토큰/코드) — 평문 저장 금지
    state           VARCHAR(20) NOT NULL DEFAULT 'INITIATED', -- TransferState enum
    expires_at      TIMESTAMP NOT NULL,        -- 만료 시각
    accepted_by_id  UUID,                      -- 수락자 (구매자)
    completed_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transfer_tokens_passport ON transfer_tokens(passport_id, state);
```

> 🔒 **보안**: `token_hash`에는 SHA-256 해시만 저장. 원본 코드는 DB에 절대 저장하지 않는다.

### 6.6 service_cases

```sql
CREATE TABLE service_cases (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    passport_id                 UUID NOT NULL REFERENCES passports(id),
    provider_id                 UUID NOT NULL,
    owner_id                    UUID NOT NULL,
    service_type                VARCHAR(50),   -- REPAIR | CLEANING | MAINTENANCE
    description                 TEXT,
    state                       VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    checkin_evidence_group_id   UUID REFERENCES evidence_groups(id),
    completion_evidence_group_id UUID REFERENCES evidence_groups(id),
    submitted_at                TIMESTAMP NOT NULL DEFAULT NOW(),
    checked_in_at               TIMESTAMP,
    completed_at                TIMESTAMP,
    approved_at                 TIMESTAMP,
    rejected_at                 TIMESTAMP,
    reject_reason               TEXT
);
```

### 6.7 evidence_groups & evidences

```sql
CREATE TABLE evidence_groups (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_type  VARCHAR(30) NOT NULL,  -- CHECKIN | COMPLETION
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE evidences (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id        UUID NOT NULL REFERENCES evidence_groups(id),
    file_url        VARCHAR(500) NOT NULL,
    file_type       VARCHAR(50),       -- IMAGE | PDF | RECEIPT
    description     TEXT,
    uploaded_by_id  UUID NOT NULL,
    uploaded_at     TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 6.8 outbox (트랜잭션 안전장치)

```sql
CREATE TABLE outbox (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(50) NOT NULL,   -- ASSET | PASSPORT | TRANSFER | SERVICE_CASE
    aggregate_id    UUID NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         JSONB NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING | PROCESSED | FAILED
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMP
);

CREATE INDEX idx_outbox_pending ON outbox(status, created_at) WHERE status = 'PENDING';
```

---

## 7. 상태 전이 다이어그램

### 7.1 AssetState

```
초기 생성
    │
    ▼
 [ACTIVE] ──────────────→ [RETIRED]
    │                         │
    └──────────────────────→ [VOIDED]
    
ACTIVE: 모든 이벤트 가능
RETIRED: 조회만 가능, 신규 이벤트 불가
VOIDED: 조회만 가능, voided_reason 필수
```

### 7.2 TransferState

```
Retail/Owner 가 transfer_tokens 생성
    │
    ▼
[INITIATED]
    │
    ├─ (만료 시간 초과) → [EXPIRED]
    ├─ (취소 버튼)      → [CANCELLED]
    └─ (구매자 확정)    → [COMPLETED]
```

### 7.3 ServiceCaseState

```
Provider 제출
    │
    ▼
[SUBMITTED] ──── (체크인 완료) ──→ [CHECKED_IN]
                                        │
                                   (작업 완료)
                                        │
                                        ▼
                                  [COMPLETED]
                                        │
                          ┌─────────────┴─────────────┐
                     (승인)                        (거절)
                          │                            │
                          ▼                            ▼
                    [APPROVED]                    [REJECTED]
                          │
                   (원장 기록)
                          │
                          ▼
               [SERVICE_CONFIRMED]
```

---

## 8. API 엔드포인트 전체 목록

### 8.1 공개 API (인증 불필요)

| Method | Path | 설명 | 응답 |
|--------|------|------|------|
| `GET` | `/p/{qrPublicCode}` | 공개 디지털 여권 조회 | `PassportPublicResponse` |
| `GET` | `/p/{qrPublicCode}/ledger` | 원장 타임라인 조회 | `LedgerTimelineResponse` |

### 8.2 인증 API — Owner

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| `GET` | `/claim/{claimToken}` | 클레임 정보 확인 | Owner (비로그인 시 /login redirect) |
| `POST` | `/claim/{claimToken}` | 클레임 확정 | Owner |
| `GET` | `/me/passports` | 내 여권 목록 | Owner |
| `GET` | `/me/passports/{passportId}` | 내 여권 상세 | Owner |
| `POST` | `/transfer/start/{passportId}` | 양도 시작 | Owner (본인 소유) |
| `DELETE` | `/transfer/{transferId}` | 양도 취소 | Owner (initiator만) |
| `GET` | `/transfer/{transferToken}` | 양도 정보 확인 | 비로그인 가능 |
| `POST` | `/transfer/{transferToken}/accept` | 양도 확정 (QR 방식) | Owner |
| `POST` | `/transfer/code` | 양도 확정 (코드 방식) | Owner |
| `GET` | `/service/{caseId}` | 서비스 케이스 상세 | Owner (본인 소유) |
| `POST` | `/service/{caseId}/approve` | 수선 승인 | Owner (본인 소유) |
| `POST` | `/service/{caseId}/reject` | 수선 거절 | Owner (본인 소유) |

### 8.3 인증 API — Retail

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| `POST` | `/retail/issue-claim` | 클레임 QR/코드 발급 | Retail (RELEASED 제품만) |
| `GET` | `/retail/products` | 출고 받은 제품 목록 | Retail |

### 8.4 인증 API — Provider

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| `POST` | `/provider/service/checkin` | 서비스 체크인 제출 | Provider |
| `POST` | `/provider/service/{caseId}/complete` | 서비스 완료 제출 | Provider |
| `GET` | `/provider/service/cases` | 내 케이스 목록 | Provider |

### 8.5 인증 API — Brand

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| `POST` | `/brand/mint` | 제품 민팅 | Brand |
| `POST` | `/brand/release` | 판매처 출고 | Brand |
| `GET` | `/brand/assets` | 브랜드 제품 목록 | Brand |

### 8.6 v2 전용 API

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| `GET` | `/register` | 직접 소유권 등록 화면 | Owner |
| `POST` | `/register` | 소유권 등록 확정 | Owner |

---

## 9. 화면별 상세 명세

### 화면 1: 공개 디지털 여권

| 항목 | 내용 |
|------|------|
| URL | `/p/{qrPublicCode}` |
| Actor | 누구나 (비로그인 가능) |
| 목적 | 제품 공개 정보 및 원장 타임라인 조회 |

**표시 정보**:
- 제품 브랜드 / 모델명 / 제조년월
- 상태 뱃지 (`ACTIVE` / `RETIRED` / `VOIDED`)
- 원장 타임라인 요약 + 전체 보기
- 로그인 유도 버튼 (클레임/양도는 로그인 후 가능)

**버튼 → 이동 경로**:
- `로그인하고 더 보기` → `/login?return=/p/{qrPublicCode}` (화면2)
- `양도 시작` → `/transfer/start/{passportId}` (화면6) — 본인 소유시만 표시
- `이력 전체 보기` → 모달 (페이지 이동 없음)

**비즈니스 규칙**:
- ❌ 클레임 입구가 아님. 클레임 QR은 `/claim/{token}`으로 화면4 직접 진입
- 소유자 PII(개인정보) 절대 미노출
- QR에는 `qrPublicCode`만 포함 (서버가 passport 조회)
- `RETIRED` / `VOIDED` 상태면 양도 버튼 비활성화

---

### 화면 2: 로그인 / 최소 인증

| 항목 | 내용 |
|------|------|
| URL | `/login` |
| Actor | 전체 |
| 목적 | 인증. 확정 행동 전 반드시 통과 |

**쿼리 파라미터**: `?return={returnUrl}` — 로그인 성공 시 원래 페이지로 redirect

**비즈니스 규칙**:
- 클레임 토큰이 있는 경우: 로그인 후 `/claim/{token}` (화면4) redirect
- 약관/동의 최소화

---

### 화면 3: 판매처 클레임 증표 발급

| 항목 | 내용 |
|------|------|
| URL | `/retail/issue-claim` |
| Actor | Retail |
| 목적 | 구매자에게 클레임 QR 또는 코드 발급 |

**표시 정보**:
- 제품 선택 (`qrPublicCode` 입력 또는 스캔)
- 클레임 QR / 코드 발급
- 만료 시간 표시

**비즈니스 규칙**:
- 이미 `CLAIMED`된 제품은 발급 불가
- 해당 판매처가 `RELEASED` 권한을 받은 제품만 발급 가능 (제품 단위 권한 체크)
- `transfer_tokens` 테이블에 `type=CLAIM, state=INITIATED` 로 INSERT

---

### 화면 4: 클레임 진행 (최초 소유권 등록)

| 항목 | 내용 |
|------|------|
| URL | `/claim/{claimToken}` |
| Actor | Owner (구매자) |
| 목적 | 최초 소유권 확정 |

**표시 정보**:
- 제품 확인 정보
- 구매 증빙 업로드 (정책에 따라 선택/필수)
- 클레임 확정 버튼

**확정 버튼 클릭 시 서버 처리 (단일 트랜잭션)**:
```
1. transfer_tokens WHERE token_hash = SHA256(claimToken) 조회
2. state == INITIATED && expires_at > NOW() 검증
3. asset의 state == ACTIVE 검증
4. 기존 CLAIMED 여부 확인
5. ledger_entries INSERT (EventAction=CLAIMED)
6. ownership_projections INSERT
7. transfer_tokens UPDATE state=COMPLETED
8. outbox INSERT
```

**비즈니스 규칙**:
- 토큰 만료/이미 사용된 토큰 → 명확한 에러 안내
- `CLAIMED` 성공 → 화면5로 redirect

---

### 화면 5: 내 여권 목록

| 항목 | 내용 |
|------|------|
| URL | `/me/passports` |
| Actor | Owner |
| 목적 | 내 소유 제품 전체 확인 및 관리 |

**표시 정보**:
- 내 제품 카드 리스트 (모델명, 상태, CLAIMED 날짜)
- 대기 알림 (수선 승인 대기 등)

**버튼 → 이동**:
- `여권 보기` → `/p/{qrPublicCode}` (화면1)
- `양도 시작` → `/transfer/start/{passportId}` (화면6)
- `수선 승인` → `/service/{caseId}` (화면9)

**비즈니스 규칙**:
- `ownership_projections` 테이블에서 `current_owner_id = 로그인 유저` 조회
- 대기 알림은 `service_cases WHERE state IN (COMPLETED) AND owner_id = 로그인 유저` 조회

---

### 화면 6: 양도 시작 (판매자)

| 항목 | 내용 |
|------|------|
| URL | `/transfer/start/{passportId}` |
| Actor | Owner (현재 소유자) |
| 목적 | 양도 수단 생성 (QR 또는 코드) |

**표시 정보**:
- 양도 방식 선택: `IN_PERSON_QR` (대면) / `ONE_TIME_CODE` (비대면)
- 1회용 QR 또는 6자리 코드 출력
- 만료 시간 표시
- 취소 버튼

**방식 선택 시 서버 처리**:

**IN_PERSON_QR**:
```
1. UUID token 생성
2. token_hash = SHA-256(token) 저장
3. transfer_tokens INSERT (method=IN_PERSON_QR, state=INITIATED)
4. 서버가 QR 이미지 URL 반환
5. 화면에 QR 코드 표시
```

**ONE_TIME_CODE**:
```
1. 6자리 랜덤 코드 생성 (A-Z, 0-9)
2. token_hash = SHA-256(code) 저장 (원문 절대 저장 금지)
3. transfer_tokens INSERT (method=ONE_TIME_CODE, state=INITIATED)
4. 서버가 원본 코드를 응답에 포함 (1회만, 이후 조회 불가)
5. 화면에 코드 표시: "A3K9P2"
6. 판매자가 KakaoTalk/SMS 또는 택배 박스 동봉으로 전달
```

**비즈니스 규칙**:
- 동시 진행 양도 1건만 허용 (중복 INITIATED 상태 방지)
- 현재 소유자만 시작 가능 (서버에서 `ownership_projections` 확인)
- 만료 시간: IN_PERSON_QR = 30분, ONE_TIME_CODE = 7일 (정책 설정)

---

### 화면 7: 양도 수락 (구매자)

| 항목 | 내용 |
|------|------|
| URL (QR 방식) | `/transfer/{transferToken}` |
| URL (코드 방식) | `/transfer/code` |
| Actor | Owner (새 구매자) |
| 목적 | 양도 확정 — 소유권 이전 |

**표시 정보**:
- 제품 확인 정보
- 수령 증빙 업로드 (추천/정책)
- 양도 확정 버튼

**양도 확정 시 서버 처리 (단일 트랜잭션)**:

```
[IN_PERSON_QR]
1. transferToken에서 token_hash 계산 → transfer_tokens 조회
2. state=INITIATED, expires_at > NOW() 검증
3. ledger_entries INSERT (EventAction=TRANSFER_COMPLETED, payload={fromUserId, toUserId, method})
4. ownership_projections UPDATE (current_owner_id = 새 구매자)
5. transfer_tokens UPDATE (state=COMPLETED, accepted_by_id = 구매자)
6. outbox INSERT

[ONE_TIME_CODE]
1. 구매자가 입력한 코드를 SHA-256 해싱
2. transfer_tokens WHERE token_hash = 해시값 조회
3. 이후 IN_PERSON_QR과 동일
```

**비즈니스 규칙**:
- 성공 → 원장 `TRANSFER_COMPLETED` 기록 + `ownership_projections` 업데이트
- 이전 소유자는 즉시 소유 권한 박탈

---

### 화면 8: 서비스 요청 제출 (업체)

| 항목 | 내용 |
|------|------|
| URL | `/provider/service/submit` |
| Actor | Provider |
| 목적 | 수선/세탁 등 서비스 케이스 등록 |

**표시 정보**:
- 제품 QR 스캔 또는 코드 입력
- 서비스 종류 (수선/세탁/수리)
- 작업 내용 입력
- Before 사진 업로드 (checkin_evidence)
- After 사진 + 영수증 업로드 (completion_evidence)

**체크인 제출 시 서버 처리**:
```
1. evidence_groups INSERT (type=CHECKIN)
2. evidences INSERT (Before 사진들)
3. service_cases INSERT (state=CHECKED_IN, checkin_evidence_group_id = 새 그룹 ID)
4. outbox INSERT (소유자 알림 이벤트)
```

**작업 완료 제출 시 서버 처리**:
```
1. evidence_groups INSERT (type=COMPLETION)
2. evidences INSERT (After 사진, 영수증)
3. service_cases UPDATE (state=COMPLETED, completion_evidence_group_id = 새 그룹 ID)
4. outbox INSERT (소유자 알림 이벤트)
```

**비즈니스 규칙**:
- `SUBMITTED`는 초안 상태. **원장 기록 없음**
- 소유자 승인(`APPROVED`) 후에만 `SERVICE_CONFIRMED` 원장 기록

---

### 화면 9: 서비스 요청 승인/거절 (소유자)

| 항목 | 내용 |
|------|------|
| URL | `/service/{caseId}` |
| Actor | Owner (현재 소유자) |
| 목적 | 서비스 케이스 승인 또는 거절 |

**표시 정보**:
- 업체 정보 / 작업 내용
- Before / After 증빙 사진
- 승인 / 거절 버튼

**승인 시 서버 처리 (단일 트랜잭션)**:
```
1. service_cases WHERE id = caseId AND owner_id = 로그인유저 AND state=COMPLETED 검증
2. ledger_entries INSERT (EventAction=SERVICE_CONFIRMED)
3. service_cases UPDATE (state=APPROVED, approved_at=NOW())
4. outbox INSERT
```

**비즈니스 규칙**:
- 본인 소유 제품만 승인/거절 가능 (서버에서 `ownership_projections` 확인)
- 거절 시 `reject_reason` 필수 (정책)
- 거절을 원장에 남길지는 정책 선택 (기본: 원장 미기록)

---

### 부록 A: 브랜드 민팅

| 항목 | 내용 |
|------|------|
| URL | `/brand/mint` |
| Actor | Brand |
| 목적 | 제품을 시스템에 최초 등록 |

**입력 정보**:
- 시리얼 번호, 모델명, 제조년월
- 정품 선언 데이터

**민팅 시 서버 처리 (단일 트랜잭션)**:
```
1. assets INSERT (state=ACTIVE)
2. passports INSERT (qrPublicCode 생성)
3. ledger_entries INSERT (EventAction=MINTED)
4. outbox INSERT
```

---

### 부록 B: 브랜드 출고 (RELEASED)

| 항목 | 내용 |
|------|------|
| URL | `/brand/release` |
| Actor | Brand |
| 목적 | 판매처로 제품 출고 + 클레임 QR 발급 권한 부여 |

**입력 정보**:
- 출고 대상 제품 선택
- 판매처 선택
- 출고 증빙 업로드

**출고 확정 시 서버 처리 (단일 트랜잭션)**:
```
1. ledger_entries INSERT (EventAction=RELEASED, payload={retailId})
2. retail_product_permissions INSERT (retail_id, asset_id, permission=ISSUE_CLAIM)
3. outbox INSERT
```

---

## 10. 양도(Transfer) 상세 설계

### 10.1 지원 방식 (2가지만)

| 방식 | 설명 | 적합 상황 |
|------|------|-----------|
| `IN_PERSON_QR` | 판매자 폰 화면의 QR을 구매자가 스캔 | 대면 거래 |
| `ONE_TIME_CODE` | 6자리 코드 전달 (KakaoTalk/SMS/택배동봉) | 비대면/온라인 거래 |

> ❌ **제거된 방식**: `LINK_ACCEPT`, `RECEIVE_AND_SCAN`, `TRUSTED_HANDOFF`, `ESCROW_PROTECTED` — 복잡도 대비 효용이 낮아 제거.

### 10.2 OneTime Code 완전 상세 흐름

```
[Step 1] 판매자가 화면6에서 "🔢 코드 생성" 버튼 클릭

[Step 2] 서버에서 실행:
    code = randomAlphanumeric(6)  // 예: "A3K9P2"
    code_hash = SHA-256(code)     // 예: "e3b0c44298fc..."
    
    INSERT transfer_tokens (
        token_hash = code_hash,   // SHA-256 해시만 저장
        method = ONE_TIME_CODE,
        state = INITIATED,
        expires_at = NOW() + 7 days
    )

[Step 3] 서버가 응답에 원본 코드 포함 (단 1회)
    응답: { "code": "A3K9P2", "expiresAt": "2026-03-04T..." }
    ⚠️ 이후 이 코드는 DB에서 조회 불가 (해시만 있으므로)

[Step 4] 판매자가 구매자에게 코드 전달:
    - KakaoTalk/SMS로 전송
    - 또는 택배 박스 안에 종이로 동봉

[Step 5] 구매자가 화면7(/transfer/code)에서 코드 입력:
    입력: "A3K9P2"

[Step 6] 서버 검증:
    input_hash = SHA-256("A3K9P2")
    SELECT * FROM transfer_tokens WHERE token_hash = input_hash
                                   AND state = 'INITIATED'
                                   AND expires_at > NOW()
    → 일치하면 양도 진행

[Step 7] 일치 확인 후 (단일 트랜잭션):
    INSERT ledger_entries (EventAction=TRANSFER_COMPLETED)
    UPDATE ownership_projections (current_owner_id = 구매자)
    UPDATE transfer_tokens (state=COMPLETED)
    INSERT outbox
```

---

## 11. 원장(Ledger) 해시체인 구조

### 11.1 해시 계산 방식

```java
// 해시 계산 입력값 구성
String hashInput = String.format("%s|%s|%s|%d",
    prevHash == null ? "GENESIS" : prevHash,
    eventAction.name(),
    objectMapper.writeValueAsString(payload),
    sequenceNo
);

// SHA-256 해시 계산
MessageDigest digest = MessageDigest.getInstance("SHA-256");
byte[] hashBytes = digest.digest(hashInput.getBytes(StandardCharsets.UTF_8));
String hash = HexFormat.of().formatHex(hashBytes);
```

### 11.2 원장 레코드 예시

```json
// 1번째 항목 (MINTED)
{
  "id": "a1b2c3d4-...",
  "assetId": "prod-001",
  "sequenceNo": 1,
  "eventAction": "MINTED",
  "actorRole": "BRAND",
  "actorId": "brand-xyz",
  "payload": { "brandId": "brand-xyz", "modelName": "X200", "serialNo": "X200-0042" },
  "prevHash": null,
  "hash": "3c9d7f2a18e5...",
  "recordedAt": "2024-06-01T09:00:00Z"
}

// 2번째 항목 (RELEASED)
{
  "sequenceNo": 2,
  "eventAction": "RELEASED",
  "prevHash": "3c9d7f2a18e5...",   ← 이전 hash
  "hash": "9b4e1c7d2f8a...",
  "payload": { "retailId": "retail-gangnam" }
}

// 3번째 항목 (CLAIMED)
{
  "sequenceNo": 3,
  "eventAction": "CLAIMED",
  "prevHash": "9b4e1c7d2f8a...",
  "hash": "f7a3e9c1b4d2...",
  "payload": { "ownerId": "user-abc", "claimMethod": "QR" }
}
```

### 11.3 체인 무결성 검증

```java
public boolean verifyLedgerChain(List<LedgerEntry> entries) {
    String expectedPrevHash = null;
    for (LedgerEntry entry : entries) {
        // prevHash 연결 검증
        if (!Objects.equals(entry.getPrevHash(), expectedPrevHash)) {
            return false; // 체인 끊김
        }
        // 해시 재계산 검증
        String recomputedHash = computeHash(
            entry.getPrevHash(),
            entry.getEventAction(),
            entry.getPayload(),
            entry.getSequenceNo()
        );
        if (!recomputedHash.equals(entry.getHash())) {
            return false; // 위변조 감지
        }
        expectedPrevHash = entry.getHash();
    }
    return true;
}
```

---

## 12. 증빙(Evidence) 시스템

### 12.1 구조

```
service_cases
    ├── checkin_evidence_group_id  ──→  evidence_groups (type=CHECKIN)
    │                                       └──→  evidences (Before 사진들)
    └── completion_evidence_group_id ──→  evidence_groups (type=COMPLETION)
                                               └──→  evidences (After 사진 + 영수증)
```

### 12.2 증빙 업로드 흐름

```
1. 클라이언트: 파일 선택
2. POST /files/presigned-url → 서버가 S3 pre-signed URL 반환
3. 클라이언트: S3에 직접 PUT 업로드 (서버 부하 없음)
4. 업로드 완료 후: POST /provider/service/checkin (file_url 포함)
5. 서버: evidence_groups + evidences DB INSERT
```

### 12.3 파일 타입 정책

| group_type | 허용 파일 | 필수 여부 |
|-----------|----------|----------|
| `CHECKIN` | 이미지 (JPG/PNG) | 권장 |
| `COMPLETION` | 이미지 + PDF (영수증) | 권장 |

---

## 13. Outbox 패턴 & 트랜잭션 설계

### 13.1 왜 Outbox 패턴인가

외부 시스템(S3, 알림 서비스, QR 생성) 연동은 실패할 수 있다. 핵심 DB 트랜잭션과 분리하여 **최소 1회(at-least-once) 이벤트 발행**을 보장한다.

### 13.2 흐름

```
[단일 DB 트랜잭션 내에서]
1. 핵심 데이터 INSERT/UPDATE (ledger, ownership 등)
2. outbox INSERT (이벤트 기록)
[트랜잭션 커밋]

[별도 스케줄러/워커]
3. outbox WHERE status=PENDING 폴링
4. 외부 시스템에 이벤트 발행 (알림, QR 생성 등)
5. outbox UPDATE status=PROCESSED
```

### 13.3 Java 구현 예시

```java
@Transactional
public ClaimResult confirmClaim(String claimToken, UUID userId) {
    // 1. 토큰 검증
    TransferToken token = transferTokenRepository
        .findByTokenHash(sha256(claimToken))
        .orElseThrow(() -> new InvalidTokenException());
    
    validateToken(token); // state, 만료 검증
    
    // 2. 원장 기록
    LedgerEntry entry = ledgerService.record(
        token.getPassportId(),
        EventAction.CLAIMED,
        Map.of("ownerId", userId, "method", token.getAcceptMethod())
    );
    
    // 3. ownership_projections 업데이트
    ownershipProjectionRepository.save(new OwnershipProjection(
        token.getPassportId(), userId, entry.getId()
    ));
    
    // 4. 토큰 완료 처리
    token.complete(userId);
    transferTokenRepository.save(token);
    
    // 5. outbox 기록 (동일 트랜잭션)
    outboxRepository.save(new OutboxEvent(
        "PASSPORT", token.getPassportId(),
        "CLAIMED", Map.of("ownerId", userId)
    ));
    
    return new ClaimResult(token.getPassportId());
    // 트랜잭션 커밋
}
```

---

## 14. 보안 & 권한 정책

### 14.1 API 인증

```java
// 모든 확정 API에서 서버 사이드 권한 검증 필수
@PostMapping("/claim/{claimToken}")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<ClaimResponse> confirmClaim(
    @PathVariable String claimToken,
    @AuthenticationPrincipal UserPrincipal user
) {
    // 서버에서 토큰 유효성 + 소유권 검증
}
```

### 14.2 권한 검증 체크리스트

```
✅ 클레임 API: 토큰 유효 + 미사용 + 미만료 검증
✅ 양도 시작 API: ownership_projections에서 현재 소유자 검증
✅ 양도 확정 API: 토큰 유효 + 미만료 + 이전 소유자 != 현재 로그인 유저 검증
✅ 수선 승인 API: 서비스케이스의 owner_id == 로그인 유저 검증
✅ 클레임 QR 발급 API: retail의 해당 제품 ISSUE_CLAIM 권한 검증
✅ 브랜드 민팅 API: Brand 롤 검증
```

### 14.3 민감 데이터 보호

| 데이터 | 저장 방식 | 주의사항 |
|--------|----------|----------|
| 전송 토큰/코드 원문 | DB 저장 안함 | SHA-256 해시만 저장 |
| 소유자 PII | 서버 내부만 | 공개 API 응답에 절대 포함 금지 |
| QR 코드 | `qrPublicCode`만 | 내부 ID / 유저 ID 포함 금지 |

### 14.4 VOIDED/RETIRED 제품 보호

```java
// 모든 상태 변경 이벤트 전 검증
private void validateAssetIsActive(Asset asset) {
    if (asset.getState() != AssetState.ACTIVE) {
        throw new AssetNotActiveException(
            "Asset is " + asset.getState() + ". No new events allowed."
        );
    }
}
```

---

## 15. 에러 처리 & 응답 코드

### 15.1 표준 에러 응답 구조

```json
{
  "error": {
    "code": "INVALID_CLAIM_TOKEN",
    "message": "클레임 토큰이 만료되었거나 이미 사용되었습니다.",
    "details": {}
  }
}
```

### 15.2 에러 코드 목록

| HTTP | 에러 코드 | 설명 |
|------|-----------|------|
| 400 | `INVALID_CLAIM_TOKEN` | 클레임 토큰 만료/사용 완료/유효하지 않음 |
| 400 | `INVALID_TRANSFER_CODE` | 양도 코드 불일치 또는 만료 |
| 400 | `ASSET_NOT_ACTIVE` | 제품이 RETIRED/VOIDED 상태 |
| 400 | `ALREADY_CLAIMED` | 이미 소유자가 있는 제품 |
| 400 | `DUPLICATE_TRANSFER` | 이미 진행 중인 양도 존재 |
| 401 | `UNAUTHORIZED` | 로그인 필요 |
| 403 | `NOT_OWNER` | 본인 소유 제품이 아님 |
| 403 | `NO_ISSUE_CLAIM_PERMISSION` | 판매처의 해당 제품 클레임 발급 권한 없음 |
| 404 | `ASSET_NOT_FOUND` | 제품 없음 |
| 404 | `PASSPORT_NOT_FOUND` | 여권 없음 |
| 409 | `SERVICE_CASE_ALREADY_PROCESSED` | 이미 처리된 서비스 케이스 |

---

## 16. Java 구현 가이드라인

### 16.1 프로젝트 패키지 구조 (권장)

```
com.attestry.dpp
├── domain
│   ├── asset
│   │   ├── Asset.java               // JPA Entity
│   │   ├── AssetState.java          // Enum
│   │   └── AssetRepository.java
│   ├── passport
│   │   ├── Passport.java
│   │   └── PassportRepository.java
│   ├── ledger
│   │   ├── LedgerEntry.java
│   │   ├── EventAction.java         // Enum
│   │   ├── LedgerRepository.java
│   │   └── LedgerHashService.java   // 해시 계산
│   ├── transfer
│   │   ├── TransferToken.java
│   │   ├── TransferState.java       // Enum
│   │   ├── AcceptMethod.java        // Enum: IN_PERSON_QR, ONE_TIME_CODE
│   │   └── TransferRepository.java
│   ├── service
│   │   ├── ServiceCase.java
│   │   ├── ServiceCaseState.java    // Enum
│   │   └── ServiceCaseRepository.java
│   ├── evidence
│   │   ├── EvidenceGroup.java
│   │   ├── Evidence.java
│   │   └── EvidenceRepository.java
│   └── ownership
│       ├── OwnershipProjection.java
│       └── OwnershipRepository.java
├── application
│   ├── claim
│   │   └── ClaimService.java
│   ├── transfer
│   │   └── TransferService.java
│   ├── service
│   │   └── ServiceCaseService.java
│   └── brand
│       └── BrandService.java
├── infrastructure
│   ├── outbox
│   │   ├── OutboxEvent.java
│   │   ├── OutboxRepository.java
│   │   └── OutboxProcessor.java     // @Scheduled
│   └── security
│       ├── HashUtils.java           // SHA-256 유틸
│       └── TokenGenerator.java      // 랜덤 코드 생성
└── interfaces
    ├── api
    │   ├── public
    │   │   └── PassportController.java
    │   ├── owner
    │   │   ├── ClaimController.java
    │   │   ├── TransferController.java
    │   │   └── ServiceController.java
    │   ├── retail
    │   │   └── RetailController.java
    │   ├── provider
    │   │   └── ProviderController.java
    │   └── brand
    │       └── BrandController.java
    └── dto
        ├── request
        └── response
```

### 16.2 핵심 유틸 클래스

```java
// HashUtils.java
@Component
public class HashUtils {
    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

// TokenGenerator.java  
@Component
public class TokenGenerator {
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();
    
    public String generateSixCharCode() {
        return random.ints(6, 0, CHARSET.length())
            .mapToObj(i -> String.valueOf(CHARSET.charAt(i)))
            .collect(Collectors.joining());
    }
    
    public String generateUuidToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
```

### 16.3 LedgerService 핵심 구현

```java
@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerRepository ledgerRepository;
    private final HashUtils hashUtils;
    private final ObjectMapper objectMapper;
    
    // 반드시 @Transactional 컨텍스트 내에서 호출할 것
    public LedgerEntry record(UUID assetId, UUID passportId,
                               EventAction action, String actorRole,
                               UUID actorId, Map<String, Object> payload) {
        // 이전 항목의 hash 조회
        String prevHash = ledgerRepository
            .findTopByAssetIdOrderBySequenceNoDesc(assetId)
            .map(LedgerEntry::getHash)
            .orElse(null);
        
        // 다음 sequence 번호
        long nextSeq = ledgerRepository.countByAssetId(assetId) + 1;
        
        // 페이로드 직렬화
        String payloadJson = serializePayload(payload);
        
        // 해시 계산
        String hashInput = String.format("%s|%s|%s|%d",
            prevHash == null ? "GENESIS" : prevHash,
            action.name(), payloadJson, nextSeq);
        String hash = hashUtils.sha256(hashInput);
        
        LedgerEntry entry = LedgerEntry.builder()
            .assetId(assetId).passportId(passportId)
            .sequenceNo(nextSeq).eventAction(action)
            .actorRole(actorRole).actorId(actorId)
            .payload(payloadJson).prevHash(prevHash).hash(hash)
            .build();
        
        return ledgerRepository.save(entry);
    }
}
```

### 16.4 기술 스택 권장 사항

| 영역 | 권장 기술 |
|------|-----------|
| Framework | Spring Boot 3.x |
| ORM | Spring Data JPA + Hibernate |
| DB | PostgreSQL 15+ |
| Migration | Flyway |
| Security | Spring Security + JWT |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Test | JUnit 5 + Mockito + TestContainers |
| Build | Gradle (Kotlin DSL) |

### 16.5 중요 구현 주의사항

```
❌ 절대 하지 말 것:
  - ledger_entries 테이블에 UPDATE/DELETE
  - DB에 토큰/코드 원문 저장 (SHA-256 해시만)
  - 공개 API 응답에 소유자 PII 포함
  - UI 버튼 숨기기로만 권한 제어 (반드시 서버 검증)
  - 민팅/클레임/양도를 여러 트랜잭션으로 분리

✅ 반드시 할 것:
  - 모든 확정 이벤트 (클레임/양도/서비스 승인)를 단일 트랜잭션으로 처리
  - Outbox 패턴으로 외부 시스템 연동
  - 진행 중 양도 1건 제한 (DB unique 또는 애플리케이션 체크)
  - 토큰 만료 시각 항상 검증
  - asset.state == ACTIVE 검증 후 이벤트 기록
```

---

## 부록: 초보 개발자 체크리스트

### 구현 전 확인
- [ ] `ledger_entries` DB 사용자에게 UPDATE, DELETE 권한 없는지 확인
- [ ] `transfer_tokens.token_hash` 컬럼에 원문이 아닌 SHA-256 해시만 저장하는지 확인
- [ ] 모든 확정 버튼 API가 `@Transactional`로 감싸여 있는지 확인

### 구현 중 확인
- [ ] 클레임 API: 토큰 유효성(미만료, 미사용) 검증 로직
- [ ] 양도 API: 현재 소유자 != 양도 수락자 검증
- [ ] 수선 승인 API: service_case.owner_id == 로그인 유저 검증
- [ ] 공개 API 응답 DTO에 PII 컬럼 없는지 확인

### 구현 후 확인
- [ ] Postman/curl로 권한 없는 사용자가 API 직접 호출했을 때 403 응답 확인
- [ ] `ledger_entries` 해시체인 검증 함수 동작 확인
- [ ] Outbox 스케줄러 동작 및 재시도 로직 확인

---

*문서 버전: v3.0 | 최종 수정: 2026-02-25 | 작성: Attestry 개발팀*
