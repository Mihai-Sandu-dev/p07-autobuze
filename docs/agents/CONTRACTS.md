# Contracts — p07-autobuze

<!-- graffino-agentic:begin v1 -->
> Promises across a boundary. **Provided:** this repo owns the contract; the text
> lives here only. **Consumed:** a link to the provider's entry plus the fields this
> repo relies on. **External:** what we rely on from a third-party service.
> Changing a provided contract is a PROPOSAL (AGENTS.md → The gate) and the plan
> lists the consumers' files too. Status: `approved` · `proposed` · `superseded-by`.
<!-- graffino-agentic:end -->

## Index

---

## Provided
Niciunul încă. REST API-ul aplicației (consumat de harta Leaflet) primește o intrare
C- când primul endpoint e planificat.

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
