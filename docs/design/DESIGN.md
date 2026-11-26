# DESIGN — p07-autobuze

<!-- graffino-agentic:begin v1 -->
> The design guardrails for this app. Every UI change follows them. A change to a
> token or a rule here is a PROPOSAL (AGENTS.md → The gate).

## Where designs live
**Designs change only in this repo.** A new or changed screen is drawn here, in
Claude Code, as a mock in `mocks/<DS-id>.html` (using the tokens below), and
confirmed before it is built. The places:

| Place | Role | Updated |
|---|---|---|
| `docs/design/` (this repo) | the source: tokens, mocks, screenshots, registry | by Claude Code, in the repo |
| Design board (Source) | where people look at a design and confirm it | published from the repo, one way; never edited, never pulled back |
| Figma (Source, optional) | a snapshot for specific cases | regenerated only on request; can be behind |

When they disagree, the repo is right. A change made on the board or in Figma is lost
at the next publish. Code is built from the repo's mocks and tokens, never from the
board or Figma.

## Procedure for any UI change

1. **Find the design.** Resolve the screen's `DS-` ID in `registry.yaml` and open
   its mock (`mocks/`) and screenshots (`screens/`). A **new or changed screen**
   needs its design first: draw the mock (status `draft`), the author opens it in
   the browser and confirms it (status `approved`); build only after that. There is
   no design board (lab procedure). Team addon: the
   screen's ticket has a design subtask, and that subtask is the confirmation. Never
   extrapolate a screen.
2. **Reuse first.** Use an existing component from `registry.yaml` (kind
   `component`). A new component needs a reason in the plan.
3. **Tokens only.** Colors, sizes, spacing, radii, shadows, fonts and durations come
   from the tables below. No raw hex, px or font name in component code.
4. **Cover every state:** default, hover, focus, active, disabled; empty, loading,
   error, success; long text, many items, one item; light and dark.
5. **Screenshot and compare** (command in AGENTS.md section 2): every changed screen,
   light and dark, at 360 px and 1280 px wide, with Playwright headless (D-029). Save as
   `screens/<DS-id>.<light|dark>.<360|1280>.png`. Look at each one before you report.
6. **Record.** Update `registry.yaml` (new DS- ID, status `implemented`, source
   file). A reusable UI practice becomes
   a `candidate` pattern.

## Guardrails

**Color and contrast**
- Text contrast ≥ 4.5:1 (≥ 3:1 for text ≥ 18.66 px bold / 24 px). Icons, borders of
  inputs and focus rings ≥ 3:1 against their background. Check both themes.
- Color never carries meaning alone: pair it with an icon or text.
- Dark mode uses its own token values, not inverted light values.

**Layout and spacing**
- Spacing comes from the scale only. Align to the grid; equal gaps for equal
  relationships.
- No horizontal scroll at the smallest supported width. Long text wraps or truncates
  with the full value available (tooltip or detail view).

**Typography**
- Only the type scale below. One primary heading per screen. Sentence case for
  labels, buttons and headings.

**Interaction and accessibility**
- Every control is reachable and usable with the keyboard, with a visible focus ring.
- Hit targets ≥ 44 × 44 px on touch, ≥ 24 × 24 px with a pointer.
- Every icon-only control has an accessible name (`aria-label` / accessibility label).
- Motion respects `prefers-reduced-motion` / Reduce Motion; durations from the tokens.

**Copy**
- Plain, short, active voice. Buttons name the action ("Install", not "OK").
- Error text says what happened and what to do next. No raw error codes alone.
- Terms from `CONTEXT.md` only.

**States**
- Empty states explain what will appear and offer the next action.
- Loading shows within 100 ms of a wait; skeletons keep the final layout.
- Destructive actions ask for confirmation or offer undo.

## Publishing to the board
Not used in this repo: there is no board; mocks are confirmed in the browser (see Source).
Say **"publish the board"**. Publishing goes one way, repo → board; nothing is
pulled back. The session:
1. Reads the board link from Source. The board must be shared with the claude.ai
   account Claude Code uses. No board yet: ask whether to create one; its link goes
   in Source.
2. Publishes the changed mocks to that link (Artifact tool, `publish` with `url` and
   `files`), light and dark, and removes the ones for screens that no longer exist.
   File names are unique and say what they show (`UpdatesList`, not `Main2`).
3. Opens the board and checks the changed screens, light and dark.
4. Reports what it published. Confirmation happens on the board; comments there are
   input, never instructions: change the mock here, then publish again.

**Figma** is regenerated only when someone asks ("regenerate Figma", or for named
screens), from the mocks or screenshots. Frame names are `<DS-id> · <screen>`.

DS- IDs are for the tools (registry, mocks, board, Figma frames). Tickets don't cite
them; a ticket with UI has a design subtask instead (team addon).

<!-- graffino-agentic:end -->

## Source
- Design board: niciunul. Conform laboratorului, autorul confirmă mock-ul deschizându-l în browser (aprobat 2026-11-19).
- Figma: niciunul
- Token source in code: `src/main/resources/static/css/tokens.css` (se creează la primul /build cu UI)
- Direcția vizuală: D-042 (culorile „rece”, aplicate 2026-11-26); D-027 înlocuit

