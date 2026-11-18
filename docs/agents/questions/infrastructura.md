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
- **Status:** open
- **Blocks:** pasul de screenshot din /build și DESIGN.md (de ex. Playwright headless pe aplicația din Docker Compose)
- **Who answers:** utilizatorul, la interviu și la /plan

## Q-008 · Aplicația rămâne doar locală sau va fi găzduită?
- **Status:** answered 2026-11-18
- **Blocks:** AGENTS.md secțiunea 5 (release), configurare și secrete
- **Who answers:** utilizatorul
- **Answer:** Găzduită pe un URL public cu HTTPS (cerință din catalog). Furnizorul se alege mai târziu (Q-012).
- **Recorded as:** D-007

## Q-012 · Pe ce furnizor găzduim aplicația?
- **Status:** open
- **Blocks:** deploy, HTTPS, configurare și secrete (D-007). Termen: înainte de checkpoint-ul de duminică, 2026-11-22, când trebuie să existe deja un deploy „hello” pe URL-ul live.
- **Who answers:** utilizatorul, la /plan (după interviu și design)

## Q-014 · Cum se autentifică utilizatorii și cum devine cineva admin?
- **Status:** answered 2026-11-18
- **Blocks:** rapoartele de abatere (D-009): înregistrare și login, roluri utilizator / admin
- **Who answers:** utilizatorul
- **Answer:** Email + parolă (BCrypt), login cu sesiune, prin Spring Security. Roluri USER (la înregistrare) și ADMIN. Primul admin se creează la pornire din variabile de mediu (`.env`); nimeni nu devine admin din aplicație.
- **Recorded as:** D-011
