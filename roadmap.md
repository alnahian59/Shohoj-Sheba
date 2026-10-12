# Core roadmap — build in stages

## Stage 1 — included in this ZIP

- [x] Spring Boot 3.5.6 project, Java 21, Maven
- [x] Service directory and illustrative Bangla guidance
- [x] Database migrations and H2 demo/Postgres option
- [x] Local assistance draft creation
- [x] Private tracking token (hash at rest) and lookup
- [x] Minimal responsive website and JSON endpoints
- [x] Typed Bangla/English keyword suggestions
- [x] Disabled official integration boundary
- [x] Unit and integration tests (run locally with Maven)

## Stage 2 — validation and trust

- [ ] Government information source directory with verified URLs, version, date checked, reviewer and expiry
- [ ] Admin portal for reviewing/approving service procedures
- [ ] Accounts, sessions, permissions and record ownership
- [ ] Consent language in Bangla and data lifecycle/retention policies
- [ ] Public deployment security review, rate limiting, HTTPS and audit logging
- [ ] Screen reader and mobile accessibility tests

## Stage 3 — user assistance

- [ ] Bangla voice input (STT), explicit user consent, manual correction
- [ ] Bangla speech output (TTS) with opt-in playback
- [ ] NLP/RAG answering *only* grounded in verified procedures; citations and safe fallbacks
- [ ] Dynamic step-by-step form wizard with validation and resuming
- [ ] OCR of permitted sample documents; secure private storage and deletion
- [ ] Background job queue for slow operations, with reliable status tracking

## Stage 4 — allowed integrations

- [ ] Per-agency legal permission and documented API contracts
- [ ] Official submission only when authorised and integrated
- [ ] Explicit confirmation by citizen before any external submission
- [ ] Separate local and official statuses; never infer official approval from local workflow
- [ ] Observability, production database backups, intrusion monitoring and recovery

**Suggested task order:** make one verified service guide robust before adding new service categories or AI features.
