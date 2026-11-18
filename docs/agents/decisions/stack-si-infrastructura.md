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

## D-011 · Autentificare cu Spring Security: email + parolă BCrypt, sesiune, roluri USER și ADMIN
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Înregistrare cu email și parolă (hash BCrypt) și login cu sesiune, prin Spring Security. Două roluri: USER (oricine se înregistrează) și ADMIN. Nimeni nu devine admin din aplicație. Primul admin se creează la pornire din variabile de mediu (`.env`), niciodată din cod.
- **Why:** Răspunsul utilizatorului la Q-014. Rapoartele cer utilizatori logați, iar moderarea cere un admin (D-009). Credențialele adminului nu ajung în cod sau în git.
- **Rejected:** promovarea la admin din aplicație; un admin scris în cod sau într-o migrare.
- **Evidence:** răspunsul utilizatorului la Q-014, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18
