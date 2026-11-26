# Decisions — stack, build, găzduire, autentificare

## D-001 · Stack: Java 21 + Spring Boot, PostgreSQL/PostGIS, Flyway, Thymeleaf + Leaflet; dezvoltare locală în Docker Compose
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Backend Java 21 + Spring Boot, expus ca REST API. Date în PostgreSQL cu PostGIS; schema doar prin migrări Flyway. Paginile sunt Thymeleaf; harta e Leaflet și își ia datele din API-ul aplicației. Dezvoltarea locală rulează în Docker Compose.
- **Why:** Alegerea utilizatorului la setup. PostGIS acoperă căutarea spațială a stațiilor; Flyway ține schema versionată.
- **Rejected:** React/Vite, Laravel, Python (FastAPI) — oferite la setup, neselectate.
- **Evidence:** cerința utilizatorului la setup, 2026-11-18 (nu există cod încă)
- **Source:** user · confidence high
- **Owner note:** 2026-11-18: reformulat din „totul rulează local” după D-007 (găzduire publică).
- **Confirmed:** 2026-11-25

## D-006 · Build cu Maven (./mvnw); teste cu JUnit 5 și Testcontainers PostGIS
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Build-ul folosește Maven prin wrapper-ul `./mvnw`. Testele sunt JUnit 5; testele cu baza de date rulează pe PostgreSQL/PostGIS prin Testcontainers.
- **Why:** Alegerea utilizatorului. Testcontainers testează pe același PostGIS ca în producție, nu pe o bază în memorie.
- **Rejected:** Gradle.
- **Evidence:** răspunsul utilizatorului la Q-006, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-007 · Aplicația e găzduită pe un URL public, cu HTTPS
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Aplicația se publică pe un URL public, doar prin HTTPS. Furnizorul de găzduire se alege mai târziu (Q-012).
- **Why:** Cerință din catalog.
- **Rejected:** rulare doar locală.
- **Evidence:** răspunsul utilizatorului la Q-008, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-011 · Autentificare cu Spring Security: email + parolă BCrypt, sesiune, roluri USER și ADMIN
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Înregistrare cu email și parolă (hash BCrypt) și login cu sesiune, prin Spring Security. Două roluri: USER (oricine se înregistrează) și ADMIN. Nimeni nu devine admin din aplicație. Primul admin se creează la pornire din variabile de mediu (`.env`), niciodată din cod.
- **Why:** Răspunsul utilizatorului la Q-014. Rapoartele cer utilizatori logați, iar moderarea cere un admin (D-009). Credențialele adminului nu ajung în cod sau în git.
- **Rejected:** promovarea la admin din aplicație; un admin scris în cod sau într-o migrare.
- **Evidence:** răspunsul utilizatorului la Q-014, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-26

## D-029 · Screenshot-uri cu Playwright headless
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Screenshot-urile ecranelor (DESIGN.md, pasul 5) se fac cu Playwright headless (Chromium), pe aplicația pornită în Docker Compose și pe mock-urile din `docs/design/mocks/`. Fiecare ecran schimbat: light și dark, la 360 px și la 1280 px lățime, salvate în `docs/design/screens/` ca `<DS-id>.<light|dark>.<360|1280>.png`. Varianta de Playwright (pentru Java, ca dependență de test în Maven, sau Node) se alege în /plan.
- **Why:** Răspunsul utilizatorului la Q-007. Definition of done cere screenshot-uri light și dark; nu există altă unealtă în repo.
- **Evidence:** răspunsul utilizatorului la Q-007, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-26

## D-032 · Autentificare și înregistrare: întoarcere pe pagina de origine; email unic, parolă de minim 8 caractere și maxim 72 de octeți
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Completează D-011. După autentificare, utilizatorul ajunge înapoi pe pagina de unde a venit; dacă nu există una, pe „Acasă”. La înregistrare: emailul e unic, fără diferență între litere mari și mici; parola are minim 8 caractere și maxim 72 de octeți UTF-8 (cât folosește BCrypt), se introduce de două ori, iar limita de octeți se verifică pe server, cu mesajul „Parola e prea lungă”. Emailul nu se verifică (în afara scopului).
- **Why:** Răspunsul utilizatorului. BCrypt folosește doar primii 72 de octeți; o parolă cu diacritice poate depăși 72 de octeți sub 72 de caractere.
- **Evidence:** răspunsul utilizatorului la recapitularea din 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-26

