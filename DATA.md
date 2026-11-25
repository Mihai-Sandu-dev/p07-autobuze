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
| **Total** | **183.887** |

Feed-ul Tursib are **183.887 de rânduri în total (93.218 în `stop_times.txt`)**, sub pragul de
200.000 de rânduri din catalog. `feed_info.txt` (1 rând) nu se importă.

| Unde | Durata importului | Memoria | Sursa |
|---|---|---|---|
| Live (Render gratuit → Neon) | 22,8 s (`durationMs` 22.820); job-ul GitHub Actions 1 min 4 s | nemăsurată: planul gratuit Render nu arată memoria; aplicația nu a repornit în timpul importului (starea a ajuns `DONE`, nu `IDLE`) | workflow-ul „Import GTFS”, pornit manual (`workflow_dispatch`); capturile autorului |
| Local (Docker Compose, PostGIS 17) | 5,9 s (`durationMs` 5.885) | containerul aplicației: 293 MiB înainte, maxim 332 MiB văzut în timpul importului (`docker stats`, 3 citiri, deci aproximativ); JVM cu `-Xmx300m`, ca pe Render | `POST`/`GET /admin/import`, verificat cu SQL în baza locală |

Verificat local cu SQL: 93.218 rânduri în `stop_times`, 84.954 în `shapes`, toate stațiile cu punct
(`geog`), 48 de plecări la ≥ 24:00 (cea mai târzie 24:21:10). Rândurile de pe Neon nu sunt verificate
separat cu SQL; numărul vine din starea importului de pe live, egal cu cel local.

## Probleme găsite
<!-- se completează la import -->

## Curățare
<!-- se completează la import -->
