# Decisions — p07-autobuze

<!-- graffino-agentic:begin v1 -->
> Constraints the code must obey. Status: `approved` (obey) · `proposed` (do not
> build on it) · `superseded-by <ID>` (kept to explain old code).
> Adding or changing an entry needs the user's OK (AGENTS.md → The gate).
<!-- graffino-agentic:end -->

## Index
### [Abateri si teren](decisions/abateri-si-teren.md)
- D-009 · Rapoartele de abatere: utilizatorii logați raportează, adminul moderează
- D-010 · Datele de teren: FIELD.md în root, observațiile în field/observations.csv
- D-016 · Abaterea medie pe linie: pe intervale de o oră, separat pe tip de zi
- D-017 · Raportul de abatere: utilizatorul alege o plecare din orar și scrie ora reală
- D-028 · Raportul de abatere pornește din grupul de linie și sens; doar plecări din ultimele 60 de minute
- D-021 · Corespondența dintre datele de teren și feed: un tabel completat de autor
- D-024 · Formatul datelor de teren în CSV și DB: dată YYYY-MM-DD, oră HH:MM (în DB: D-043), abatere întreg cu semn
- D-025 · Observațiile de teren intră ca rapoarte de abatere cu sursa „teren”
- D-026 · Coloana `poza`: calea relativă a pozei cu panoul stației, sau goală
- D-035 · Abaterile vizibile pe pagina liniei: media pe ore și ultimele 20 de rapoarte, cu sursa
- D-036 · Raportul fără plecări în ultimele 60 de minute: butonul rămâne, formularul o spune
- D-045 · Adminul șterge un raport de abatere definitiv (DELETE), fără marcare

### [Date gtfs](decisions/date-gtfs.md)
- D-003 · Re-importul creează o versiune nouă a feed-ului, atomic, cu raport de diferențe
- D-004 · Doar orarul static GTFS; GTFS-Realtime e în afara scopului
- D-019 · Jobul de import: zilnic și la cererea adminului; versiune nouă doar la checksum nou
- D-020 · Rapoartele și favoritele se leagă de identificatori stabili, nu de o versiune de feed
- D-031 · O stație = parent_station din feed; altfel, rândurile din stops.txt cu același nume la maxim 150 m
- D-038 · Importul rulează pe Render, pornit de un cron din GitHub Actions printr-un endpoint protejat
- D-043 · Orele din feed se țin în DB ca int, secunde de la începutul zilei de serviciu
- D-044 · Schema: tabele GTFS pe versiune de feed, datele utilizatorilor fără chei spre GTFS

### [Ecrane si scop](decisions/ecrane-si-scop.md)
- D-005 · Harta folosește tile-uri OpenStreetMap, cu atribuire
- D-008 · Fiecare pagină afișează în subsol „Date de transport: Tursib S.A.”
- D-022 · Pagina principală: hartă pentru vizitatori, „Acasă” pentru utilizatorii logați
- D-023 · În afara scopului: alte orașe, rutare pe străzi, notificări, aplicație mobilă, bilete, alte limbi
- D-030 · Harta e și la /harta, pentru toți; „Hartă” din antet duce acolo
- D-034 · „Aproape de mine”: cele mai apropiate 3 stații, pe o rază de maxim 1 km
- D-039 · Tema: după sistem, cu un buton light/dark în antet, ținut în browser
- D-041 · Numele aplicației: „Staționar — când vine autobuzul?”, scurt „Staționar”
- D-042 · Direcția vizuală: bază ușor colorată „rece”, umbre moi și mișcare lină; orele rămân de contrast maxim
- D-049 · Subsolul spune că aplicația e neoficială, lângă atribuirea Tursib
- D-050 · Paginile cu plecări se randează pe server; același serviciu dă endpoint-ul JSON
- D-052 · O plecare la mai puțin de un minut apare „acum” pe pagina de stație
- D-054 · Căutarea stațiilor după nume se face în browser, fără diacritice

### [Orar si plecari](decisions/orar-si-plecari.md)
- D-014 · Tipuri de zi: lucrătoare, sâmbătă, duminică și sărbătoare legală
- D-015 · Lista sărbătorilor legale e în DB, doar pentru eticheta zilei și media abaterilor
- D-018 · Următoarele plecări: primele 3 pe fiecare linie și sens, completate cu ziua următoare
- D-047 · Orele din feed sunt ora de pe ceas, ca pe panoul din stație
- D-048 · Următoarele plecări nu arată cursele care se termină în stație
- D-051 · Un grup din „Următoarele plecări” = linie + capăt (trip_headsign)

### [Planificator](decisions/planificator.md)
- D-002 · Planificatorul caută itinerarii cu maxim un transfer
- D-012 · Transfer: stații la maxim 150 m; timp minim 2 min plus mersul pe jos la 1,2 m/s
- D-013 · Planificatorul arată primele 3 itinerarii, ordonate după sosire
- D-033 · Planificatorul: data și ora alese în perioada de valabilitate a feed-ului; continuă cu ziua următoare

### [Stack si infrastructura](decisions/stack-si-infrastructura.md)
- D-001 · Stack: Java 21 + Spring Boot, PostgreSQL/PostGIS, Flyway, Thymeleaf + Leaflet; dezvoltare locală în Docker Compose
- D-006 · Build cu Maven (./mvnw); teste cu JUnit 5 și Testcontainers PostGIS
- D-007 · Aplicația e găzduită pe un URL public, cu HTTPS
- D-011 · Autentificare cu Spring Security: email + parolă BCrypt, sesiune, roluri USER și ADMIN
- D-029 · Screenshot-uri cu Playwright headless
- D-032 · Autentificare și înregistrare: întoarcere pe pagina de origine; email unic, parolă de minim 8 caractere și maxim 72 de octeți
- D-037 · Găzduire: Render (aplicația din Dockerfile) + Neon (PostgreSQL cu PostGIS)
- D-040 · Conexiunea la Neon verifică certificatul serverului: sslmode=verify-full
- D-046 · CSV-ul din feed se citește cu Apache Commons CSV
- D-053 · Leaflet se servește din fișiere copiate în `static/vendor/leaflet/`

---
