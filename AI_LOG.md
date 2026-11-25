# AI_LOG — p07-autobuze

> Jurnalul folosirii AI în proiect: pentru fiecare sesiune, ce am cerut, ce am primit
> și ce am făcut cu răspunsul. Cele mai noi intrări sunt primele.

## 2026-11-25 · Twist 1+2: serviciile active și orele ≥ 24:00 (punctul 7), D-047
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`, pe branch-ul
`feat/twist-calendar`.

**Ce am cerut**
- `/plan` pentru punctul 7 din ROADMAP.

**Ce am primit**
- Planul: serviciile active la o dată (calendar ∪ adăugate − scoase, într-un singur SQL), plecările
  dintr-o listă de stații într-un interval, căutate în toate zilele de serviciu care îl ating,
  sărbătorile 2026–2027 în V3 și tipul de zi. Granița cu punctul 8 (pagina, „primele 3”, 300 ms) explicită.
- Lista sărbătorilor, cu Paștele ortodox calculat: 16 date în 2026, 17 în 2027.
- Build-ul: testele întâi (picate pe cod lipsă), apoi codul; `./mvnw verify` cu 59 de teste trecute.
  O întrebare nouă pentru punctul 8: Q-024 (cursele care se termină în stație).

**Ce am făcut cu răspunsul**
- Am aprobat planul și cele 4 presupuneri; am verificat eu lista sărbătorilor.
- Am confirmat că orele din feed se citesc ca oră de pe ceas, ca pe panou → D-047.
- **Am adăugat în mini-feed serviciul `DUM`** (doar duminica), cu o cursă de noapte din `S_NOAPTE`
  la 24:35:00, și testul care răspunde la întrebarea din fișă: „următoarele plecări la 00:30 într-o
  zi de luni, sărbătoare legală”. Luni 30.11.2026, 00:30: pleacă doar cursa de duminică de la 00:35;
  nimic din serviciul luni–vineri, scos pe 30.11.
- La Q-024 (punctul 8) am răspuns: „Următoarele plecări” nu arată cursele în stația în care se termină,
  ca la panoul din stație → D-048. Am aprobat merge-ul în `main` și push-ul.
- Deploy manual pe Render al commit-ului `fd5bfbe` (făcut de mine). În log: „Migrating schema "public"
  to version "3 - public holidays"” și „Successfully applied 1 migration ... now at version v3”
  (0,354 s); aplicația a pornit în 79 s. Numărul de rânduri din `public_holidays` pe Neon nu l-am
  verificat separat cu SQL.

## 2026-11-25 · Primul import real, pe live și local (punctul 6)
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`, pe `main`
(doar documentație; deploy-ul, variabilele și workflow-ul le-am făcut eu).

**Ce am făcut eu**
- Deploy pe Render al commit-ului `d2ea174`; `IMPORT_TOKEN` pe Render și ca secret în GitHub; am
  pornit manual workflow-ul „Import GTFS”.
- **Greșeala:** prima încercare a picat cu 404. Pusesem `IMPORT_TOKEN` pe Render la „Secret Files”
  în loc de „Environment Variables”. Aplicația nu vedea variabila, deci endpoint-ul răspundea 404,
  cum cere C-001 când tokenul nu e configurat. **Corectura:** am mutat tokenul la Environment
  Variables, am redat deploy-ul, iar a doua rulare a mers: `POST` 202, `DONE` după primul minut.

**Ce am cerut**
- Durata, rândurile pe fișier și memoria în DATA.md; precizarea că feed-ul are 183.887 de rânduri
  (93.218 în `stop_times`), sub pragul de 200.000 din catalog; punctul 6 bifat.

**Ce am primit**
- Cifrele de pe live luate din capturile mele: 22,8 s, versiunea 1, rândurile pe fișier. Agentul a
  observat că lăsasem necompletate memoria și verificarea pe Neon. Memoria pe Render nu se vede pe
  planul gratuit, așa că în DATA.md apare ca nemăsurată.
- Importul local, rulat de agent în Docker Compose cu un token temporar ținut doar în shell (nescris
  în fișiere): `DONE` în 5,9 s, același checksum, rândurile verificate cu SQL, maxim ~332 MiB pentru
  container.

