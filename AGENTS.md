# Repository Guidelines

## Project Structure & Module Organization
This backend is a Java 17 Spring Boot service using Clean Architecture under `src/main/java/com/attestry/dpp`.
- `domain/`: core models, repository interfaces, domain services, exceptions.
- `application/`: use cases (`command/`, `query/`), DTOs, ports.
- `infrastructure/`: web controllers, security, persistence adapters, configuration.

Tests live in `src/test/java` (unit/use-case and integration tests). Runtime configs are in `src/main/resources` (`application.yml`, `application-local.yml`, `application-prod.yml`), and test config is in `src/test/resources/application-test.yml`.

## Build, Test, and Development Commands
- `docker-compose up -d`: start local PostgreSQL and Minio.
- `./gradlew clean build`: compile, run all tests, and build artifacts.
- `./gradlew bootRun`: run the API locally on `localhost:8080`.
- `./gradlew test`: run all JUnit 5 tests.
- `./gradlew test --tests "com.attestry.dpp.application.usecase.TransferCommandUseCaseHandlerTest"`: run a single test class.

## Coding Style & Naming Conventions
- Use 4-space indentation and standard Java formatting.
- Keep boundaries strict: `domain` must not depend on `infrastructure`.
- Use naming patterns consistently: `*Controller`, `*UseCase`, `*UseCaseHandler`, `Jpa*Repository`.
- Prefer descriptive method names (for example, `findInitiatedTransfer`, `validateOwnership`) and domain-specific exceptions.

## Testing Guidelines
- Use Spring Boot Test, JUnit 5, and Spring Security Test.
- Mirror production package paths under `src/test/java`.
- Use `*Test` suffix for test classes; use nested test classes for scenario grouping when helpful.
- Prioritize coverage for state transitions and business rules (registration approval, transfer accept/cancel, ledger append behavior).

## Commit & Pull Request Guidelines
Git history is not available in this workspace snapshot, so follow this standard.
- Write commit messages in imperative Conventional Commit style, e.g. `feat: add transfer acceptance validation`.
- Keep each commit focused and runnable (build and tests pass).
- Include in PRs: behavior summary, affected endpoints/modules, test evidence (`./gradlew test`), configuration changes, and sample API requests/responses when endpoints change.

## Security & Configuration Tips
- Never commit secrets; use env vars such as `JWT_SECRET` and `CORS_ALLOWED_ORIGINS`.
- Read ownership-sensitive identifiers from JWT principal, not request bodies.
- Preserve ledger immutability: append only; never update or delete existing ledger rows.
