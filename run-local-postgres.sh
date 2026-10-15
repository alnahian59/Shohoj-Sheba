#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
if [[ ! -f .env ]]; then
  printf 'Missing .env. Copy .env.example and set your local PostgreSQL connection.\n' >&2
  exit 1
fi

set -a
source .env
set +a

: "${DB_URL:?DB_URL is required in .env}"
: "${DB_USER:?DB_USER is required in .env}"
: "${DB_PASSWORD?DB_PASSWORD must be defined in .env (it may be empty for local trust authentication)}"

SERVER_ADDRESS=127.0.0.1 SPRING_PROFILES_ACTIVE=postgres exec ./mvnw spring-boot:run