## 2026-11-25 · Importul GTFS (punctul 5), D-046, C-001
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`, pe branch-ul
`feat/import-gtfs`.

**Ce am cerut**
- `/plan` pentru punctul 5: descărcarea feed-ului printr-un job, cu versiune și checksum; insert în
  batch, nu rând cu rând; totul într-o tranzacție; indecși expliciți; mini-feed de test construit de
  noi. Doar importul: fără raportul de diferențe, fără pagini și fără plecări.

**Ce am primit**
- Planul: descărcare + SHA-256, o tranzacție, câte un `COPY` pe fișier (D-038), endpoint cu token și
  stare (C-001), workflow cu cron, test că fiecare cheie străină are index; un cititor CSV scris de mână.
- Înainte de build, analiza arhivei reale (`tursib.gtfs_2026-10-v4.9.zip`): are `shapes.txt`
  (84.954 de puncte), `stop_times.txt` are 93.218 rânduri (183.887 în total), `stop_code` lipsește
  la toate cele 512 stații, iar `parent_station` e gol.
- Build-ul: testele întâi (16 unitare picate pe cod lipsă), apoi codul; `./mvnw verify` cu 42 de teste
  trecute. Două reguli stricate intenționat (păstrarea a 2 versiuni, verificarea checksum-ului) au
  fost prinse de teste.

**Ce am făcut cu răspunsul**
- Am aprobat planul, C-001 și presupunerile (pornire prin endpoint + cron; același checksum notat
  doar în stare și în log, fără migrare).
- Am înlocuit cititorul CSV scris de mână cu Apache Commons CSV (D-046): mai puțin cod de întreținut
  și de explicat. Testul cititorului a devenit un test pe maparea noastră.
- **Scenariile mini-feed-ului sunt ale mele:** a) serviciu luni–vineri scos pe 01.12.2026; b) serviciu
  doar în `calendar_dates`; c) cursă 23:50 → 25:10:00; d) două stații la ~100 m pe linii diferite;
  e) o stație la peste 150 m, care nu e transfer; f) un `stop_name` cu virgulă. Agentul a scris
  fișierele după ele; le-am verificat rând cu rând înainte de commit și am cerut două ajustări:
  o stație intermediară pe cursa de noapte (`S_NOAPTE`, plecare la 24:40:00 dintr-o stație care nu
  e capăt) și 30.11.2026 (Sf. Andrei) scos din serviciul luni–vineri.
- Am cerut închiderea Q-010 după verificarea arhivei. Pentru Q-019 agentul a propus `stop_id`;
  am confirmat și am închis-o. Am aprobat D-046 și cele două abateri de la plan (driverul PostgreSQL
  la compilare, `IMPORT_TOKEN` în `docker-compose.yml`). PATTERN-002 e marcat outdated: gazda are
  acum JDK 21.

## 2026-11-24 · V2 pe Neon (punctul 2 încheiat), D-045
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`, pe `main`
(doar documentație; deploy-ul l-am făcut eu, din consola Render).

**Ce am făcut eu**
- Deploy manual pe Render al commit-ului `c863401`. În log: „Migrating schema "public" to version
  "2 - schema"” și „Successfully applied 1 migration ... now at version v2” (1,1 s).
- În Neon (SQL Editor, `main_db`): `flyway_schema_history` are 1 `init` și 2 `schema`, ambele
  `success = t`; PostGIS 3.5.7. Agentul nu are acces la Render sau Neon, deci aceste date vin de la mine.

**Ce am cerut**
- Verificarea paginii pe HTTPS, punctul 2 bifat, intrarea asta, commit și push; ștergerea
  branch-ului local `feat/schema-db`. Am aprobat D-045 (raportul șters de admin: DELETE definitiv).

**Ce am primit**
- Verificarea: https://autobuze.onrender.com răspunde 200 pe HTTPS după deploy; HTTP face 301.
- D-045 aprobată; Q-023 trimite la ea. Punctul 2 bifat în TODO.md, cu dovada de pe Neon.
- Semnalarea că rândul meu despre diagramă a rămas necompletat (am lipit textul cu ambele
  variante), deci diagrama nu era încă confirmată.

**Ce am făcut cu răspunsul**
- Am verificat pe GitHub: diagrama Mermaid din `docs/schema.md` se vede corect.

