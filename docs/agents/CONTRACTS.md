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

REST API-ul aplicației (consumat de harta Leaflet) primește o intrare C- separată când
primul endpoint e planificat.

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
