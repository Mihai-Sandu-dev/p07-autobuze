# Architecture — p07-autobuze

<!-- graffino-agentic:begin v1 -->
> How this repo is built, generated from the code at backfill and kept current by
> whoever changes the structure. The code wins: when this page is wrong, fix it.
<!-- graffino-agentic:end -->

> **Nu există cod încă.** Pagina descrie structura stabilită în D-001 (aprobat).
> Se rescrie din cod după primul build.

## Overview
Pentru Sibiu, pe feed-ul GTFS Tursib S.A. (DATA.md). O singură aplicație Spring Boot, rulată local în Docker Compose lângă o bază
PostgreSQL + PostGIS. Aplicația importă feed-ul GTFS, servește un REST API și
pagini Thymeleaf. Harta Leaflet din pagini citește datele din API.

```text
 Feed GTFS (ZIP) ──import──▶ ┌────────────── Docker Compose ──────────────┐
                             │  app: Spring Boot ───JDBC───▶ db: PostGIS  │
 Browser ◀──HTML (Thymeleaf)─│   ├─ REST API (JSON)                       │
   └─ Leaflet ──fetch JSON──▶│   └─ pagini                                │
   └─ tile-uri hartă ◀── OpenStreetMap (D-005)                         │
                             └────────────────────────────────────────────┘
```

## Parts
| Part | Path | Responsibility | Talks to |
|---|---|---|---|
| app | — (încă nu există) | import, REST API, pagini Thymeleaf | db |
| db | — (încă nu există) | date GTFS, interogări spațiale (PostGIS) | — |
| harta | — (încă nu există) | Leaflet în pagini | REST API, furnizor de tile-uri |

## Data flow
1. **Import:** feed GTFS → parsare → tabele PostGIS într-o versiune nouă, atomic, cu raport de diferențe (D-003).
2. **Căutare de stații:** zona hărții sau text → API → interogare spațială → markere pe hartă.
3. **Următoarele plecări:** stație + ora curentă → calendar + plecări → listă (doar orar static: D-004).
4. **Trasee:** linie → traseele ei → geometrie pe hartă (shapes: Q-010).
5. **Planificator:** origine + destinație + oră → itinerarii cu maxim un transfer (D-002; criterii: Q-005).
6. **Raport de abatere:** utilizator logat → raport (linie, stație, oră, minute) → moderare de admin → medie pe intervale orare pe pagina liniei; seed din FIELD.md (D-009, propus; auth: Q-014).

## State and storage
| What | Where | Owner | Notes |
|---|---|---|---|
| Date GTFS importate | PostgreSQL/PostGIS | app | schema doar prin Flyway (D-001) |

## Configuration
| Name | Where set | Used by | Notes |
|---|---|---|---|
<!-- names only, never values -->

## Build, deploy, runtime
Dezvoltare locală în Docker Compose (D-001); build cu `./mvnw` (D-006). Găzduire publică cu HTTPS (D-007), furnizor: Q-012. Comenzi: AGENTS.md secțiunea 2; release: secțiunea 5.

## Boundaries
Feed-ul GTFS e date din exterior: importul îl validează. Contractele sunt în CONTRACTS.md.
