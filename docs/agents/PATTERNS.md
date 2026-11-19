# Patterns — p07-autobuze

<!-- graffino-agentic:begin v1 -->
> How we build things here. Copy only `approved` entries. `candidate` = seen once or
> not yet confirmed; agents add these freely. Promotion needs the user's OK.
<!-- graffino-agentic:end -->

## Index
- PATTERN-001 · Testele de UI rulează în imaginea oficială Playwright când browser-ul nu se poate descărca · candidate

---

## PATTERN-001 · Testele de UI rulează în imaginea oficială Playwright când browser-ul nu se poate descărca
- **Status:** candidate
- **Date:** 2026-11-19
- **How:** Cu aplicația pornită în Compose, rulează `./mvnw test -Pui` în `mcr.microsoft.com/playwright/java:v<versiunea din pom>-noble`, cu rețeaua gazdei, utilizatorul curent și cache-ul Maven montat; `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`. Versiunea imaginii = `playwright.version` din pom.xml, ca revizia de Chromium să se potrivească.
- **Example:** `docker run --rm --network host --user "$(id -u):$(id -g)" -e HOME=/tmp/home -e PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 -v "$HOME/.m2:/tmp/home/.m2" -v "$PWD:/work" -w /work mcr.microsoft.com/playwright/java:v1.64.0-noble ./mvnw -B test -Pui`
- **Why:** La primul build (2026-11-19), descărcarea Chromium de pe storage.googleapis.com a mers cu ~65 KB/s și apoi a eșuat; imaginea de pe mcr.microsoft.com are browser-ele incluse.
- **Evidence:** build-ul scheletului, 2026-11-19; 7 teste UI și 4 screenshot-uri trecute așa

