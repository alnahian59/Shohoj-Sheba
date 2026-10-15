# Architecture — Core components and responsibilities

This document describes an independent implementation for Shohoj Sheba; it is not a port or clone of any other repository.

## Logical flow

```text
User (browser / JSON client)
      |
      +-- Web pages (web/PageController -> Thymeleaf)
      |
      +-- API routes (api/ApiController)
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

No official submission connector or route exists in this MVP.
```

## Core now (every file has a purpose)

| Path | Component responsibility |
| --- | --- |
| `pom.xml` | Maven + pinned Spring Boot version and dependencies |
| `ShohojShebaApplication.java` | Start the application |
| `config/SecurityConfig.java` | Public route access, CSP, form and API CSRF |
| `catalog/ServiceDefinition.java` | Database entity representing service guidance |
| `catalog/ServiceDefinitionRepository.java` | Query enabled guidance entries |
| `catalog/CatalogService.java` | Reusable service lookup logic |
| `request/AssistanceRequest.java` | Local draft entity and request state |
| `request/AssistanceRequestRepository.java` | Draft persistence |
| `request/AssistanceRequestService.java` | Generate and verify tracking code, create, retrieve and version-check draft edits |
| `request/RequestState.java` | Local draft state enum only |
| `intent/IntentResolver.java` | Interface for future intent providers |
| `intent/KeywordIntentResolver.java` | Demonstration matching typed text to service groups |
| `dto/*.java` | External input/output shapes and input constraints |
| `api/ApiController.java` and `api/ApiExceptionHandler.java` | JSON endpoints and safe API errors |
| `web/PageController.java` | HTML routes, form validation and presentation |
| `application*.yml` | H2 demo, PostgreSQL and HTTPS profile configuration |
| `db/migration/*.sql` | Versioned Flyway schema migration and illustrative service seed data |
| `templates/*.html` | Server-rendered interface |
| `static/css/style.css` | Responsive styles |
| `static/js/site.js` | Progressive enhancement for browser interactions |
| `src/test/**` | Tests |
| `docs/database.md` | Local run and PostgreSQL connection instructions |

## Data model

**service_definitions**: code (PK), title_bn, title_en, description_bn, checklist_bn, enabled.

**assistance_requests**: reference (UUID text, PK), service_code (FK), question (optional, up to 500 chars), state, tracking_hash (SHA-256 hex, not the original code), created_at, version (optimistic locking).

**Local request lifecycle (MVP)**: `DRAFT` only. The MVP does not implement state transitions. This is never an official processing state.

## Boundaries and limitations

- No government API or web automation is used.
- Draft creation and editing only change local database rows. There is no official submission action.
- Do not misrepresent demonstration service text as official requirements.
- No identity fields or file upload routes are provided.
- No users, roles, staff/admin dashboard or ownership accounts are implemented.
- The tracking code behaves as a bearer secret. Anyone with both parts can view the limited draft state, so safeguard them.
- Production requires HTTPS, secure authenticated sessions, CSRF/authorization review, safe exception reporting, rate limiting, consent, retention and monitoring.