## 2026-11-24 · Schema DB: V2, diagrama, D-043 și D-044 (punctul 2)
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`, pe branch-ul
`feat/schema-db`, apoi pe `main`.

**Ce am cerut**
- `/plan` pentru punctul 2: tabelele GTFS legate de o versiune de feed, plus utilizatori,
  favorite și rapoarte de abatere; o migrare Flyway nouă (V1 rămâne), PostGIS activat în migrare
  și pentru Neon, diagramă Mermaid în `docs/schema.md`, test că migrarea se aplică. Doar schema.

**Ce am primit**
- Patru întrebări înainte de plan: identificatorul stației cât timp Q-019 e deschisă, cum se țin
  orele ≥ 24:00, ștergerea rapoartelor, limba numelor de tabele.
- Planul (12 tabele, 6 teste de schemă) și două propuneri: D-043 (orele în secunde de la începutul
  zilei de serviciu) și D-044 (tabele GTFS pe versiune, datele utilizatorilor fără chei spre GTFS).
- Build-ul: `V2__schema.sql`, `SchemaIT` (scris întâi, picat pe „relation does not exist”, apoi
  verde), `docs/schema.md`; `./mvnw verify` cu 11 teste trecute.
- Testele n-au mers din prima: WSL nu avea JDK, iar Docker Desktop era oprit. După ce l-am pornit,
  `./mvnw` a rulat în imaginea `eclipse-temurin:21-jdk`, cu socket-ul Docker montat pentru
  Testcontainers.

**Ce am făcut cu răspunsul**
- La întrebarea despre ștergerea rapoartelor am răspuns de fapt despre versiunile vechi de feed
  (trebuie să rămână cea anterioară, pentru raportul de diferențe: era deja D-019). Agentul a
  notat întrebarea ca Q-023; am răspuns apoi: ștergere definitivă → D-045, propusă.
- Am cerut corectura „11 tabele” → 12 în plan; am aprobat planul, D-043, D-044, schimbarea lui
  D-024 (partea „DB” trimite la D-043) și cele două presupuneri.
- Am dat acordul pentru merge pe `main` și deploy pe Neon după ce testele trec local.

## 2026-11-24 · Deploy-ul „hello” pe Render + Neon (punctul 1)
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`, pe `main`
(doar documentație; deploy-ul l-am făcut eu, din consolele Render și Neon).

**Ce am făcut eu**
- Proiectul Neon: PostgreSQL 17.12, Frankfurt, baza `main_db`, conexiunea directă.
- Render → Blueprint din repo (`render.yaml`: Docker, Frankfurt, plan gratuit), cu cele trei
  variabile `SPRING_DATASOURCE_*` completate în Render, nu în git.
- **Greșeala:** prima încercare a picat. În `SPRING_DATASOURCE_URL` rămăsese `HOST` din exemplu
  în locul host-ului real de la Neon, deci aplicația nu avea la ce server să se conecteze.
- **Cum am reparat-o:** am corectat variabila în Render, cu host-ul din conexiunea directă Neon,
  și am pornit din nou deploy-ul manual. A doua încercare a mers: în log, conexiunea cu
  `sslmode=verify-full` (D-040), Flyway a aplicat V1, pornire în ~57 s.

**Ce am cerut**
- Verificarea paginii pe HTTPS, URL-ul în README, intrarea asta în AI_LOG, punctul 1 bifat în
  TODO.md, commit și push.

**Ce am primit**
- Verificarea: https://autobuze.onrender.com răspunde 200 pe HTTPS, cu pagina „hello”
  (certificat Google Trust Services, valabil până la 17.02.2027); HTTP face 301 spre HTTPS.
- Întrebarea despre ce nu avea dovadă: regiunea Neon și conexiunea directă (confirmate de mine)
  și memoria pe Render (nemăsurată; serviciul rulează pe planul de 512 MB fără să cadă).
- URL-ul live în README, punctul 1 bifat în TODO.md, cu ce s-a verificat și ce nu.

**Ce am făcut cu răspunsul**
- Am acceptat bifarea cu nota despre memorie și am cerut un singur commit, împreună cu
  `docs/ROADMAP.md` și TODO.md rescris după el.
- De reținut: înainte de deploy, verific că în variabile nu a rămas niciun placeholder din exemplu.