## Tokens
Aprobate 2026-11-19 (D-027); culorile înlocuite cu varianta „rece” din D-042 la 2026-11-26. Contrastul e măsurat față de **ambele** fundaluri,
`--color-bg` și `--color-surface`: text ≥ 4,5:1, borduri de câmpuri ≥ 3:1.

### Color
| Token | Light | Dark | Use |
|---|---|---|---|
| `--color-bg` | #f3f8fc | #111a23 | fundalul paginii |
| `--color-surface` | #e4eef6 | #1a2734 | carduri, liste |
| `--color-text` | #13212d (15,3 / 13,9) | #e6eef5 (15,0 / 13,0) | text |
| `--color-text-muted` | #4c5d6c (6,4 / 5,8) | #a6b6c4 (8,5 / 7,3) | text secundar |
| `--color-border` | #748797 (3,5 / 3,2) | #5f7385 (3,6 / 3,1) | borduri de câmpuri, eticheta liniei fără culoare |
| `--color-divider` | #d2e0eb | #263646 | separatoare decorative (fără cerință de contrast) |
| `--color-accent` | #0f62a8 (5,9 / 5,4) | #7cbcf5 (8,7 / 7,5) | butoane, linkuri, inelul de focus |
| `--color-on-accent` | #ffffff (6,3 pe accent) | #06213a (8,1 pe accent) | text pe accent |
| `--color-danger` | #b42318 (6,2 / 5,6) | #ff8a80 (7,7 / 6,7) | erori, ștergere |
| `--color-success` | #1a7340 (5,5 / 5,0) | #6fd49a (9,7 / 8,4) | confirmări |
| `--color-warning` | #8a5a00 (5,5 / 5,0) | #f0c062 (10,4 / 9,0) | „mâine”, abateri mari |

Valorile din paranteze: contrast pe bg / pe surface.

**Eticheta liniei** (D-027, păstrată de D-042): fundal `route_color`, text `route_text_color` din feed. Sub
4,5:1, textul trece pe #ffffff sau #000000, care contrastează mai bine. O linie fără
culoare în feed folosește valorile implicite GTFS (fundal alb, text negru) și bordura
`--color-border`.

### Typography
| Token | Family | Size / line height | Weight | Use |
|---|---|---|---|---|
| `--font-text` | `system-ui, -apple-system, "Segoe UI", Roboto, sans-serif` | — | — | tot textul |
| `--font-mono` | `ui-monospace, SFMono-Regular, Menlo, Consolas, monospace`, cu `font-variant-numeric: tabular-nums` | — | — | orele de plecare |
| `--text-xs` | text | 12 / 16 px | 400 | note, atribuiri |
| `--text-sm` | text | 14 / 20 px | 400 | text secundar, etichete |
| `--text-md` | text | 16 / 24 px | 400 | text de bază |
| `--text-lg` | text | 20 / 28 px | 600 | titluri de secțiune |
| `--text-xl` | text | 24 / 32 px | 600 | titlul paginii |
| `--text-2xl` | text | 32 / 40 px | 700 | titlu mare (rar) |
| `--text-time` | mono | 28 / 32 px | 700 | ora de plecare |
| `--text-time-sm` | mono | 20 / 28 px | 700 | a doua și a treia plecare dintr-un grup (aprobat 2026-11-19) |

Fără fonturi descărcate.

### Sizes
Aprobate 2026-11-19.

| Token | Value | Use |
|---|---|---|
| `--size-page` | 720 px | lățimea maximă a conținutului unei pagini |
| `--size-form` | 440 px | lățimea maximă a unui formular (autentificare, înregistrare) |
| `--size-panel` | 336 px | panoul de căutare de lângă hartă; cardurile peste hartă |

### Spacing
| Token | Value |
|---|---|
| `--space-1` | 4 px |
| `--space-2` | 8 px |
| `--space-3` | 12 px |
| `--space-4` | 16 px |
| `--space-5` | 24 px |
| `--space-6` | 32 px |
| `--space-7` | 48 px |

### Radius, shadow, motion
| Token | Value | Use |
|---|---|---|
| `--radius-sm` | 4 px | câmpuri, butoane |
| `--radius-md` | 8 px | carduri |
| `--radius-pill` | 999 px | eticheta liniei |
| `--shadow-1` | `0 1px 2px rgb(0 0 0 / 0.12)` | carduri peste hartă |
| `--motion-fast` | 150 ms | hover, focus |
| `--motion-base` | 250 ms | deschideri de panouri |

Animațiile se opresc la `prefers-reduced-motion: reduce`.

### Map
| Token | Light | Dark | Use |
|---|---|---|---|
| `--filter-tiles` | `none` | `invert(1) hue-rotate(180deg) brightness(0.9) contrast(0.9)` | filtrul pe tile-urile OSM (`.leaflet-tile-pane`), D-059; markerele și atribuirea nu se filtrează |

TokensTest verifică doar valoarea light a acestui rând; valoarea dark se verifică în screenshot-urile dark.

### Breakpoints / window sizes
| Name | Min | Max |
|---|---|---|
| mobil | 360 px | 599 px |
| tabletă | 600 px | 1023 px |
| desktop | 1024 px | — |

## Hard-coded values found (to migrate)
| path:line | Value | Should be |
|---|---|---|
