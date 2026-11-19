# Când vine autobuzul?

Aplicație web pentru transportul public din Sibiu, construită pe orarul GTFS publicat de
Tursib S.A. Proiect educațional (P07), necomercial.

- Caută stațiile pe hartă, după nume sau „aproape de mine”.
- Arată următoarele 3 plecări pe fiecare linie și sens dintr-o stație, inclusiv cursele de
  după miezul nopții și excepțiile de calendar (sărbători).
- Arată traseul fiecărei linii, orarul complet pe tipuri de zi și abaterea medie raportată.
- Planificator de la o stație la alta, cu cel mult un transfer.
- Utilizatorii logați salvează stații la favorite și raportează abateri (o cursă venită mai
  devreme sau mai târziu decât în orar).

Doar orarul static: aplicația nu folosește date în timp real (GTFS-Realtime). Detalii despre
ce intră în proiect și ce nu: [`SCOPE.md`](SCOPE.md).

## Stack
Java 21 + Spring Boot (REST API), PostgreSQL cu PostGIS, migrări Flyway, pagini Thymeleaf cu o
hartă Leaflet care citește din API. Build cu Maven (`./mvnw`); teste cu JUnit 5 și
Testcontainers. Dezvoltare locală în Docker Compose. Găzduire publică, cu HTTPS: aplicația pe
Render (din `Dockerfile`), baza de date pe Neon (PostgreSQL cu PostGIS).

## Cum se pornește
Ai nevoie de Java 21 și Docker (Maven vine prin wrapper-ul `./mvnw`).

```bash
cp .env.example .env            # apoi completează POSTGRES_PASSWORD
docker compose up -d --build    # aplicația pe http://localhost:8080, cu PostgreSQL + PostGIS
./mvnw verify                   # build și teste (Testcontainers pornește PostGIS singur)
```

Celelalte comenzi (teste de UI, screenshot-uri, lint-ul registrului) sunt în
[`AGENTS.md`](AGENTS.md), secțiunea 2.

## Variabile de mediu
| Variabilă | Unde | Ce |
|---|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | `.env` (local) | baza din Docker Compose |
| `SPRING_DATASOURCE_URL`, `_USERNAME`, `_PASSWORD` | Compose (din `.env`), Render | conexiunea aplicației; pe Render, la Neon |
| `PORT` | Render | portul HTTP (local: 8080) |

Datele primului administrator vor veni tot din variabile de mediu, la pornire (niciodată din
cod sau din git); numele lor se stabilesc odată cu autentificarea.

Fișierul `.env` e ignorat de git.

## Date și atribuire
- **Date de transport: Tursib S.A.** Feed-ul GTFS oficial: https://tursib.ro/trasee/gtfs .
  Licența și condițiile de folosire sunt descrise în [`DATA.md`](DATA.md); confirmarea de la
  Tursib e încă în așteptare.
- **Hartă: © OpenStreetMap contributors.** Tile-uri OpenStreetMap, folosite cu atribuire și
  conform politicii lor de utilizare.
- **Date de teren:** 20 de observații făcute de autor în Sibiu, în
  [`FIELD.md`](FIELD.md) și [`field/observations.csv`](field/observations.csv).

Aplicația nu e oficială și nu e afiliată cu Tursib S.A.

## Documentație pentru dezvoltare
- [`AGENTS.md`](AGENTS.md): cum se lucrează în acest repo.
- [`docs/agents/`](docs/agents/): decizii, întrebări deschise și arhitectură.
- [`docs/design/`](docs/design/): reguli de design, tokens și mock-urile ecranelor.
- [`CONTEXT.md`](CONTEXT.md): termenii de domeniu.
- [`AI_LOG.md`](AI_LOG.md): jurnalul folosirii AI în proiect.
