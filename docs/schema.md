# Schema bazei de date

Sursa: [`V2__schema.sql`](../src/main/resources/db/migration/V2__schema.sql) (V1 e goală).
PostgreSQL 17 cu PostGIS, local (Docker Compose) și pe Neon. Deciziile: D-043, D-044.

```mermaid
erDiagram
    feed_versions ||--o{ agency : ""
    feed_versions ||--o{ routes : ""
    feed_versions ||--o{ stops : ""
    feed_versions ||--o{ calendar : ""
    feed_versions ||--o{ calendar_dates : ""
    feed_versions ||--o{ shapes : ""
    feed_versions ||--o{ trips : ""
    feed_versions ||--o{ stop_times : ""
    routes ||--o{ trips : "route_id"
    trips ||--o{ stop_times : "trip_id"
    stops ||--o{ stop_times : "stop_id"
    users ||--o{ favorites : "user_id"
    users |o--o{ deviation_reports : "user_id (doar sursa utilizator)"

    feed_versions {
        bigint id PK
        text checksum
        text source_url
        timestamptz imported_at
        boolean active "una singura true"
    }
    agency {
        bigint feed_version_id PK, FK
        text agency_id PK
        text agency_name
        text agency_url
        text agency_timezone
        text agency_lang
        text agency_phone
    }
    routes {
        bigint feed_version_id PK, FK
        text route_id PK
        text agency_id
        text route_short_name
        text route_long_name
        smallint route_type
        text route_color
        text route_text_color
    }
    stops {
        bigint feed_version_id PK, FK
        text stop_id PK
        text stop_code
        text stop_name
        float stop_lat
        float stop_lon
        smallint location_type
        text parent_station
        geography geog "din lat/lon, index GIST"
    }
    calendar {
        bigint feed_version_id PK, FK
        text service_id PK
        boolean monday_to_sunday "7 coloane"
        date start_date
        date end_date
    }
    calendar_dates {
        bigint feed_version_id PK, FK
        text service_id PK
        date date PK
        smallint exception_type "1 adaugat, 2 scos"
    }
    shapes {
        bigint feed_version_id PK, FK
        text shape_id PK
        int shape_pt_sequence PK
        float shape_pt_lat
        float shape_pt_lon
        float shape_dist_traveled
    }
    trips {
        bigint feed_version_id PK, FK
        text trip_id PK
        text route_id FK
        text service_id
        text trip_headsign
        smallint direction_id "0 sau 1"
        text shape_id
    }
    stop_times {
        bigint feed_version_id PK, FK
        text trip_id PK, FK
        int stop_sequence PK
        text stop_id FK
        int arrival_sec "secunde, poate depasi 86400"
        int departure_sec "secunde, poate depasi 86400"
        smallint pickup_type
        smallint drop_off_type
    }
    users {
        bigint id PK
        text email "unic pe lower(email)"
        text password_hash "BCrypt"
        text role "USER sau ADMIN"
        timestamptz created_at
    }
    favorites {
        bigint id PK
        bigint user_id FK
        text stop_key "unic cu user_id"
        timestamptz created_at
    }
    deviation_reports {
        bigint id PK
        text source "utilizator sau teren"
        bigint user_id FK "null pentru teren"
        text route_short_name
        text stop_key
        smallint direction_id
        date service_date
        int feed_time_sec
        int actual_time_sec
        int deviation_min "cu semn, pozitiv = intarziere"
        timestamptz created_at
    }
```

## Cum se citește
- **Versiunile de feed (D-003, D-019, D-044).** Fiecare tabel GTFS are cheia
  `(feed_version_id, id-ul din GTFS)`, deci versiunea activă și cea anterioară stau una lângă
  alta (raportul de diferențe le compară). Ștergerea unei versiuni din `feed_versions` șterge în
  cascadă toate rândurile ei. Un singur rând are `active = true` (index unic parțial).
- **Orele (D-043).** `arrival_sec`, `departure_sec`, `feed_time_sec`, `actual_time_sec`: secunde de
  la începutul zilei de serviciu, ca în GTFS. 25:10:00 → 90600. Pe ecran și în CSV: HH:MM (D-024).
- **Datele utilizatorilor (D-020, D-044).** `favorites` și `deviation_reports` nu au chei străine
  spre tabelele GTFS: se leagă prin `route_short_name` și `stop_key`, care rămân la fel între
  versiuni. Ce conține `stop_key` (stop_code sau stop_id) e Q-019. O favorită ține cheia unui rând
  din stație; gruparea în stații (D-031) se face la citire.
- **Rapoartele de abatere (D-009, D-025).** Sursa „utilizator” are `user_id`; sursa „teren”
  (observațiile din `field/observations.csv`) nu are. Un raport șters de admin se șterge definitiv
  (D-045, propusă).
- **Ce nu e încă în schemă:** sărbătorile legale (D-015, punctul 7 din ROADMAP), tabelul de
  corespondență teren → feed (Q-020), raportul de diferențe (D-003).
- **Termenii:** `stops` = rânduri din `stops.txt` (o stație le grupează), `routes` = linii,
  `trips` = curse, `stop_times` = plecări (CONTEXT.md).
