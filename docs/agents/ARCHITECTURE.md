# Architecture — p07-autobuze

<!-- graffino-agentic:begin v1 -->
> How this repo is built, generated from the code at backfill and kept current by
> whoever changes the structure. The code wins: when this page is wrong, fix it.
<!-- graffino-agentic:end -->

> Rescris din cod după primul build (scheletul proiectului, 2026-11-19). Importul, API-ul,
> harta și autentificarea nu există încă; secțiunile marcate „plan” descriu deciziile aprobate.

## Overview
Pentru Sibiu, pe feed-ul GTFS Tursib S.A. (DATA.md). O singură aplicație Spring Boot
(`ro.autobuze`), cu o bază PostgreSQL + PostGIS. Local: Docker Compose (D-001). Public: aplicația
pe Render, din `Dockerfile`, și baza pe Neon (D-037).

```text
 Local (docker-compose.yml)              Public (D-037)
 ┌─────────────────────────────┐         ┌──────────────── Render (gratuit) ───────────────┐
 │ app: Spring Boot :8080 ─JDBC─▶ db     │ app din Dockerfile, -Xmx300m ──JDBC/TLS──▶ Neon  │
 │      (Dockerfile)          PostGIS 17 │ HTTPS ◀── browser                  PostgreSQL 17 │
 └─────────────────────────────┘         └──────────────────────────────────────────────────┘
 Plan (D-038): GitHub Actions (cron zilnic) ──token──▶ endpoint de admin ──▶ import ──COPY──▶ Neon
```

## Parts
| Part | Path | Responsibility | Talks to |
|---|---|---|---|
| app | `src/main/java/ro/autobuze/` | pornirea (`AutobuzeApplication`); `web/HelloController`: pagina „hello” de la „/” (temporară, D-022) | db |
| pagini | `src/main/resources/templates/` | Thymeleaf; fragmentele comune `fragments/head`, `antet` (DS-autobuze-antet), `subsol` (DS-autobuze-subsol, D-008) | — |
| stiluri | `src/main/resources/static/css/` | `tokens.css` (copia tokenilor din DESIGN.md, verificată de `TokensTest`), `app.css` | — |
| tema | `src/main/resources/static/js/tema.js` + scriptul din `fragments/head` | butonul light/dark, alegerea în `localStorage` (D-039) | — |
| db | `src/main/resources/db/migration/` | schema doar prin Flyway; V1 goală | — |
| teste | `src/test/java/ro/autobuze/` | `*Test` unitare, `*IT` cu Testcontainers (PostGIS 17), `ui/*UiTest` cu Playwright pe Compose (D-029) | db, app |

## Data flow
Acum: browser → `GET /` → `HelloController` → `hello.html` (antet, text, subsol).

Plan, din deciziile aprobate:
1. **Import (D-003, D-019, D-038):** cron GitHub Actions → endpoint de admin protejat cu token → import în flux (COPY) într-o versiune nouă, atomic, cu raport de diferențe; workflow-ul întreabă starea la fiecare minut până la final.
2. **Stații, plecări, trasee, planificator, rapoarte:** D-018, D-031, D-002, D-012, D-013, D-033, D-009, D-028, D-035, D-036.

## State and storage
| What | Where | Owner | Notes |
|---|---|---|---|
| Schema și date | PostgreSQL 17 + PostGIS (Compose local; Neon public) | app | doar prin Flyway (D-001); Neon gratuit: 1 GB (D-037) |
| Alegerea de temă | `localStorage` în browser, cheia `tema` | tema.js | `light` sau `dark` (D-039) |

## Configuration
| Name | Where set | Used by | Notes |
|---|---|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | `.env` (niciodată în git; model: `.env.example`) | docker-compose.yml | baza locală |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Compose (din `.env`); Render (`render.yaml`, `sync: false`) | app | Neon: conexiunea directă, `sslmode=require` |
| `PORT` | Render | app (`server.port`) | local 8080 |
| `JAVA_TOOL_OPTIONS` | `Dockerfile` | JVM | `-Xmx300m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m -Xss512k` |
| `spring.datasource.hikari.maximum-pool-size` | `application.properties` | app | 3 |
| datele primului admin | variabile de mediu | pornirea aplicației | nume: la planul autentificării (D-011) |
<!-- names only, never values -->

## Build, deploy, runtime
- Build: Maven prin `./mvnw` (wrapper 3.3.4, Maven 3.9.16), Spring Boot 4.1.2, Java 21 (D-006). Comenzi: AGENTS.md §2.
- CI: `.github/workflows/ci.yml`, la push și PR: `./mvnw -B verify` și lint-ul registrului.
- Imagine: `Dockerfile` în două etape (JDK → JRE 21).
- Deploy: Render Blueprint `render.yaml` (plan gratuit, Frankfurt, `autoDeployTrigger: off`, health check pe „/”); deploy doar la cerere, cu acordul autorului. Baza: Neon, PostgreSQL 17, Frankfurt.
- Render gratuit: 0,1 CPU și 512 MB; serviciul adoarme după 15 min fără cereri și pornește în ~1 min.

## Boundaries
Feed-ul GTFS e date din exterior: importul îl validează. Contractele sunt în CONTRACTS.md.
