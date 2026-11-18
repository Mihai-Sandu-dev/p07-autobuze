# Decisions — p07-autobuze

<!-- graffino-agentic:begin v1 -->
> Constraints the code must obey. Status: `approved` (obey) · `proposed` (do not
> build on it) · `superseded-by <ID>` (kept to explain old code).
> Adding or changing an entry needs the user's OK (AGENTS.md → The gate).
<!-- graffino-agentic:end -->

## Index
- D-001 · Stack: Java 21 + Spring Boot, PostgreSQL/PostGIS, Flyway, Thymeleaf + Leaflet; dezvoltare locală în Docker Compose
- D-002 · Planificatorul caută itinerarii cu maxim un transfer
- D-003 · Re-importul creează o versiune nouă a feed-ului, atomic, cu raport de diferențe
- D-004 · Doar orarul static GTFS; GTFS-Realtime e în afara scopului
- D-005 · Harta folosește tile-uri OpenStreetMap, cu atribuire
- D-006 · Build cu Maven (./mvnw); teste cu JUnit 5 și Testcontainers PostGIS
- D-007 · Aplicația e găzduită pe un URL public, cu HTTPS
- D-008 · Fiecare pagină afișează în subsol „Date de transport: Tursib S.A.”
- D-009 · Rapoartele de abatere: utilizatorii logați raportează, adminul moderează · _proposed_

---

## D-001 · Stack: Java 21 + Spring Boot, PostgreSQL/PostGIS, Flyway, Thymeleaf + Leaflet; dezvoltare locală în Docker Compose
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Backend Java 21 + Spring Boot, expus ca REST API. Date în PostgreSQL cu PostGIS; schema doar prin migrări Flyway. Paginile sunt Thymeleaf; harta e Leaflet și își ia datele din API-ul aplicației. Dezvoltarea locală rulează în Docker Compose.
- **Why:** Alegerea utilizatorului la setup. PostGIS acoperă căutarea spațială a stațiilor; Flyway ține schema versionată.
- **Rejected:** React/Vite, Laravel, Python (FastAPI) — oferite la setup, neselectate.
- **Evidence:** cerința utilizatorului la setup, 2026-11-18 (nu există cod încă)
- **Source:** user · confidence high
- **Owner note:** 2026-11-18: reformulat din „totul rulează local” după D-007 (găzduire publică).
- **Confirmed:** 2026-11-18

## D-002 · Planificatorul caută itinerarii cu maxim un transfer
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Planificatorul întoarce doar itinerarii cu o cursă directă sau cu exact un transfer; niciodată două sau mai multe.
- **Why:** Cerință din catalog: „un planificator simplu cu maxim un transfer”.
- **Rejected:** rutare multi-transfer (de ex. RAPTOR complet) — în afara scopului cerut.
- **Evidence:** cerința utilizatorului la setup, 2026-11-18 (nu există cod încă)
- **Source:** user · confidence high
- **Owner note:** cerință din catalog; se păstrează.
- **Confirmed:** 2026-11-18

## D-003 · Re-importul creează o versiune nouă a feed-ului, atomic, cu raport de diferențe
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Fiecare import creează o versiune nouă a feed-ului GTFS, într-o singură tranzacție: fie se importă tot, fie nimic. Importul produce un raport de diferențe față de versiunea anterioară: stații noi, stații dispărute, trasee schimbate.
- **Why:** Cerință din catalog (raportul de diferențe). Un import eșuat nu trebuie să lase date pe jumătate.
- **Rejected:** înlocuirea datelor pe loc, fără versiuni (nu permite raportul de diferențe).
- **Evidence:** răspunsul utilizatorului la Q-002, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-004 · Doar orarul static GTFS; GTFS-Realtime e în afara scopului
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Următoarele plecări și planificatorul folosesc doar orarul static GTFS. Aplicația nu citește GTFS-Realtime. Abaterile de la orar se cunosc din rapoartele utilizatorilor și din observațiile de teren ale autorului.
- **Why:** Decizia utilizatorului: realtime e în afara scopului.
- **Rejected:** GTFS-Realtime.
- **Evidence:** răspunsul utilizatorului la Q-003, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-005 · Harta folosește tile-uri OpenStreetMap, cu atribuire
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Fundalul hărții Leaflet vine din tile-urile OpenStreetMap. Harta afișează mereu atribuirea „© OpenStreetMap contributors”.
- **Why:** Alegerea utilizatorului; atribuirea e cerută de licența OSM.
- **Rejected:** alți furnizori de tile-uri.
- **Evidence:** răspunsul utilizatorului la Q-004, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-006 · Build cu Maven (./mvnw); teste cu JUnit 5 și Testcontainers PostGIS
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Build-ul folosește Maven prin wrapper-ul `./mvnw`. Testele sunt JUnit 5; testele cu baza de date rulează pe PostgreSQL/PostGIS prin Testcontainers.
- **Why:** Alegerea utilizatorului. Testcontainers testează pe același PostGIS ca în producție, nu pe o bază în memorie.
- **Rejected:** Gradle.
- **Evidence:** răspunsul utilizatorului la Q-006, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-007 · Aplicația e găzduită pe un URL public, cu HTTPS
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Aplicația se publică pe un URL public, doar prin HTTPS. Furnizorul de găzduire se alege mai târziu (Q-012).
- **Why:** Cerință din catalog.
- **Rejected:** rulare doar locală.
- **Evidence:** răspunsul utilizatorului la Q-008, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-008 · Fiecare pagină afișează în subsol „Date de transport: Tursib S.A.”
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Subsolul fiecărei pagini afișează textul „Date de transport: Tursib S.A.”.
- **Why:** Atribuirea cerută pentru datele Tursib (DATA.md → Licență).
- **Rejected:** atribuire doar pe o pagină „Despre”.
- **Evidence:** cerința utilizatorului, 2026-11-18; DATA.md
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-009 · Rapoartele de abatere: utilizatorii logați raportează, adminul moderează
- **Status:** proposed
- **Date:** 2026-11-18
- **Rule:** Un utilizator logat raportează o abatere pentru o linie, într-o stație, la o oră (ex. „a venit cu 7 minute întârziere”). Pagina liniei arată abaterea medie raportată, pe intervale orare. Adminul moderează rapoartele. Observațiile de teren din FIELD.md intră ca seed în aceleași date.
- **Why:** Cerință din catalog. Completează D-004: abaterile vin din rapoarte, nu din GTFS-Realtime.
- **Rejected:** rapoarte anonime (doar utilizatori logați); un depozit separat pentru observațiile de teren.
- **Evidence:** răspunsul utilizatorului la Q-011, 2026-11-18
- **Source:** user · confidence high
