# AGENTS.md — p07-autobuze

<!-- graffino-agentic:begin v1 -->
## How we work here

**Before any work:** `git fetch --all --prune`, then `git pull --ff-only`. Stop on a
non-fast-forward or on uncommitted changes you did not make.

**Always load** (indexes; open the entry, or its topic file, before you rely on it):
`docs/agents/DECISIONS.md`, `docs/agents/PATTERNS.md`,
`docs/agents/CONTRACTS.md`, `docs/agents/OPEN-QUESTIONS.md`, `CONTEXT.md`.

**Load on demand:**

| When | Load |
|---|---|
| You rely on an entry | the entry, or its topic file under `docs/agents/<kind>/` |
| Structural work | `docs/agents/ARCHITECTURE.md` |
| Old code or a past decision needs explaining | `docs/agents/DECISIONS-SUPERSEDED.md`, `PATTERNS-SUPERSEDED.md` (never obeyed) |
| UI work | `docs/design/DESIGN.md`, `docs/design/registry.yaml` |
| Ce intră în proiect și ce nu | `SCOPE.md` |
| Import, date GTFS sau atribuire | `DATA.md` (sursă, licență, probleme, curățare) |
| Date de teren sau seed-ul de abateri | `FIELD.md`, `field/observations.csv` (completate doar de autor) |

**Flow:** a change to more than a few lines, or one with a design choice, starts with
`/plan`. The approved plan runs with `/build`. Small fixes go straight to build, with
tests first where tests exist.

### The gate
Ask yourself: **does this change something already approved?**
- **Do, then report:** add a `candidate` pattern; add or answer an open question
  (only with an answer the user gave); write a `Proposed answer` the evidence
  supports (the question stays `investigating` until the user confirms it); fix a
  typo, link or moved path; code that follows the approved entries.
- **Propose and wait:** change, supersede or delete an `approved` pattern or
  decision; promote `candidate → approved`; add a decision; code that contradicts an
  entry. Use this shape, then stop:

```text
PROPOSAL: <add-decision | change | supersede | promote | delete>
- Existing: <ID and what it says, or "none">
- Proposed: <the new rule>
- Why now: <what changed>
- Impact: <files that follow the old rule>
- If approved: <old ID> → superseded-by <new ID> (kept, not deleted)
```
Before you propose, check `docs/agents/REJECTED.md`: never re-propose a rejected item
without new evidence. A rejected proposal is added there with the reason.

### Hard rules
- Build only from `approved` entries. Unanswered means out of scope: record a Q-,
  never invent a value, a requirement or a behaviour.
- Add nothing nobody asked for. Note it as a suggestion instead.
- Build the smallest change that meets the approved criteria, reusing existing code.
  A new abstraction, dependency, configuration option, extension point or broad
  refactor needs a concrete reason today. Never at the cost of tests or error handling.
- Tests first where the repo has tests. Never change what an existing test asserts
  to make code pass; an assertion you think is wrong goes to the user.
- Never write a secret, token, password or personal data into a file, commit or log.
- Never push, tag, release, or deploy without the user's go for that action.
- When you cite past work, label the intent: **replicate** (only for an `approved`
  pattern) or **for awareness only**. History is not a template.
- The code wins over any document. When they disagree, say so and fix the document.
- Report honestly: a failed check is reported with its output; a skipped step is
  named with its reason.

### Ledger rules
- IDs are one sequence per kind in this repo: `D-`, `PATTERN-`, `C-`, `Q-`. Never renumber.
- A provided contract (C-) lives here only; consumers link to it. Changing one is a
  PROPOSAL, and the plan names the consumer files too.
- Superseded entries stay (`status: superseded-by <ID>`) so old code stays explainable.
  Their index lines live in `DECISIONS-SUPERSEDED.md` / `PATTERNS-SUPERSEDED.md`, not
  in the always-loaded indexes; the lint (`--fix`) moves them.
- After any ledger change: `python3 docs/agents/tools/ledger-lint.py --fix` (next free
  ID: `--next D`). It must pass.
- **Confirmed:** when the user confirms an entry in a plan, set `- **Confirmed:** <date>`
  on it. Any edit to an entry removes its Confirmed line.
- A new reusable practice becomes a `candidate` pattern in the same session.
- A settled constraint becomes a decision PROPOSAL in the same session.
<!-- graffino-agentic:end -->

## 1. Ce este acest repo
Aplicație de transport public pentru Sibiu, pe datele GTFS ale Tursib S.A. (DATA.md).
Funcții cerute: import complet al feed-ului GTFS, căutare de stații pe hartă,
următoarele plecări dintr-o stație, vizualizarea traseelor și un planificator simplu
cu maxim un transfer (D-002). Doar orar static, fără GTFS-Realtime (D-004); abaterile vin
din rapoartele utilizatorilor logați (D-009). Dezvoltare locală în Docker Compose
(D-001); găzduire publică cu HTTPS (D-007).

## 2. Stack și comenzi
D-001: Java 21 + Spring Boot (REST API), PostgreSQL cu
PostGIS, migrări Flyway, pagini Thymeleaf cu o hartă Leaflet care citește din API;
dezvoltarea locală rulează în Docker Compose.

Build: Maven prin `./mvnw`; teste JUnit 5 + Testcontainers PostGIS (D-006).

```bash
./mvnw verify                    # build + teste unitare (*Test) și de integrare (*IT, Testcontainers PostGIS)
./mvnw test                      # doar testele unitare
cp .env.example .env             # o dată; completează POSTGRES_PASSWORD (.env nu intră în git)
docker compose up -d --build     # pornește aplicația (http://localhost:8080) și PostGIS
./mvnw test -Pui                 # teste de UI și screenshot-uri cu Playwright, pe aplicația din Compose (D-029)
python3 docs/agents/tools/ledger-lint.py   # lint-ul registrului (rulează și în CI)
```
Screenshot-urile se salvează în `docs/design/screens/<nume>.<light|dark>.<360|1280>.png`.
La prima rulare, Playwright descarcă Chromium (~100–200 MB). Dacă descărcarea nu merge, testele de UI rulează în imaginea
oficială Playwright (PATTERN-001, candidate).

## 3. Structură
- `docs/agents/` — registrul (decizii, pattern-uri, contracte, întrebări) și arhitectura.
- `docs/design/` — reguli de design, registrul de ecrane, mock-uri și screenshot-uri.
- `.claude/` — skill-urile `/plan` și `/build` și agenții planner și builder.

## 4. Reguli pentru acest repo
- Termenii de domeniu sunt cei din `CONTEXT.md` (stație, linie, traseu, cursă, plecare...).
- Sursa, licența și problemele datelor GTFS se țin în `DATA.md`.
- Stack-ul e cel din D-001 (aprobat). O tehnologie sau o dependență nouă se propune întâi (PROPOSAL).

## 5. Release
Aplicația va fi publică, pe HTTPS (D-007); furnizorul e Q-012, stabilit la /plan.
Termen: un deploy „hello” pe URL-ul live înainte de checkpoint-ul din 2026-11-22.
Branch-uri, versiune și changelog: nestabilite încă.
Niciun push, tag sau deploy fără acordul utilizatorului.

## 6. Definition of done
- `./mvnw verify` trece (build și teste, inclusiv Testcontainers).
- Screenshot-uri pentru fiecare ecran schimbat, light și dark, comparate cu `docs/design/DESIGN.md` (procedura: Q-007).
- Practicile noi sunt notate ca pattern-uri `candidate`; punctele deschise ca intrări Q-.
