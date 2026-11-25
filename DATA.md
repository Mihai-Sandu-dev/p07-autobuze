# DATA — datele GTFS ale aplicației

## Sursă
- Oraș: Sibiu. Operator: Tursib S.A.
- Feed GTFS oficial: https://tursib.ro/trasee/gtfs
- Copie de rezervă: https://mobilitydatabase.org/feeds/gtfs/mdb-2099
- Arhiva verificată la 25.11.2026: `tursib.gtfs_2026-10-v4.9.zip` (1,66 MB; `feed_version` v4.9),
  la care redirecționează adresa oficială. Conține `shapes.txt` (84.954 de puncte; Q-010 închisă).
  `stop_times.txt` are 93.218 rânduri; în total, 183.887 de rânduri în 9 fișiere.
  `stops.txt` (512 rânduri) nu are `stop_code`, iar `parent_station` e gol (Q-019).

## Licență
- Feed-ul nu are o licență declarată: câmpul de licență din `feed_info.txt` e gol (verificat la 18.11.2026).
- Termenii site-ului tursib.ro (https://tursib.ro/page/terms) permit folosirea informațiilor în scop personal, necomercial. Dreptul de autor e al TURSIB SA.
- La 29.01.2026 (nr. 525), Tursib a acordat o permisiune scrisă pentru OpenStreetMap, cu condiția atribuirii „Tursib S.A.” (https://wiki.openstreetmap.org/wiki/File:Permisiune_de_utilizare_a_datelor_format_GTFS.pdf).
- Proiectul folosește datele necomercial, în scop educațional, cu atribuire (D-008).

## Import
Primul import al feed-ului real, 25.11.2026: arhiva `tursib.gtfs_2026-10-v4.9.zip`, SHA-256
`cb0ddad96bbbac813ea78f94d728fef592f6214953005b19d7f8f6aaaafa9f91`, versiunea de feed 1, activă.

| Fișier | Rânduri |
|---|---:|
| `agency.txt` | 1 |
| `stops.txt` | 512 |
| `routes.txt` | 57 |
| `calendar.txt` | 4 |
| `calendar_dates.txt` | 2 |
| `shapes.txt` | 84.954 |
| `trips.txt` | 5.138 |
| `stop_times.txt` | 93.218 |
| **Total importat** | **183.886** |

Feed-ul Tursib are **183.887 de rânduri în total (93.218 în `stop_times.txt`)**, sub pragul de
200.000 de rânduri din catalog. `feed_info.txt` (1 rând) nu se importă, deci în DB intră 183.886
(totalul tabelului de mai sus; corectat 25.11.2026, scria 183.887).

| Unde | Durata importului | Memoria | Sursa |
|---|---|---|---|
| Live (Render gratuit → Neon) | 22,8 s (`durationMs` 22.820); job-ul GitHub Actions 1 min 4 s | nemăsurată: planul gratuit Render nu arată memoria; aplicația nu a repornit în timpul importului (starea a ajuns `DONE`, nu `IDLE`) | workflow-ul „Import GTFS”, pornit manual (`workflow_dispatch`); capturile autorului |
| Local (Docker Compose, PostGIS 17) | 5,9 s (`durationMs` 5.885) | containerul aplicației: 293 MiB înainte, maxim 332 MiB văzut în timpul importului (`docker stats`, 3 citiri, deci aproximativ); JVM cu `-Xmx300m`, ca pe Render | `POST`/`GET /admin/import`, verificat cu SQL în baza locală |

Verificat local cu SQL: 93.218 rânduri în `stop_times`, 84.954 în `shapes`, toate stațiile cu punct
(`geog`), 48 de plecări la ≥ 24:00 (cea mai târzie 24:21:10). Rândurile de pe Neon nu sunt verificate
separat cu SQL; numărul vine din starea importului de pe live, egal cu cel local.

## Performanță: următoarele plecări
Cerința (fișa P07, D-018): „următoarele plecări” sub 300 ms pe feed-ul real, **pe interogare**.
Măsurat pe Gară (`stop_id` 1051), stația cu cele mai multe plecări (1.730 de rânduri în `stop_times`),
pentru o cerere de joi, 26.11.2026, 16:00: fereastra până sâmbătă 00:00, deci 4 zile de serviciu.
O cerere face 13 interogări; textul lor exact, cu `EXPLAIN (ANALYZE, BUFFERS)`, e în
[`docs/perf/next-departures.sql`](docs/perf/next-departures.sql).

**(a) Pragul: EXPLAIN ANALYZE, suma „Execution Time” a celor 13 interogări**

| Unde | Suma | Cea mai lentă | Sursa |
|---|---:|---|---|
| Local (Docker Compose, PostGIS 17.5) | 9,5–9,7 ms (3 rulări) | stația, D-031 (~4,5 ms); lista de curse pentru D-048 (~2,6 ms, 757 de curse) | `psql` pe fișierul de mai sus, 25.11.2026 |
| Neon (PostgreSQL 17, Frankfurt) | _de completat_ | | autorul, în Neon SQL Editor, cu același fișier |

Toate cele 4 interogări de plecări folosesc indexul `stop_times_stop_departure`
(`feed_version_id, stop_id, departure_sec`); lista de curse folosește cheia primară a lui `stop_times`.

**(b) Cererea întreagă pe live (notată separat, nu e pragul)**

| Unde | Mediana | Maxim | Cum |
|---|---:|---:|---|
| Local, `GET /api/statii/1051/plecari` | 16 ms | 18 ms | 12 cereri calde, `curl -w %{time_starttransfer}` |
| Live (Render gratuit → Neon) | 438 ms | 692 ms | 10 cereri calde (după 3 de încălzire), `curl -w %{time_starttransfer}` din WSL, 26.11.2026, 00:10; minim 211 ms |

Pe live, timpul unei cereri adună cele 13 drumuri dus-întors până la Neon (aplicația pe Render,
baza pe Neon, amândouă în Frankfurt, dar pe rețele diferite) și procesorul de 0,1 CPU al planului
gratuit Render. Cererile „la rece” (Render adormit ~1 min, Neon adormit) nu intră în măsurare.

Pe live, cererea **trece de 300 ms** (mediana 438 ms). Diferența față de local (16 ms) nu vine din
interogări (pragul, (a)), ci din cele 13 drumuri până la Neon și din cei 0,1 CPU ai planului gratuit:
doar drumul până la Render, fără aplicație, a durat 108 ms (`GET /`, aceeași sesiune). Nu s-a
optimizat acum (decizia autorului, 2026-11-25); comasarea interogărilor e o temă separată în TODO.

## Probleme găsite
<!-- se completează la import -->

## Curățare
<!-- se completează la import -->