## 2026-11-19 · Design mai viu: variante de culoare, D-042, mock-urile actualizate
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`, pe `main`
(doar documentație).

### 1. Variantele
**Ce am cerut**
- Un design mai viu și mai primitor, fără să schimb organizarea și poziția elementelor: fundal
  și suprafețe nuanțate, un accent mai prietenos, dark fără negru pur; umbre moi, stratificate,
  ca tokens noi; animații line pe butoane, carduri și la comutarea temei, oprite la
  prefers-reduced-motion. Contrast minim 4,5:1 pentru text și 3:1 pentru borduri, pe bg și pe
  surface, în ambele teme; orele de plecare cu contrast maxim și fără animații. 2–3 variante
  una lângă alta, într-un mock separat al paginii de stație; DESIGN.md și tokens.css neatinse.

**Ce am primit**
- Semnalarea că o bază colorată contrazice D-027 („bază neutră”), deci o propunere de înlocuire.
- `DS-autobuze-statie.variante.html`: pagina de acum lângă trei variante (cald, rece, salvie),
  cu contrastul calculat în pagină; toate pragurile trec, orele la ~17:1 cu `--color-time`.
- Două propuneri: D-042 în locul lui D-027 și tokens-urile noi (culori, `--color-time`, patru
  umbre, `--ease-out`, regulile de mișcare).

**Ce am făcut cu răspunsul**
- Am aprobat ambele propuneri și am ales varianta „rece” (cer + azur) → D-042; D-027 înlocuită.

### 2. Mock-urile
**Ce am cerut**
- Cele 5 mock-uri actualizate la D-042, cu valorile scrise direct în ele, pe draft până le
  reconfirm; DESIGN.md și tokens.css rămân până la /plan-ul care aplică D-042.

**Ce am primit**
- Mock-urile stației, hărții, planificatorului, autentificării și antetului, cu culorile
  „rece”, umbrele, mișcarea și orele pe `--color-time`; toate pe `draft` în registry.yaml.
- Două defecte vechi ale mock-urilor, găsite la verificarea capturilor și reparate: în mock-ul
  antetului, navigarea apărea și în varianta „doar numele”, iar în mock-ul de login eroarea
  apărea la formularul gol (`display` anula atributul `hidden`). Aplicația nu era afectată.

**Ce am făcut cu răspunsul**
- Am cerut commit pe `main` (doar documentație), lint, AI_LOG și push, cu comanda arătată înainte.

## 2026-11-19 · Redenumirea aplicației: „Staționar — când vine autobuzul?”
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`, pe branch-ul
`feat/nume-stationar`.

**Ce am cerut**
- Schimbarea numelui aplicației din „Când vine autobuzul?” în „Staționar — când vine autobuzul?”,
  cu numele scurt „Staționar” unde e loc puțin; titlul proiectului din catalog (P07) rămâne.
  Întâi ca PROPOSAL; după aprobare, schimbarea peste tot, teste, push pe branch și PR.

**Ce am primit**
- Propunerea D-041, cu lista celor 13 fișiere în care apare numele, și trei întrebări: unde
  apare numele scurt (numele complet nu încape în antet la 360 px), dacă mock-urile trec pe
  draft și semnalarea că „staționar” înseamnă și „care nu se mișcă”.
- După aprobare: testul schimbat întâi (`HelloPageIT`, la cererea mea) și văzut picând; apoi
  antetul, `<title>`, `<h1>`, cele 5 mock-uri, README.md, SCOPE.md, `pom.xml`; screenshot-uri
  refăcute și verificate; `./mvnw clean verify` (5 teste), 7 teste UI și lint-ul verzi;
  PR-ul #2, cu CI-ul verde.

**Ce am făcut cu răspunsul**
- Am aprobat D-041 și am ales: antetul arată mereu „Staționar”; `<title>`, `<h1>` și README au
  numele complet; mock-urile rămân `approved`, pentru că se schimbă doar textul aprobat;
  numele e un joc de cuvinte voit (stație + „-ar”).
- Am cerut merge-ul PR-ului #2 prin fast-forward și ștergerea branch-ului, cu comenzile
  arătate înainte.

## 2026-11-19 · Review-ul PR-ului #1 și reparațiile
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`, pe branch-ul
`chore/schelet-proiect`.

### 1. Pull request-ul
**Ce am cerut**
- Push pe branch și un pull request spre `main` cu `gh`, cu comenzile arătate înainte.

**Ce am primit**
- Comenzile și descrierea PR-ului; după aprobarea mea, PR-ul #1. Un run CI a picat la
  descărcarea Maven pe runner (eroare de rețea trecătoare); repornit, a trecut.

### 2. Review-ul cu trei sub-agenți
**Ce am cerut**
- Trei sub-agenți în paralel, fără să modifice fișiere: performanță, securitate, SEO și GEO;
  doar probleme care afectează corectitudinea, cu fișier și linie.

**Ce am primit**
- Nicio problemă de prioritate mare. Constatările, verificate în cod: tokenul CI fără limită de
  permisiuni, portul Postgres deschis pe toate interfețele, conexiunile Hikari care țin Neon
  treaz, build-ul Docker care descărca Playwright (196 MB), memoria JVM nelimitată în afara
  heap-ului, `sslmode=require` fără verificarea certificatului, pagina temporară indexabilă.
  Două referințe greșite din rapoarte corectate (o linie inexistentă, Q-012 depășită).

**Ce am făcut cu răspunsul**
- Am cerut reparațiile 1–5, `noindex` doar pe pagina „hello”, cache pentru distribuția Maven în
  CI, Q-022 pentru mențiunea „aplicație neoficială” pe paginile reale și o PROPOSAL pentru
  `sslmode=verify-full`.
- Am primit reparațiile, cu un test nou pentru `noindex` și o abatere explicată: Playwright și
  testele UI mutate în profilul Maven `ui`, pentru că simpla sărire a testelor tot descărca
  Playwright. CI verde pe ambele run-uri.
- Am aprobat D-040 (`sslmode=verify-full` spre Neon).
- Am cerut merge-ul PR-ului în `main` și ștergerea branch-ului, cu comanda arătată înainte.

## 2026-11-19 · /build: scheletul proiectului
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`, pe branch-ul
`chore/schelet-proiect`.

