# Open questions — hartă, build, screenshot-uri, găzduire, autentificare

## Q-004 · Ce furnizor de tile-uri folosește harta?
- **Status:** answered 2026-11-18
- **Blocks:** configurarea Leaflet, atribuirea
- **Who answers:** utilizatorul
- **Answer:** Tile-uri OpenStreetMap, cu atribuirea „© OpenStreetMap contributors”.
- **Recorded as:** D-005

## Q-006 · Maven sau Gradle, și cu ce rulăm testele?
- **Status:** answered 2026-11-18
- **Blocks:** comenzile din AGENTS.md secțiunea 2 și definition of done
- **Who answers:** utilizatorul
- **Answer:** Maven prin `./mvnw`, JUnit 5 și Testcontainers cu PostgreSQL/PostGIS.
- **Recorded as:** D-006

## Q-007 · Cum facem screenshot-uri ale paginilor?
- **Status:** answered 2026-11-19
- **Blocks:** pasul de screenshot din /build și DESIGN.md (de ex. Playwright headless pe aplicația din Docker Compose)
- **Who answers:** utilizatorul, la interviu și la /plan
- **Answer:** Playwright headless.
- **Recorded as:** D-029 (proposed)

## Q-008 · Aplicația rămâne doar locală sau va fi găzduită?
- **Status:** answered 2026-11-18
- **Blocks:** AGENTS.md secțiunea 5 (release), configurare și secrete
- **Who answers:** utilizatorul
- **Answer:** Găzduită pe un URL public cu HTTPS (cerință din catalog). Furnizorul se alege mai târziu (Q-012).
- **Recorded as:** D-007

## Q-012 · Pe ce furnizor găzduim aplicația?
- **Status:** answered 2026-11-19
- **Blocks:** deploy, HTTPS, configurare și secrete (D-007). Termen: înainte de checkpoint-ul de duminică, 2026-11-22, când trebuie să existe deja un deploy „hello” pe URL-ul live.
- **Who answers:** utilizatorul, la /plan (după interviu și design)
- **Proposed answer:** Render (web service din Dockerfile) + Neon (PostgreSQL cu PostGIS), propus de utilizator pe 2026-11-19; alegerea se face în primul /plan. Limite verificate 2026-11-19:
  - Render, plan gratuit (render.com/docs/free): serviciul adoarme după 15 min fără trafic și pornește în ~1 min; 750 de ore de instanță pe lună per workspace; sistem de fișiere efemer (se pierde la redeploy, restart sau adormire); build din Dockerfile suportat (render.com/docs/web-services). RAM/CPU nu apar în documentația oficială; surse terțe spun 512 MB și 0,1 CPU (neverificat).
  - Neon, plan gratuit (neon.com/docs/introduction/free-tier, actualizat 2026-11-12): 1 GB stocare per proiect; 100 CU-ore de calcul per proiect pe lună; baza adoarme după 5 min (nu se poate opri); 5 GB transfer pe lună; istoric de restaurare 6 h. PostGIS se activează cu `CREATE EXTENSION postgis` (neon.com/docs/extensions/postgis); pagina nu spune nimic despre restricții pe planul gratuit.
  - Riscuri de discutat în /plan: jobul zilnic de import (D-019) nu rulează cât timp serviciul Render doarme; importul a 200.000+ rânduri pe 0,1 CPU și 512 MB poate fi lent; două versiuni de feed (D-019) trebuie să încapă în 1 GB; prima cerere după adormire durează ~1 min (cerința de 300 ms e pentru serviciul treaz).
- **Answer:** Render (web service din Dockerfile) + Neon (PostgreSQL cu PostGIS). Riscurile (jobul zilnic cât serviciul doarme, RAM-ul mic la import, limita de 1 GB) se rezolvă în primul /plan.
- **Recorded as:** D-037 (proposed)

## Q-014 · Cum se autentifică utilizatorii și cum devine cineva admin?
- **Status:** answered 2026-11-18
- **Blocks:** rapoartele de abatere (D-009): înregistrare și login, roluri utilizator / admin
- **Who answers:** utilizatorul
- **Answer:** Email + parolă (BCrypt), login cu sesiune, prin Spring Security. Roluri USER (la înregistrare) și ADMIN. Primul admin se creează la pornire din variabile de mediu (`.env`); nimeni nu devine admin din aplicație.
- **Recorded as:** D-011
