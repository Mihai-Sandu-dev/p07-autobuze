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
- [ ] **2. Schema DB**: migrarea cu tabelele GTFS + utilizatori, favorite, rapoarte, versiuni de feed; diagramă
  - [ ] Migrarea Flyway cu tabelele (V1 e încă goală; V1 nu se mai editează după Neon)
  - [ ] Diagrama schemei
- [ ] **3. Date de teren și Tursib** (D-010, D-024, D-026)
  - [x] `FIELD.md` cu cele 20 de observații
  - [x] `field/observations.csv` cu 20 de rânduri
  - [ ] **Autor:** pozele panourilor în `field/photos/<data>/` (~1600 px), coloana `poza`, secțiunea din FIELD.md (Q-017)
  - [x] Interviul 1 (L.B.)
  - [ ] **Autor:** interviurile 2 și 3 (inițiale și rol) (Q-018)
  - [ ] **Autor:** secțiunea „De ce aceste linii și stații” din FIELD.md
  - [ ] **Autor:** emailul către Tursib (office@tursib.ro) și data trimiterii → DATA.md, Q-009
- [ ] **4. README și TODO.md**
  - [x] README: scope
  - [x] README: stack
  - [ ] README: variabilele personale ale proiectului (sunt în `SCOPE.md` → „Variabilele proiectului”,
        nu în README; README are doar variabilele de mediu) — de clarificat ce cere fișa
  - [x] `TODO.md`

## Checkpoint 2 · du 29.11 (intern)

- [ ] **5. Importul GTFS** (D-003, D-019, D-038): job de descărcare cu versiune + checksum, insert în
      batch, tranzacțional, indecși expliciți; mini-feed de test
  - [ ] Jobul de import
  - [ ] **Autor:** mini-feed-ul de test: calendar cu excepții, curse după miezul nopții (25:10:00), transfer ≤ 150 m
- [ ] **6. Feed-ul real importat local și pe live**; durata și numărul de rânduri în DATA.md
  - [ ] **Autor, după descărcare:** are `shapes.txt`? (Q-010)
  - [ ] **Autor, după descărcare:** identificatorul stabil al stațiilor și regula de grupare din D-031 (Q-019)
- [ ] **7. Twist 1+2**: serviciile active într-o zi (`calendar` + `calendar_dates`) și orele ≥ 24:00; teste pe mini-feed
      (D-014, D-015: sărbătorile 2026–2027 în DB)
- [ ] **8. Pagina de stație**: „următoarele plecări”, sub 300 ms pe feed-ul real, măsurat și documentat
      (D-018, D-028, D-036, D-041)
- [ ] **9. Harta Leaflet** cu date din API, căutare după nume, „aproape de mine” (D-005, D-022, D-030, D-031, D-034);
      înlocuiește pagina „hello” de la „/” (se scoate `noindex`); Q-022 (cum spun paginile că nu e oficială Tursib)
- [ ] **10. Auth USER + ADMIN**, rute protejate pe server; favorite + pagina „Acasă” (D-011, D-020, D-022, D-032)
- [ ] **11. Pagina de linie**: traseul pe hartă, stațiile în ordine, orarul pe tipuri de zi (D-014, D-016, D-035; Q-010)

## Livrare · jo 03.12

- [ ] **12. (lu 30.11) Twist 3: planificatorul** cu maxim un transfer (stații comune sau ≤ 150 m), sub 1 s,
      teste pe mini-feed (D-002, D-012, D-013, D-033)
- [ ] **13. (ma 01.12) Abateri raportate și admin** (D-009, D-016, D-017, D-021, D-025, D-028, D-035, D-036)
  - [ ] Rapoartele de abatere și media pe intervale orare
  - [ ] Seed-ul din observațiile de teren (tabelul de corespondență: Q-020)
  - [ ] Admin: import, raportul de diferențe, moderare
- [ ] **14. (mi 02.12) Teste, documentație, predare scrisă**
  - [ ] Minim 10 teste, minim 4 pe twist (acum: 5 în `./mvnw verify` + 3 de UI, toate pe scheletul „hello”, niciunul pe twist)
  - [x] CI verde (ultimul run pe `main`: success)
  - [ ] API docs + intrările C- în CONTRACTS.md (încă niciun contract „Provided”)
  - [ ] README final
  - [x] AI_LOG ≥ 10 intrări (12)
  - [ ] `SUBMISSION.md`
- [ ] **15. (jo 03.12) Final**: video demo, verificare pe o clonă nouă, căutare de secrete, predare

## În afara celor 15 puncte (din vechiul TODO, păstrate)
- [ ] `/plan` pentru D-042: tokens noi în DESIGN.md și `tokens.css`, umbrele și mișcarea în `app.css`,
      screenshot-uri noi; DESIGN.md trimite încă la D-027.
- [ ] Mock-uri lipsă (înainte de build-ul ecranului): acasa, linie, raport-abatere, inregistrare,
      admin-import, admin-diferente, admin-rapoarte, eroare; comutatorul vizitator / logat în fiecare mock.
- [ ] Pagina de eroare pentru 403, 404 și 500 (DS-autobuze-eroare).
- [ ] Pregătirea susținerii (SCOPE.md): plecările la 00:30 într-o luni de sărbătoare; ce e precalculat
      pentru planificator și când se invalidează; cum s-au importat 200.000 de rânduri și cât a durat.
- [ ] PATTERN-001 (testele UI în imaginea Playwright) e `candidate`: de promovat sau de renunțat.
- [ ] Release: branch-uri, versiune și changelog nestabilite (AGENTS.md §5).