### 1. Mock-ul antetului
**Ce am cerut**
- Rularea `/build` pe planul aprobat pentru scheletul proiectului.

**Ce am primit**
- Întâi mock-ul DS-autobuze-antet cu butonul de temă (D-039), cum cerea planul, și oprirea
  până la confirmare.

**Ce am făcut cu răspunsul**
- L-am deschis în browser și l-am confirmat.

### 2. Construcția
**Ce am cerut**
- Continuarea planului.

**Ce am primit**
- Versiunile alese pe Maven Central, publicate cu cel puțin 2 săptămâni în urmă: Spring Boot
  4.1.2, Maven 3.9.16 prin wrapper-ul oficial 3.3.4, Playwright 1.64.0.
- Testele scrise întâi și văzute picând din motivul corect (404, `tokens.css` lipsă, 0 migrări);
  apoi codul: pagina „hello” cu antetul, textul aprobat și subsolul Tursib, `tokens.css` verificat
  față de DESIGN.md, butonul de temă, Flyway cu V1 goală, `Dockerfile` cu `-Xmx300m`,
  `docker-compose.yml`, `render.yaml`, CI, documentația rescrisă după cod.
- Rezultate: `./mvnw verify` verde (4 teste), 7 teste de UI verzi, 4 screenshot-uri verificate cu
  mock-ul; aplicația pornește în Compose în 2,4 s și folosește ~303 MB.
- O problemă de rețea: Chromium pentru Playwright nu s-a putut descărca de pe Google Storage;
  testele de UI au rulat în imaginea oficială Playwright (PATTERN-001, candidate). Testele de UI
  nu au putut fi văzute picând înainte de cod.
- O abatere de la plan: `autoDeployTrigger: "off"` în locul câmpului depreciat `autoDeploy: false`.

**Ce am făcut cu răspunsul**
- Nu am făcut merge: urmează review-ul din laborator. Am cerut push pe branch și un pull request
  spre `main`, cu comenzile arătate înainte.

## 2026-11-19 · Aprobarea D-037 (găzduire)
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`.

**Ce am cerut**
- Aprobarea D-037: Render (aplicația din Dockerfile) + Neon (PostgreSQL cu PostGIS).

**Ce am primit**
- D-037 aprobată; SCOPE.md și Q-012 actualizate.

**Ce am făcut cu răspunsul**
- Am aprobat commit-ul și push-ul. Continui cu primul /plan într-o conversație nouă.

## 2026-11-19 · Aprobarea D-029 – D-036, tokens noi, găzduirea
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`.

**Ce am cerut**
- Aprobarea D-029 – D-036 și a tokens-urilor noi. La D-032: limita parolei e de maxim 72 de
  octeți UTF-8, verificată pe server, cu mesajul „Parola e prea lungă”; minimul rămâne
  8 caractere. La D-035: lista de rapoarte e pe pagina liniei.
- Q-012: rămâne Render + Neon; riscurile se rezolvă în /plan.
- Confirmarea din nou a mock-urilor DS-autobuze-statie și DS-autobuze-harta.

**Ce am primit**
- D-029 – D-036 aprobate; D-032 rescrisă cu limita în octeți. La D-031, „stopurile” a
  devenit „rândurile din stops.txt” („stop” e termen de evitat în CONTEXT.md).
- Tokens noi în DESIGN.md (`--text-time-sm`, `--size-page`, `--size-form`, `--size-panel`),
  folosite în cele 4 mock-uri, fără schimbări vizuale; numele screenshot-urilor după D-029.
- SCOPE.md și definiția „Stație” din CONTEXT.md, actualizate după D-030, D-031, D-033, D-034.
- Q-012 răspunsă și propunerea D-037 (găzduire pe Render + Neon), încă neaprobată.
- DS-autobuze-statie și DS-autobuze-harta înapoi pe `approved`.

