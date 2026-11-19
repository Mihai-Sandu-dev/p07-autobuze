# AI_LOG — p07-autobuze

> Jurnalul folosirii AI în proiect: pentru fiecare sesiune, ce am cerut, ce am primit
> și ce am făcut cu răspunsul. Cele mai noi intrări sunt primele.

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
