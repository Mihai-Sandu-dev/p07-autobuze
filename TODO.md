# TODO — Staționar (P07) până la livrare

> Ce mai e de făcut, adunat pe 2026-11-19 din `SCOPE.md`, întrebările deschise
> (`docs/agents/OPEN-QUESTIONS.md`), `docs/design/registry.yaml` și deciziile aprobate.
> Termen cunoscut: deploy-ul „hello” pe URL-ul live **înainte de checkpoint-ul din
> 2026-11-22**. Data livrării finale nu e notată în repo.
> Fiecare funcție nouă trece prin `/plan` → `/build` (AGENTS.md). Ordinea din secțiunea 4 e o
> propunere, după dependențe.

## 1. Urgent: deploy-ul „hello” (până la 2026-11-22)
- [ ] **Autor:** proiect Neon — PostgreSQL 17, regiunea Frankfurt, conexiunea directă (nu „pooler”).
- [ ] **Autor:** Render → New → Blueprint din repo (`render.yaml`); valorile `SPRING_DATASOURCE_URL`
      (`?sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory`, D-040),
      `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.
- [ ] **Agent, cu acordul autorului:** deploy manual; verificare: pagina pe HTTPS, V1 aplicată pe
      Neon, conexiunea `verify-full` acceptată, memoria (≤ 512 MB) și timpul de pornire pe Render.

## 2. De la autor (date și răspunsuri)
- [ ] **Q-009:** trimite cererea către Tursib (office@tursib.ro) și dă data → DATA.md, Q-009.
- [ ] **Q-010:** după descărcarea feed-ului: are `shapes.txt`? (trasee reale sau linii drepte).
- [ ] **Q-019:** după descărcarea feed-ului: identificatorul stabil al stațiilor (`stop_code` sau
      `stop_id`) și confirmarea regulii de grupare din D-031 (`parent_station` / același nume ≤ 150 m).
- [ ] **Q-020:** locul și coloanele tabelului de corespondență teren → feed (D-021), la /plan-ul seed-ului.
- [ ] **Q-017:** pozele cu panourile: care stații, în `field/photos/<data>/` (~1600 px); coloana `poza`
      din `field/observations.csv` (D-026); secțiunea din FIELD.md.
- [ ] **Q-018:** celelalte 2 interviuri cu navetiști (inițiale și rol) → FIELD.md.
- [ ] **FIELD.md:** secțiunea „De ce aceste linii și stații”.
- [ ] **Q-022:** cum spun paginile reale că aplicația nu e oficială Tursib (la mock-ul și planul hărții).

## 3. Design (mock confirmat înainte de build, DESIGN.md)
- [ ] `/plan` pentru **D-042**: tokens noi în DESIGN.md și `tokens.css` (culorile „rece”,
      `--color-time`, umbrele, `--ease-out`), umbrele și mișcarea în `app.css`, screenshot-uri noi;
      DESIGN.md trimite încă la D-027.
- [ ] Mock-uri lipsă: DS-autobuze-acasa, DS-autobuze-linie, DS-autobuze-raport-abatere,
      DS-autobuze-inregistrare, DS-autobuze-admin-import, DS-autobuze-admin-diferente,
      DS-autobuze-admin-rapoarte, DS-autobuze-eroare.
- [ ] Comutatorul vizitator / logat în bara fiecărui mock (pas notat în registry.yaml, înainte de
      build-ul ecranelor respective).
- [ ] Componente fără mock propriu: DS-autobuze-eticheta-linie, -rand-plecare, -harta-leaflet,
      -cautare-statii, -abatere-medie (desenate deja în mock-urile ecranelor; de confirmat la plan).

## 4. Funcții (fiecare: /plan → /build) — ordine propusă
- [ ] **Importul GTFS** (D-003, D-019, D-038): toate fișierele; versiune nouă doar la checksum nou;
      tranzacție atomică; 2 versiuni păstrate; raportul de diferențe; endpoint de admin cu token,
      cron zilnic din GitHub Actions cu interogarea stării la fiecare minut; COPY în flux;
      măsurarea memoriei pe Render (rezervă: importul în GitHub Actions) și a mărimii bazei (1 GB);
      problemele găsite → DATA.md.
- [ ] **Mini-feed-ul de test** al autorului: calendar cu excepții, curse după miezul nopții (25:10:00),
      transfer ≤ 150 m.
- [ ] **Tipuri de zi și sărbători** (D-014, D-015): tabelul de sărbători 2026–2027 prin Flyway.
- [ ] **Stațiile și harta** (D-005, D-022, D-030, D-031, D-034): API JSON, Leaflet + OSM, căutare după
      nume, „aproape de mine”; înlocuiește pagina „hello” de la „/” (se scoate `noindex`).
- [ ] **Pagina de stație** (D-018, D-028, D-036, D-041): următoarele 3 plecări pe linie și sens,
      completate cu „mâine”, curse ≥ 24:00, starea de sărbătoare.
- [ ] **Autentificarea și înregistrarea** (D-011, D-032): email + parolă BCrypt (≤ 72 de octeți),
      roluri USER/ADMIN, primul admin din variabile de mediu, întoarcerea pe pagina de origine.
- [ ] **Favorite și „Acasă”** (D-020, D-022).
- [ ] **Rapoartele de abatere** (D-009, D-017, D-028, D-036): formularul cu plecările din ultimele 60 min.
- [ ] **Pagina de linie** (D-014, D-016, D-035): traseul (Q-010), stațiile, orarul pe tipuri de zi,
      media abaterilor pe ore, ultimele 20 de rapoarte cu sursa.
- [ ] **Seed-ul de teren** (D-021, D-024, D-025): cele 20 de observații ca rapoarte cu sursa „teren”.
- [ ] **Planificatorul** (D-002, D-012, D-013, D-033): maxim un transfer, primele 3 după sosire,
      data și ora, continuarea cu ziua următoare, precalculul și invalidarea lui.
- [ ] **Adminul** (D-003, D-016, D-019): import la cerere, versiuni, raportul de diferențe,
      ștergerea rapoartelor greșite.
- [ ] **Pagina de eroare** pentru 403, 404 și 500 (DS-autobuze-eroare).

## 5. Cerințe tehnice de măsurat și documentat (SCOPE.md)
- [ ] Importul a 200.000+ rânduri: minute, nu ore (batch/COPY, indecși expliciți); durata notată.
- [ ] „Următoarele plecări” sub 300 ms pe feed-ul real, măsurat și documentat.
- [ ] Planificatorul: o rută cu transfer între 2 stații reale în sub 1 secundă, fără scanarea tuturor
      curselor la fiecare cerere.
- [ ] Harta Leaflet citește din API-ul aplicației, nu din fișiere statice.

## 6. Definition of done (SCOPE.md)
- [ ] Feed-ul real importat pe live; o stație reală arată plecările corecte acum, verificat cu panoul.
- [ ] Cursele de după miezul nopții și excepțiile de calendar corecte (teste pe mini-feed).
- [ ] Planificatorul: rută cu transfer între 2 stații reale în sub 1 secundă.
- [ ] Re-importul produce un raport de diferențe.
- [ ] FIELD.md are cele 20 de observații și ele sunt vizibile ca abateri în aplicație.
- [ ] La fiecare ecran: `./mvnw verify` verde, screenshot-uri light și dark la 360 și 1280 px (D-029).

## 7. Pregătirea susținerii (SCOPE.md)
- [ ] „Următoarele plecări” la 00:30 într-o zi de luni care e sărbătoare legală.
- [ ] Ce e precalculat pentru planificator și când se invalidează.
- [ ] Cum s-au importat 200.000 de rânduri și cât a durat.

## 8. Mici, de reținut
- [ ] PATTERN-001 (testele UI în imaginea Playwright) e `candidate`: de promovat sau de renunțat.
- [ ] Release: branch-uri, versiune și changelog sunt încă nestabilite (AGENTS.md §5).