**Ce am făcut cu răspunsul**
- Am aprobat commit-ul și push-ul.

## 2026-11-19 · Recapitulare: ce lipsea, ce se contrazicea; FIELD.md, README.md
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`.

### 1. Recapitularea
**Ce am cerut**
- Recitirea README.md, SCOPE.md, CONTEXT.md, a întrebărilor deschise și a mock-urilor:
  ce e neclar, ce lipsește, ce se contrazice; întrebările rămase.

**Ce am primit**
- Trei corecturi mici făcute direct (SCOPE.md și Q-005 depășite).
- 19 întrebări: găzduirea și screenshot-urile (urgent), 3 contradicții (harta pentru
  utilizatorul logat, accentul pe markerul hărții, definiții din CONTEXT.md), lipsuri
  (ce e o stație, ieșirea din cont, intrarea spre pagina liniei, raportul fără plecări,
  întoarcerea după login, regulile de înregistrare, data în planificator, „aproape de
  mine”), plus sărbătoarea pe pagina de stație, abaterile vizibile, tokens lipsă,
  README.md, FIELD.md și cererea către Tursib.

**Ce am făcut cu răspunsul**
- Am răspuns la toate 19 și am cerut fiecare decizie nouă ca PROPOSAL.

### 2. Ce a rezultat
**Ce am primit**
- D-029 – D-036, toate `proposed`: screenshot-uri cu Playwright, harta la /harta, ce e o
  stație, autentificare și înregistrare, data în planificator, „aproape de mine”,
  abaterile vizibile pe pagina liniei, raportul fără plecări. Q-007 răspunsă; la Q-012 un
  răspuns propus (Render + Neon), cu limitele planurilor gratuite verificate și riscurile
  pentru /plan.
- O propunere de tokens noi pentru DESIGN.md (nescrisă în fișier până la aprobare).
- CONTEXT.md: două definiții corectate și termenul „Sens”.
- Mock-urile: „Ieșire” în antet, eticheta liniei ca link, starea „luni, sărbătoare
  legală”, markerul hărții pe culoarea textului, data în planificator, „nicio stație în
  1 km”. Pagina de stație și harta au trecut înapoi pe `draft`.
- `field/observations.csv` și `FIELD.md`, generate din xlsx; toate cele 20 de abateri
  recalculate se potrivesc cu fișa. `README.md`.
- Semnalări: limita BCrypt e de 72 de octeți, nu de caractere; jobul zilnic de import nu
  rulează cât timp serviciul Render gratuit doarme.

**Ce am făcut cu răspunsul**
- Am aprobat commit-ul și push-ul. Cererea către Tursib (Q-009) o trimit azi.

## 2026-11-19 · Mock-uri: stația refăcută, planificator, autentificare, hartă
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`.

### 1. Pagina de stație refăcută
**Ce am cerut**
- Un mock mai puțin simplu, doar cu tokens din DESIGN.md: prima plecare din grup mare, ca
  pe panou, celelalte două mai mici; fiecare grup un card cu o linie în culoarea liniei;
  sensul ca săgeată + capăt de linie; o hartă mică cu stația sus; iconițe simple
  (autobuz, ceas, stea); mock-ul înapoi pe draft. Și o verificare a barei de control.
- Apoi: linia colorată deasupra card-ului, nu în stânga.

**Ce am primit**
- Mock-ul refăcut, cu harta ca desen schematic (fără coordonate reale ale stației).
- Două erori găsite în bara de control: previzualizarea la 360 px folosea stilurile de
  desktop pe un ecran lat, iar la reîncărcare selecturile și pagina nu se mai potriveau.
  Ambele reparate.
- Semnalarea că nu există token pentru „ora mai mică” (am folosit mărimea din `--text-lg`).

### 2. Mock-uri noi: planificator, autentificare, hartă
**Ce am cerut**
- Exemple simple pentru planificator și autentificare, apoi pentru pagina hărții.
- Să pot naviga între ele: linkurile din mock-uri nu duceau nicăieri.

**Ce am primit**
- `DS-autobuze-planificator`, `DS-autobuze-login` și `DS-autobuze-harta`, cu stări și
  date de exemplu marcate ca inventate.
- Mock-urile legate între ele din antet și din butoane, cu tema și lățimea păstrate.
- Răspunsuri la două întrebări: pagina Acasă nu a existat niciodată ca mock; starea
  „Utilizator logat” e doar în meniul „Stare” al paginii de stație.

**Ce am făcut cu răspunsul**
- Am amânat mock-ul paginii Acasă și comutatorul vizitator / logat până înainte de
  build-ul ecranelor respective; sunt notate ca pași următori în `registry.yaml`.
