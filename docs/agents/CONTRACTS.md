# Contracts — p07-autobuze

<!-- graffino-agentic:begin v1 -->
> Promises across a boundary. **Provided:** this repo owns the contract; the text
> lives here only. **Consumed:** a link to the provider's entry plus the fields this
> repo relies on. **External:** what we rely on from a third-party service.
> Changing a provided contract is a PROPOSAL (AGENTS.md → The gate) and the plan
> lists the consumers' files too. Status: `approved` · `proposed` · `superseded-by`.
<!-- graffino-agentic:end -->

## Index
- C-001 · Endpoint-ul de import al feed-ului GTFS · provided
- C-002 · Următoarele plecări dintr-o stație, în JSON · provided

---

## Provided

### C-001 · Endpoint-ul de import al feed-ului GTFS
- **Status:** approved
- **Date:** 2026-11-25
- **Interface:** `POST /admin/import` pornește importul (D-019, D-038) și răspunde imediat; `GET /admin/import` dă starea ultimului import. Ambele cer antetul `Authorization: Bearer <IMPORT_TOKEN>` (variabilă de mediu pe Render, secret în GitHub).
- **Responses:**
  - `POST`: `202` + starea, dacă importul a pornit; `409` + starea, dacă rulează deja.
  - `GET`: `200` + starea.
  - Ambele: `401` fără token sau cu token greșit; `404` dacă `IMPORT_TOKEN` nu e setat pe server.
- **Body (JSON):** `state` (`IDLE` · `RUNNING` · `DONE` · `UNCHANGED` · `FAILED`), `checksum` (SHA-256, hex), `sourceUrl`, `feedVersionId` (doar la `DONE`), `rows` (tabel → număr de rânduri), `startedAt`, `finishedAt` (ISO-8601), `durationMs`, `message` (motivul la `FAILED`). Câmpurile necunoscute încă sunt `null`.
- **Consumers:** `.github/workflows/import.yml` (cron zilnic; întreabă starea la fiecare minut până la `DONE`, `UNCHANGED` sau `FAILED`). Mai târziu, butonul adminului (D-038).
- **Evidence:** planul importului GTFS, aprobat de utilizator 2026-11-25
- **Confirmed:** 2026-11-25

### C-002 · Următoarele plecări dintr-o stație, în JSON
- **Status:** approved
- **Date:** 2026-11-25
- **Interface:** `GET /api/statii/{stopId}/plecari`, public. `stopId` e orice `stop_id` al stației (Q-019); răspunsul e pentru toată stația (D-031). Ora curentă e cea de pe ceas, Europe/Bucharest (D-047). Aceleași date ca pagina `/statie/{stopId}` (D-050).
- **Responses:** `200` + JSON; `404` pentru un `stopId` necunoscut în versiunea de feed activă. Fără versiune de feed activă: `200` cu `feedImported: false`, `station: null` și `groups` gol.
- **Body (JSON):** `station` {`name`, `stopIds[]`}, `now` (ISO-8601, ora locală), `dayLabel` (de ex. „Miercuri, 25 noiembrie · zi lucrătoare”), `feedImported` (bool), `groups[]` {`line`, `headsign`, `color`, `textColor` (hex fără `#`, după regula de contrast din DESIGN.md), `departures[]` {`time` („HH:mm”), `at` (ISO-8601, ora locală), `tomorrow` (bool), `minutes` (int, de acum)}}. Regulile listei: D-018, D-048, D-051.
- **Consumers:** niciunul încă; harta (punctul 9) și „Acasă” (punctul 10).
- **Evidence:** planul punctului 8, aprobat de utilizator 2026-11-25
- **Confirmed:** 2026-11-25

## Consumed
Niciunul.

## External

### Tursib S.A. · feed-ul GTFS Schedule al Sibiului
- **Interface:** specificația GTFS Schedule, https://gtfs.org/documentation/schedule/reference/ ; feed oficial https://tursib.ro/trasee/gtfs ; copie de rezervă https://mobilitydatabase.org/feeds/gtfs/mdb-2099
- **We rely on:** `agency`, `stops`, `routes`, `trips`, `stop_times`, `calendar` / `calendar_dates`; `shapes` pentru trasee (dacă există: Q-010). Doar static (D-004).
- **Known quirks:** licență nedeclarată în `feed_info.txt`; atribuire „Tursib S.A.” obligatorie (DATA.md, D-008). Problemele de date se notează în DATA.md la import.
- **Evidence:** nu există cod încă

### OpenStreetMap · tile-uri pentru fundalul hărții Leaflet
- **Interface:** tile-urile OpenStreetMap (D-005)
- **We rely on:** tile-uri raster pentru zona orașului
- **Known quirks:** politica de utilizare a tile-urilor OSM: atribuirea „© OpenStreetMap contributors” obligatorie, fără trafic intens
- **Evidence:** nu există cod încă