## D-037 · Găzduire: Render (aplicația din Dockerfile) + Neon (PostgreSQL cu PostGIS)
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Completează D-007. Aplicația rulează pe Render, ca web service construit din `Dockerfile`; baza de date e pe Neon, PostgreSQL cu extensia PostGIS. Riscurile planurilor gratuite (jobul zilnic de import D-019 nu rulează cât serviciul doarme; RAM-ul mic la importul a 200.000+ rânduri; limita de 1 GB pentru două versiuni de feed) se rezolvă în primul /plan.
- **Why:** Răspunsul utilizatorului la Q-012; limitele verificate pe 2026-11-19 sunt în Q-012.
- **Evidence:** răspunsul utilizatorului la Q-012, 2026-11-19; render.com/docs/free; neon.com/docs/introduction/free-tier
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-040 · Conexiunea la Neon verifică certificatul serverului: sslmode=verify-full
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Completează D-037. Adresa JDBC spre Neon (`SPRING_DATASOURCE_URL` din Render) folosește `sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory`: conexiunea e criptată și certificatul Neon e verificat față de autoritățile de încredere ale JVM-ului, inclusiv numele serverului.
- **Why:** Review-ul de securitate al PR-ului #1: cu `sslmode=require`, driverul PostgreSQL criptează conexiunea, dar nu verifică certificatul, deci cineva aflat pe drumul rețelei poate intercepta conexiunea și credențialele bazei.
- **Rejected:** `sslmode=require` (varianta din planul scheletului).
- **Evidence:** review-ul de securitate al PR-ului #1, 2026-11-19; documentația driverului PostgreSQL JDBC (sslmode)
- **Source:** review · confidence medium
- **Confirmed:** 2026-11-24

## D-046 · CSV-ul din feed se citește cu Apache Commons CSV
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Fișierele `.txt` din feed-ul GTFS se citesc cu Apache Commons CSV (`org.apache.commons:commons-csv`), în flux, cu antetul din primul rând. Codul nostru face doar maparea coloanelor GTFS spre coloanele din DB și conversia valorilor (orele după D-043, datele `YYYYMMDD`); testele acoperă maparea noastră, nu biblioteca.
- **Why:** Decizia utilizatorului la /plan pentru importul GTFS: mai puțin cod de întreținut și de explicat decât un cititor CSV scris de mână (ghilimele, BOM, CRLF).
- **Rejected:** cititorul CSV scris de mână (propus în planul importului).
- **Evidence:** răspunsul utilizatorului la /plan pentru importul GTFS, 2026-11-25
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-053 · Leaflet se servește din fișiere copiate în `static/vendor/leaflet/`
- **Status:** approved
- **Date:** 2026-11-26
- **Rule:** Completează D-001. Leaflet 1.9.4 (`leaflet.js`, `leaflet.css`, `images/`) e copiat în `src/main/resources/static/vendor/leaflet/`, cu licența BSD-2 (`LICENSE`) alături, și se servește de aplicație. Fără CDN la rulare și fără dependență Maven (WebJar). O versiune nouă se copiază de mână.
- **Why:** Alegerea utilizatorului la /plan pentru punctul 9: fără terți la rulare și fără dependență Maven în plus.
- **Rejected:** WebJar Maven (`org.webjars.npm:leaflet`); CDN (unpkg / jsdelivr).
- **Evidence:** răspunsul utilizatorului la /plan pentru punctul 9, 2026-11-26
- **Source:** user · confidence high
- **Confirmed:** 2026-11-26

## D-055 · Primul admin: la fiecare pornire, contul cu `ADMIN_EMAIL` primește rolul ADMIN și parola din `ADMIN_PASSWORD`
- **Status:** approved
- **Date:** 2026-11-26
- **Rule:** Completează D-011. La fiecare pornire, dacă `ADMIN_EMAIL` și `ADMIN_PASSWORD` sunt setate, contul cu acel email (fără diferență de majuscule) primește rolul ADMIN și parola din `ADMIN_PASSWORD` (hash BCrypt); dacă nu există, se creează. Fără variabile, nu se creează niciun admin. O parolă care nu respectă D-032 oprește pornirea, cu un mesaj fără parolă. Parola nu se scrie niciodată în log.
- **Why:** Înregistrarea e publică: dacă contul s-ar crea doar când lipsește, cine se înregistrează primul cu emailul adminului ar deveni admin cu parola lui. Rescrierea la pornire îi ia contul.
- **Rejected:** contul creat doar dacă lipsește (rolul ADMIN dat unui cont existent cu parola lui; sau contul existent lăsat neatins).
- **Evidence:** răspunsul utilizatorului la /plan pentru punctul 10, 2026-11-26
- **Source:** user · confidence high
- **Confirmed:** 2026-11-26

## D-058 · Rutele: public totul, în afară de `/favorite/**` (login) și `/admin/**` (ADMIN); `/admin/import` doar cu token
- **Status:** approved
- **Date:** 2026-11-26
- **Rule:** Completează D-011. Paginile, API-ul JSON și fișierele statice sunt publice. `/favorite/**` cere un utilizator logat (USER sau ADMIN); `/admin/**` cere rolul ADMIN. Excepție: `/admin/import` (C-001) rămâne protejat doar cu tokenul `IMPORT_TOKEN`, fără sesiune și fără CSRF, pentru workflow-ul din GitHub Actions. Formularele POST au token CSRF; ieșirea e POST pe `/iesire`.
- **Why:** Răspunsul utilizatorului la /plan pentru punctul 10: C-001 nu se schimbă.
- **Rejected:** `/admin/import` cu token sau sesiune ADMIN (schimbarea lui C-001).
- **Evidence:** răspunsul utilizatorului la /plan pentru punctul 10, 2026-11-26
- **Source:** user · confidence high
- **Confirmed:** 2026-11-26