- Am confirmat în browser DS-autobuze-statie și DS-autobuze-harta → `approved`.
  Planificatorul și autentificarea rămân `draft`.
- Am aprobat commit-ul și push-ul.

## 2026-11-19 · Pagina de stație: răspunsurile la mock, D-028, confirmarea în browser
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`.

### 1. Răspunsurile la cele 4 întrebări din mock
**Ce am cerut**
- Actualizarea mock-ului DS-autobuze-statie cu răspunsurile mele: numele rămâne
  „Când vine autobuzul?”; ora exactă mare, „în X min” mic dedesubt; 24:41 apare ca
  „00:41”, fără altă notă; link „Raportează” la fiecare plecare. Plus intrarea din
  AI_LOG pentru sesiunea de design.

**Ce am primit**
- Constatarea că 3 din 4 răspunsuri erau deja în mock; singura schimbare de aranjare a
  fost „în X min” sub oră. O corectură necerută: linkul de raport mărit la 44 × 44 px
  (regula de țintă din DESIGN.md).
- Intrarea din AI_LOG, reconstituită din commit-uri și D-027, cu avertismentul că partea
  „Ce am cerut” trebuie verificată de mine.
- Două semnalări: dacă vizitatorii văd „Raportează” și faptul că un link pe fiecare
  plecare permitea raportarea unor plecări care nu au avut loc încă.

**Ce am făcut cu răspunsul**
- Am confirmat că vizitatorii nu văd „Raportează”, doar rândul cu autentificarea (D-009).
- Am schimbat răspunsul 4: un singur buton „Raportează o abatere” pe fiecare grup de
  linie și sens; formularul arată plecările din ultimele 60 de minute; plecările viitoare
  nu se pot raporta → propus ca D-028, care completează D-017.
- Am păstrat data intrării din AI_LOG; partea „Ce am cerut” o verific eu.

### 2. Aprobări
**Ce am primit**
- Propunerea D-028 (cu întrebarea dacă formularul se limitează la sensul grupului) și o
  propunere de schimbare în DESIGN.md: fără board de design, confirmarea în browser.

**Ce am făcut cu răspunsul**
- Am aprobat D-028, cu sensul limitat la grup.
- Am aprobat schimbarea din DESIGN.md și registry.yaml: nu avem board, conform
  laboratorului confirm mock-ul din browser.
- Am confirmat mock-ul paginii de stație → DS-autobuze-statie are status `approved`.
- Am aprobat fiecare commit și push.

## 2026-11-19 · Design: direcția vizuală, tokens, registrul de ecrane, primul mock
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`.

### 1. Direcția vizuală și tokens
**Ce am cerut**
- Direcția vizuală a aplicației și tokens de design (culori, fonturi, spațieri) pentru
  `docs/design/DESIGN.md`.

**Ce am primit**
- Variante de direcție vizuală; propunerea D-027 și tabelele de tokens pentru light și
  dark, cu contrastul fiecărei culori.

**Ce am făcut cu răspunsul**
- Am ales baza neutră cu un singur accent, cu stil de panou de stație doar la orele de
  plecare și la eticheta liniei (culoarea din `route_color` / `route_text_color`), fără
  culorile sau logo-ul Tursib → D-027 aprobat.
- Am cerut o corectură: contrastul fiecărei culori verificat pe ambele fundaluri,
  `--color-bg` și `--color-surface`; apoi am aprobat tokens.
- Am spus că feed-ul Tursib are `route_color` / `route_text_color`; încă neverificat în feed.

### 2. Registrul de ecrane
**Ce am primit**
- `docs/design/registry.yaml` construit din `SCOPE.md` → Ecrane: 12 ecrane, 7 componente
  și fundația de tokens.

**Ce am făcut cu răspunsul**
- Am aprobat lista, cu pagina comună de eroare (DS-autobuze-eroare, pentru 403, 404 și
  500) și fără pagină „Despre”. Primul mock: pagina de stație.

### 3. Primul mock: pagina de stație
**Ce am primit**
- `docs/design/mocks/DS-autobuze-statie.html` (status `draft`): plecările pe linie și
  sens, toate stările (vizitator, logat, „mâine”, după miezul nopții, text lung, gol,
  încărcare, eroare), light și dark, la 360 px; date de exemplu pornind de la stația
  Turnișor din fișa de teren.
- 4 întrebări rămase deschise: numele aplicației, cum se afișează ora, eticheta curselor
  de după miezul nopții și linkul de raport la fiecare plecare.

