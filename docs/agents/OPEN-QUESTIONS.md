# Open questions — p07-autobuze

<!-- graffino-agentic:begin v1 -->
> Unknowns. Work that depends on an open question is out of scope until it is
> answered. Only the user answers; an agent never answers by guessing. An agent may
> write a `Proposed answer` the evidence supports; the question stays
> `investigating` until the user confirms it.
> Answered entries stay, with the answer and date, and usually become a D- or PATTERN-.
<!-- graffino-agentic:end -->

## Index
- Q-001 · Ce oraș și ce feed GTFS (URL, licență, conține shapes.txt)? · _answered_
- Q-002 · Ce face un import nou cu datele existente? · _answered_
- Q-003 · Următoarele plecări vin doar din orar (static) sau și din GTFS-Realtime? · _answered_
- Q-004 · Ce furnizor de tile-uri folosește harta? · _answered_
- Q-005 · După ce criterii alege planificatorul și cum definim un transfer?
- Q-006 · Maven sau Gradle, și cu ce rulăm testele? · _answered_
- Q-007 · Cum facem screenshot-uri ale paginilor?
- Q-008 · Aplicația rămâne doar locală sau va fi găzduită? · _answered_
- Q-009 · Confirmă Tursib folosirea datelor GTFS în acest proiect?
- Q-010 · Feed-ul Tursib conține shapes.txt?
- Q-011 · Rapoartele utilizatorilor despre abateri sunt o funcție a aplicației? · _answered_
- Q-012 · Pe ce furnizor găzduim aplicația?
- Q-013 · Unde e FIELD.md și ce format au observațiile de teren?
- Q-014 · Cum se autentifică utilizatorii și cum devine cineva admin?

---

## Q-001 · Ce oraș și ce feed GTFS (URL, licență, conține shapes.txt)?
- **Status:** answered 2026-11-18
- **Blocks:** importul, datele de test, vizualizarea traseelor
- **Who answers:** utilizatorul
- **Answer:** Sibiu, operator Tursib S.A. Feed oficial: https://tursib.ro/trasee/gtfs ; copie de rezervă: https://mobilitydatabase.org/feeds/gtfs/mdb-2099 . Licența: vezi DATA.md și Q-009. Dacă există `shapes.txt`: Q-010.
- **Recorded as:** DATA.md, CONTRACTS.md (External)

## Q-002 · Ce face un import nou cu datele existente?
- **Status:** answered 2026-11-18
- **Blocks:** designul importului
- **Who answers:** utilizatorul
- **Answer:** Re-importul creează o versiune nouă a feed-ului, într-o singură tranzacție (tot sau nimic), și produce raportul de diferențe cerut de catalog (stații noi / dispărute, trasee schimbate).
- **Recorded as:** D-003 (proposed)

## Q-003 · Următoarele plecări vin doar din orar (static) sau și din GTFS-Realtime?
- **Status:** answered 2026-11-18
- **Blocks:** sursa pentru următoarele plecări
- **Who answers:** utilizatorul
- **Answer:** Doar orarul static GTFS; GTFS-Realtime e în afara scopului. Abaterile vin din rapoartele utilizatorilor și din observațiile de teren ale autorului.
- **Recorded as:** D-004 (proposed)

## Q-004 · Ce furnizor de tile-uri folosește harta?
- **Status:** answered 2026-11-18
- **Blocks:** configurarea Leaflet, atribuirea
- **Who answers:** utilizatorul
- **Answer:** Tile-uri OpenStreetMap, cu atribuirea „© OpenStreetMap contributors”.
- **Recorded as:** D-005 (proposed)

## Q-005 · După ce criterii alege planificatorul și cum definim un transfer?
- **Status:** open
- **Blocks:** planificatorul: criteriul (sosire cea mai devreme? cel mai puțin mers pe jos?), timpul minim de transfer, transfer doar în aceeași stație sau și între stații apropiate (la ce distanță)
- **Who answers:** utilizatorul, la interviu și la /plan

## Q-006 · Maven sau Gradle, și cu ce rulăm testele?
- **Status:** answered 2026-11-18
- **Blocks:** comenzile din AGENTS.md secțiunea 2 și definition of done
- **Who answers:** utilizatorul
- **Answer:** Maven prin `./mvnw`, JUnit 5 și Testcontainers cu PostgreSQL/PostGIS.
- **Recorded as:** D-006 (proposed)

## Q-007 · Cum facem screenshot-uri ale paginilor?
- **Status:** open
- **Blocks:** pasul de screenshot din /build și DESIGN.md (de ex. Playwright headless pe aplicația din Docker Compose)
- **Who answers:** utilizatorul, la interviu și la /plan

## Q-008 · Aplicația rămâne doar locală sau va fi găzduită?
- **Status:** answered 2026-11-18
- **Blocks:** AGENTS.md secțiunea 5 (release), configurare și secrete
- **Who answers:** utilizatorul
- **Answer:** Găzduită pe un URL public cu HTTPS (cerință din catalog). Furnizorul se alege mai târziu (Q-012).
- **Recorded as:** D-007 (proposed)

## Q-009 · Confirmă Tursib folosirea datelor GTFS în acest proiect?
- **Status:** open
- **Blocks:** statutul licenței în DATA.md
- **Who answers:** utilizatorul, după răspunsul Tursib la cererea către office@tursib.ro (data trimiterii: de completat)

## Q-010 · Feed-ul Tursib conține shapes.txt?
- **Status:** open
- **Blocks:** vizualizarea traseelor: forma reală de pe hartă sau linii drepte între stații
- **Who answers:** utilizatorul, la descărcarea feed-ului

## Q-011 · Rapoartele utilizatorilor despre abateri sunt o funcție a aplicației?
- **Status:** answered 2026-11-18
- **Blocks:** orice funcție de raportare a abaterilor (D-004 spune doar că abaterile vin din rapoarte și observații de teren)
- **Who answers:** utilizatorul
- **Answer:** Da, cerută de catalog: utilizatorii logați raportează o abatere (linie, stație, oră); pagina liniei arată abaterea medie pe intervale orare; adminul moderează; observațiile din FIELD.md intră ca seed.
- **Recorded as:** D-009 (proposed)

## Q-012 · Pe ce furnizor găzduim aplicația?
- **Status:** open
- **Blocks:** deploy, HTTPS, configurare și secrete (D-007). Termen: înainte de checkpoint-ul de duminică, 2026-11-22, când trebuie să existe deja un deploy „hello” pe URL-ul live.
- **Who answers:** utilizatorul

## Q-013 · Unde e FIELD.md și ce format au observațiile de teren?
- **Status:** open
- **Blocks:** seed-ul observațiilor de teren în rapoartele de abatere (D-009); FIELD.md nu există încă în repo
- **Who answers:** utilizatorul

## Q-014 · Cum se autentifică utilizatorii și cum devine cineva admin?
- **Status:** open
- **Blocks:** rapoartele de abatere (D-009): înregistrare și login, roluri utilizator / admin
- **Who answers:** utilizatorul
