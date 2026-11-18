# AI_LOG — p07-autobuze

> Jurnalul folosirii AI în proiect: pentru fiecare sesiune, ce am cerut, ce am primit
> și ce am făcut cu răspunsul. Cele mai noi intrări sunt primele.

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