**Ce am făcut cu răspunsul**
- Am lăsat mock-ul ca draft, cu cele 4 întrebări deschise, pentru sesiunea următoare.

## 2026-11-18 – 2026-11-19 · Setup agentic + interviu de scope
**Unealta:** Claude Code (model Claude Opus 5.5), în repo-ul local `p07-autobuze`.

### 1. Setup (2026-11-18)
**Ce am cerut**
- Rularea skill-ului `/graffino-agentic-code-setup` pe repo-ul gol (fără commit-uri, fără cod).

**Ce am primit**
- Întrebări despre scop, stack, oraș, addon-ul de echipă și limba documentelor.
- Fișierele de lucru: `AGENTS.md`, `CLAUDE.md`, `CONTEXT.md`, registrul din `docs/agents/`
  (decizii, pattern-uri, contracte, întrebări, arhitectură), `docs/design/`, skill-urile
  `/plan` și `/build`, agenții planner și builder.
- Propuneri de decizii (PROPOSAL) și întrebări deschise (Q-) pentru tot ce nu era stabilit.
- Semnalări pe care nu le cerusem: D-001 („totul local”) contrazicea găzduirea publică
  (D-007); o regulă din `AGENTS.md` rămăsese depășită; `.env` trebuia ignorat de git;
  emailul de universitate ar fi devenit public în commit-uri.

**Ce am făcut cu răspunsul**
- Am ales stack-ul (Java 21 + Spring Boot, PostgreSQL/PostGIS, Flyway, Thymeleaf + Leaflet,
  Docker Compose), documentele în română și fără addon-ul de echipă.
- Am dat orașul și feed-ul (Sibiu, Tursib S.A.) și textul pentru licență → `DATA.md`.
- Am răspuns la întrebări și am aprobat D-001 – D-011 (stack, maxim un transfer, import
  versionat, doar orar static, tile-uri OSM, Maven, găzduire HTTPS, atribuirea Tursib,
  rapoarte de abatere, formatul datelor de teren, autentificarea).
- Am acceptat reformularea D-001, ștergerea regulii depășite și `.env` în `.gitignore`.
- Am schimbat contul GitHub pe Mihai-Sandu-dev, cu adresa noreply ca autor al
  commit-urilor; repo-ul public a fost creat și urcat pe `main`. Fiecare comandă de push
  mi-a fost arătată înainte și am aprobat-o.

### 2. Interviu de scope (2026-11-19)
**Ce am cerut**
- Un interviu cu întrebări una câte una, fără cod, despre variabile, roluri, date, ecrane
  și ce nu intră în proiect, sărind peste ce era deja decis. Datele de teren din
  `field/Date_Teren_Autobuze.xlsx` folosite exact cum sunt; ce nu știu devine Q-.

**Ce am primit**
- Un rezumat al fișei de teren: 20 de observații (numerotate 1–12 și 14–21), 6 linii,
  9 stații, 1 interviu din 3, plus diferențele găsite (foaia „Stații” are 6 din cele
  9 stații, numele fișierului de poză doar la Metro).
- 18 întrebări cu variante de răspuns.
- `SCOPE.md`; propunerile D-012 – D-023; întrebările Q-016 – Q-020; registrul împărțit
  pe teme (`docs/agents/decisions/`, `docs/agents/questions/`); 3 termeni noi în
  `CONTEXT.md`.

**Ce am făcut cu răspunsul**
- Am ales variantele: transfer la 150 m, 2 min + mers la 1,2 m/s, primele 3 itinerarii
  după sosire, tipuri de zi lucrătoare / sâmbătă / duminică + sărbătoare, următoarele 3
  plecări completate cu ziua următoare, job zilnic + buton admin, pagina „/” hartă /
  „Acasă”, formularul de raport cu ora reală, ce nu intră în proiect.
- Am completat două răspunsuri cu reguli proprii: lista sărbătorilor nu modifică
  plecările (D-015); rapoartele și favoritele se leagă de identificatori stabili, ca să
  nu se piardă la ștergerea unei versiuni (D-020).
- La o întrebare am răspuns „nu știu încă” (cum apar observațiile de teren); a rămas Q-016.
- Am aprobat D-012 – D-023 și termenii noi. Am răspuns la Q-015 (dată YYYY-MM-DD, oră
  HH:MM, abatere întreg cu semn) și Q-016 (sursa „teren”) → propuse ca D-024 și D-025.
- Q-017 (poze) și Q-018 (interviuri) rămân la mine: completez foaia „Stații” și fac
  celelalte 2 interviuri.
- Am hotărât ca xlsx-ul să intre în repo, ca dovadă de teren (are doar inițiale).
