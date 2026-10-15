# Run Shohoj Sheba

The website uses Java Spring Boot, Thymeleaf, HTML forms, and CSS. There is no site JavaScript. The app can run with a local H2 database or PostgreSQL. Flyway creates and updates the schema, and Hibernate validates it at startup.

## Local PostgreSQL setup

Use Java 21 or newer and a running PostgreSQL server. Create a dedicated database called `shohoj_sheba` and a database user with permission to create and alter its tables. This project already has a local `.env` configured for the PostgreSQL installation on this computer; the file is ignored by Git.

For another computer or database user, copy `.env.example` to `.env` and fill in `DB_URL`, `DB_USER`, and `DB_PASSWORD`. The JDBC URL has the form `jdbc:postgresql://localhost:5432/shohoj_sheba`. `DB_PASSWORD` may be empty only when the local PostgreSQL server is configured for passwordless local connections. Do not put a password in the URL or in source control.

From the `Shohoj-Sheba` directory, start the website:

```sh
./run-local-postgres.sh
```

Open `http://localhost:8080`. The first page is login; visitors can choose to continue as guests. The script loads `.env`, selects the `postgres` profile, binds the website to this computer, and runs the Spring Boot application. Flyway applies migrations V1 through V4 on the first launch and any later pending migrations on subsequent launches. Existing drafts from before accounts are retained and can be claimed by signing in and entering their original reference and private tracking code. New drafts belong to the signed-in account.

## H2 development mode

For a quick local run without PostgreSQL:

```sh
./mvnw test
./mvnw spring-boot:run
```

H2 stores accounts and drafts in the ignored `data/` directory. This mode is for local development. Switching between H2 and PostgreSQL switches databases; the accounts and drafts in one are not visible in the other.

## What visitors can do

Visitors can browse public service guides, see reviewed helplines, and use the chat page without registering. A normal HTML form sends each question to Java Spring Boot, which returns a server-rendered reply and suggestions from the enabled service catalogue. The form carries up to five recent questions between submissions; guest questions are not stored in the application's database and the thread disappears when the page is reopened. A visitor must register and sign in to create, view, or edit a saved draft. Drafts remain inside Shohoj Sheba and are not government applications.

Voice input is not included in this Java-only interface. A later version would need a chosen speech service or Java speech engine, an audio upload flow, and a privacy review. The current chat accepts typed questions.

## Deployment notes

Use the `postgres` profile with `DB_URL`, `DB_USER`, and `DB_PASSWORD` supplied through the host's secret settings. An HTTPS deployment should also activate the `https` profile so session cookies are marked `Secure`. Keep a database backup before migrations, restrict database access, and add rate limiting, account recovery, retention policy, and monitoring before inviting real citizen data. The current content is an illustrative guide; [service links](content-sources.md) and [helpline sources](helpline-sources.md) should be reviewed again before public release.

Never commit `.env`, a database export, H2 database files, real personal information, or recordings. Use synthetic data for evaluation.
