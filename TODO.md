# TODO — Staționar (P07) până la livrare

> Starea celor 15 puncte din [`docs/ROADMAP.md`](docs/ROADMAP.md) (ce și când). Cum se face
> fiecare punct se stabilește prin `/plan`, câte un punct pe `/plan` (CLAUDE.md).
> Un punct se bifează doar când toate sub-punctele lui sunt bifate, cu dovada din repo.
> Rescris pe 2026-11-23 după roadmap. Livrare: **jo 03.12.2026, 23:59**.

## Checkpoint 1 · du 22.11 (oficial)

- [x] **1. Deploy „hello” pe Render + Neon, pe HTTPS** (D-007, D-037, D-040) — https://autobuze.onrender.com
  - [x] Pagina „hello”, `Dockerfile` și `render.yaml` (Blueprint, Frankfurt, deploy doar manual)
  - [x] **Autor:** proiect Neon — PostgreSQL 17 (17.12, baza `main_db`), Frankfurt, conexiunea directă (nu „pooler”)
  - [x] **Autor:** Render → New → Blueprint din repo; `SPRING_DATASOURCE_URL`
        (`?sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory`),
        `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
  - [x] Deploy manual (făcut de autor, 2026-11-24; AI_LOG); verificare: pagina pe HTTPS (200, HTTP → 301 la HTTPS),
        V1 aplicată pe Neon, `verify-full` acceptat, pornire în ~57 s pe Render; memoria: rulează pe planul
        de 512 MB fără să cadă, consumul exact nemăsurat
- [x] **2. Schema DB**: migrarea cu tabelele GTFS + utilizatori, favorite, rapoarte, versiuni de feed; diagramă
  - [x] Migrarea Flyway `V2__schema.sql` cu cele 12 tabele și PostGIS (D-043, D-044); `SchemaIT` trece local
  - [x] Diagrama schemei: `docs/schema.md` (Mermaid)
  - [x] V2 aplicată pe Neon (deploy 2026-11-24: `flyway_schema_history` cu 1 init și 2 schema, ambele reușite; PostGIS 3.5.7)
- [ ] **3. Date de teren și Tursib** (D-010, D-024, D-026)
  - [x] `FIELD.md` cu cele 20 de observații
  - [x] `field/observations.csv` cu 20 de rânduri
  - [ ] **Autor:** pozele panourilor în `field/photos/<data>/` (~1600 px), coloana `poza`, secțiunea din FIELD.md (Q-017)
  - [x] Interviul 1 (L.B.)
  - [ ] **Autor:** interviurile 2 și 3 (inițiale și rol) (Q-018)
  - [ ] **Autor:** secțiunea „De ce aceste linii și stații” din FIELD.md
- [ ] **4. README și TODO.md**
  - [x] README: scope
  - [x] README: stack
  - [ ] README: variabilele personale ale proiectului (sunt în `SCOPE.md` → „Variabilele proiectului”,
        nu în README; README are doar variabilele de mediu) — de clarificat ce cere fișa
  - [x] `TODO.md`

## Checkpoint 2 · du 29.11 (intern)

- [x] **5. Importul GTFS** (D-003, D-019, D-038): job de descărcare cu versiune + checksum, insert în
      batch, tranzacțional, indecși expliciți; mini-feed de test
  - [x] Jobul de import: `GtfsImportService` (SHA-256, o tranzacție, câte un COPY pe fișier, 2 versiuni păstrate),
        `POST`/`GET /admin/import` cu token (C-001), `.github/workflows/import.yml`; `GtfsImportIT` și
        `ImportControllerIT` trec local. Pe live: încă nu (deploy + `IMPORT_TOKEN` pe Render și în GitHub, de la autor)
  - [x] **Autor:** mini-feed-ul de test (`src/test/resources/gtfs/mini/`), pe scenariile a–f ale autorului: scris de
        agent, verificat de autor rând cu rând (2026-11-25)
- [x] **6. Feed-ul real importat local și pe live**; durata și numărul de rânduri în DATA.md
  - [x] Pe live, 2026-11-25: `DONE` în 22,8 s, versiunea 1, 183.886 de rânduri importate (183.887 în arhivă, cu
        `feed_info.txt`, care nu se importă; corectat 2026-11-25) (workflow pornit manual; capturile autorului)
  - [x] Local, 2026-11-25: `DONE` în 5,9 s, aceleași rânduri și același checksum, verificat cu SQL
  - [x] Durata, rândurile pe fișier și memoria în DATA.md → „Import”
  - [x] **Autor, după descărcare:** are `shapes.txt`? Da, 84.954 de puncte (Q-010 închisă, 2026-11-25)
  - [x] **Autor, după descărcare:** identificatorul stabil al stațiilor: `stop_id` (Q-019 închisă, 2026-11-25);
        feed-ul nu are `parent_station`, deci gruparea din D-031 merge după nume și distanță
- [x] **7. Twist 1+2**: serviciile active într-o zi (`calendar` + `calendar_dates`) și orele ≥ 24:00; teste pe mini-feed
      (D-014, D-015: sărbătorile 2026–2027 în DB)
  - [x] `ServiceCalendar`, `Departures`, `DayType`; V3 cu 33 de sărbători (16 în 2026, 17 în 2027); D-047 (ora de pe ceas)
  - [x] V3 aplicată pe Neon (deploy `fd5bfbe`, 2026-11-25: „Successfully applied 1 migration ... now at version v3”, 0,354 s)
  - [x] Teste pe mini-feed: `ServiceCalendarIT` (8), `DeparturesIT` (6, inclusiv „luni, sărbătoare, 00:30”), `DayTypeTest` (3)
- [x] **8. Pagina de stație**: „următoarele plecări”, sub 300 ms pe feed-ul real (pragul: fișa P07, „Cerințe
      tehnice specifice”), măsurat și documentat (D-018, D-028, D-036, D-041)
  - [x] `NextDepartures`, `Stations`, `LineColors`; `/statie/{stopId}` și `/api/statii/{stopId}/plecari` (C-002,
        D-048, D-049, D-050, D-051); teste: `NextDeparturesIT` (7, pe mini-feed), `NextDeparturesTest` (7),
        `LineColorsTest` (4), `StationControllerIT` (5); screenshot-uri `DS-autobuze-statie` pe Gară (1051)
  - [x] Măsurat local: EXPLAIN ANALYZE, 13 interogări, 9,5–9,7 ms în total (DATA.md → Performanță)
  - [x] **Autor:** `docs/perf/next-departures.sql` rulat în Neon SQL Editor (26.11.2026): suma „Execution Time”
        147,4 ms, sub pragul de 300 ms (DATA.md → Performanță)
  - [x] Deploy (făcut de autor, 2026-11-26) și cererea pe live: mediana 438 ms, maxim 692 ms (10 cereri calde),
        în DATA.md, notată separat (nu e pragul); peste 300 ms, deci tema de mai jos
  - [x] Q-025: sub un minut, pagina afișează „acum” (D-052; testul în `NextDeparturesTest`)
- [x] **9. Harta Leaflet** cu date din API, căutare după nume, „aproape de mine” (D-005, D-022, D-030, D-031, D-034);
      înlocuiește pagina „hello” de la „/” (se scoate `noindex`); Q-022 (cum spun paginile că nu e oficială Tursib)
  - [x] `StationDirectory`, `MapController`: `/` și `/harta`, `GET /api/statii` și `/api/statii/aproape` (C-003,
        D-053, D-054); `station.lat/lon` în C-002; harta mică și „Vezi pe hartă” pe pagina de stație;
        „Hartă” în antet; pagina „hello” scoasă
  - [x] Teste: `StationDirectoryIT` (5), `MapControllerIT` (10, înlocuiește `HelloPageIT`), `StationControllerIT` (+1),
        `HartaUiTest` (8); screenshot-uri `DS-autobuze-harta` și `DS-autobuze-statie` (feed-ul real, local)
  - [x] Deploy (făcut de autor) și „aproape de mine” verificat de autor pe telefon, pe live (HTTPS), 2026-11-26
- [x] **10. Auth USER + ADMIN**, rute protejate pe server; favorite + pagina „Acasă” (D-011, D-020, D-022, D-032)
  - [x] Spring Security (`security/`): `/autentificare`, `/inregistrare` (→ autentificare, D-056), `/iesire`; rutele D-058
        (`/admin/import` doar cu token); adminul din mediu (D-055); favoritele, maxim 10, și „Acasă” la „/” (D-057)
  - [x] Teste: `SecurityIT` (12), `RegistrationIT` (9), `AdminAccountIT` (4), `FavoritesIT` (9), `HomePageIT` (7),
        `ReturnToTest` (11), `AuthUiTest` (2); screenshot-uri `DS-autobuze-login`, `-inregistrare`, `-acasa`
  - [x] **Autor:** `ADMIN_EMAIL` și `ADMIN_PASSWORD` în Render; deploy și verificare pe live (2026-11-26):
        login admin, favorite + „Acasă”, USER pe /admin → 403, „aproape de mine” pe telefon
- [ ] **11. Pagina de linie**: traseul pe hartă, stațiile în ordine, orarul pe tipuri de zi (D-014, D-016, D-035; Q-010)

## Livrare · jo 03.12

- [ ] **12. (lu 30.11) Twist 3: planificatorul** cu maxim un transfer (stații comune sau ≤ 150 m), sub 1 s,
      teste pe mini-feed (D-002, D-012, D-013, D-033)
- [ ] **13. (ma 01.12) Abateri raportate și admin** (D-009, D-016, D-017, D-021, D-025, D-028, D-035, D-036)
  - [ ] Rapoartele de abatere și media pe intervale orare
  - [ ] Seed-ul din observațiile de teren (tabelul de corespondență: Q-020)
  - [ ] Admin: import, raportul de diferențe, moderare
- [ ] **14. (mi 02.12) Teste, documentație, predare scrisă**
  - [ ] Minim 10 teste, minim 4 pe twist (acum: 82 în `./mvnw verify`: 30 unitare și 52 de integrare;
        pe twist: 21, adică 8 în `ServiceCalendarIT`, 6 în `DeparturesIT` și 7 în `NextDeparturesIT`;
        + testele de UI)
  - [x] CI verde (ultimul run pe `main`: success)
  - [ ] API docs + intrările C- în CONTRACTS.md (încă niciun contract „Provided”)
  - [ ] README final
  - [x] AI_LOG ≥ 10 intrări (14)
  - [ ] `SUBMISSION.md`
- [ ] **15. (jo 03.12) Final**: video demo, verificare pe o clonă nouă, căutare de secrete, predare

## În afara celor 15 puncte (din vechiul TODO, păstrate)
- [ ] Comasarea interogărilor pentru „următoarele plecări” (cererea pe live: mediana 438 ms; 13 drumuri până
      la Neon pe cerere, DATA.md → Performanță). De propus prin `/plan`; nu e cerința de 300 ms, care e pe interogare.
- [ ] Gruparea stațiilor cu nume numerotat (ex. „Aeroport 1” + „Aeroport 2”) la ≤ 150 m. Pe feed-ul real, D-031
      nu unește nimic: cele două părți ale drumului au nume diferite (DATA.md → Stațiile pe hartă). Nu e cerută
      de fișă; o regulă nouă de grupare ar schimba D-031 (PROPOSAL, prin `/plan`).
- [ ] Login direct după înregistrare (acum: pagina de autentificare cu „Contul a fost creat”, D-056), pentru o
      experiență mai scurtă. Ar schimba D-056 (PROPOSAL, prin `/plan`).
- [x] D-042, culorile: valorile „rece” în DESIGN.md și `tokens.css`, tile-urile întunecate în dark (D-059),
      screenshot-uri noi (2026-11-26)
- [ ] Restul D-042 (cere schimbări în `app.css`, nu doar tokeni): `--color-time` pe ore, umbrele moi
      (`--shadow-card`, `--shadow-button`) cu hover ridicat, fade-ul cardurilor, comutarea lină a temei.
- [ ] Mock-uri lipsă (înainte de build-ul ecranului): acasa, linie, raport-abatere, inregistrare,
      admin-import, admin-diferente, admin-rapoarte, eroare; comutatorul vizitator / logat în fiecare mock.
- [ ] Pagina de eroare pentru 403, 404 și 500 (DS-autobuze-eroare).
- [ ] Pregătirea susținerii (SCOPE.md): plecările la 00:30 într-o luni de sărbătoare; ce e precalculat
      pentru planificator și când se invalidează; cum s-au importat 200.000 de rânduri și cât a durat.
- [ ] PATTERN-001 (testele UI în imaginea Playwright) e `candidate`: de promovat sau de renunțat.
- [ ] Release: branch-uri, versiune și changelog nestabilite (AGENTS.md §5).
