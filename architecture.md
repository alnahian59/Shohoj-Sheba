# Architecture — Core components and responsibilities

This document describes an independent implementation for Shohoj Sheba; it is not a port or clone of any other repository.

## Logical flow

```text
User (browser / JSON client)
      |
      +-- Web pages (web/PageController -> Thymeleaf)
      |
      +-- API routes (api/*Controller)
                   |
             DTO validation
                   |
          +--------+-------------+
          |        |             |
      catalog/  intent/       request/
          |        |             |
          |    keyword       random token
          |    matching      SHA-256 hash
          |        |             |
          +--------+-------------+
                   |
           Spring Data JPA
                   |
              H2 / PostgreSQL
                   ^
              Flyway SQL

integration/OfficialSubmissionPort is intentionally DISABLED.
```

## Core now (every file has a purpose)

| Path | Component responsibility |
| --- | --- |
| `pom.xml` | Maven + pinned Spring Boot version and dependencies |
| `ShohojShebaApplication.java` | Start the application |
| `config/SecurityConfig.java` | Access policy, CSP, form CSRF |
| `catalog/ServiceDefinition.java` | Database entity representing service guidance |
| `catalog/ServiceDefinitionRepository.java` | Query enabled guidance entries |
| `catalog/ServiceCatalog.java` | Reusable service lookup logic |
| `request/AssistanceRequest.java` | Local draft entity and request state |
| `request/AssistanceRequestRepository.java` | Draft persistence |
| `request/AssistanceRequestService.java` | Generate and verify tracking token, create and retrieve drafts |
| `request/RequestState.java` | Local draft state enum only |
| `intent/IntentMatcher.java` | Interface for future intent providers |
| `intent/KeywordIntentMatcher.java` | Demonstration matching typed text to service groups |
| `dto/*.java` | External input/output shapes and input constraints |
| `api/*.java` | JSON endpoints (controllers do not implement persistence) |
| `web/PageController.java` | HTML routes, form validation and presentation |
| `integration/*.java` | Future authorised external-system boundary (blocked by default) |
| `application*.yml` | Runtime configuration |
| `db/migration/*.sql` | Repeatable versioned database initialization |
| `templates/*.html` | Server-rendered interface |
| `static/css/style.css` | Responsive styles |
| `static/js/intent.js` | Optional progressive-enhancement client for text intent |
| `src/test/**` | Tests |

## Data model

**service_definitions**: code (PK), title_bn, title_en, description_bn, checklist_bn, enabled.

**assistance_requests**: reference (UUID text, PK), service_code (FK), question (optional, up to 500 chars), state, tracking_hash (SHA-256 hex, NOT original code), created_at.

**Local request lifecycle (MVP)**: `DRAFT` only. Enum also defines future `NEEDS_INFORMATION` and `READY_FOR_OFFICIAL_PORTAL`; the MVP does not implement transitions. None of these represent official processing states.

## Boundaries and limitations

- No government API or web automation is used.
- Do not misrepresent demonstration service text as official requirements.
- No identity fields or file upload routes are provided.
- No users, roles, staff/admin dashboard or ownership accounts are implemented.
- The tracking code behaves as a bearer secret. Anyone with both parts can view the limited draft state, so safeguard them.
- Production requires HTTPS, secure authenticated sessions, CSRF/authorization review, safe exception reporting, rate limiting, consent, retention and monitoring.
