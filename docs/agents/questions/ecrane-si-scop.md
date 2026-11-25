# Open questions — ecrane și textele lor

## Q-022 · Cum spun paginile reale că aplicația nu e oficială Tursib?
- **Status:** answered 2026-11-25
- **Blocks:** mock-ul și planul hărții (DS-autobuze-harta) și paginile reale de după ea. Textul paginilor (titlu, „stațiile Tursib”, „Date de transport: Tursib S.A.”) nu spune nicăieri că aplicația e neoficială, deci un motor de căutare sau un asistent AI o poate prezenta drept serviciul Tursib, contrar intenției din D-027. Locul (subsol, meta description, pagina de pornire) și textul exact nu sunt stabilite.
- **Who answers:** utilizatorul, la mock-ul și planul hărții
- **Evidence:** review-ul SEO/GEO al PR-ului #1, 2026-11-19
- **Answer:** În subsolul fiecărei pagini, lângă „Date de transport: Tursib S.A.”, textul „Aplicație neoficială. Nu este afiliată cu Tursib S.A.”. Cu el, pagina de stație nu mai are nevoie de `noindex`. (Răspunsul utilizatorului la /plan pentru punctul 8, 2026-11-25.)
- **Recorded as:** D-049

## Q-024 · Următoarele plecări arată și cursele care se termină în stația aceea?
- **Status:** answered 2026-11-25
- **Blocks:** lista „Următoarele plecări” (punctul 8, D-018). În `stop_times`, ultima stație a unei curse are și ea o oră de plecare, deși din capăt cursa nu mai pleacă nicăieri. `Departures` (punctul 7) le întoarce pe toate, cu ordinea din cursă; punctul 8 decide dacă le arată.
- **Who answers:** utilizatorul, la /plan pentru punctul 8
- **Evidence:** build-ul punctului 7, 2026-11-25
- **Answer:** Nu. „Următoarele plecări” nu arată o cursă în stația în care se termină (ultimul `stop_sequence` al cursei), ca la panoul din stație.
- **Recorded as:** D-048

## Q-025 · Ce arată pagina de stație pentru o plecare în mai puțin de un minut?
- **Status:** answered 2026-11-25
- **Blocks:** doar textul de sub ora plecării (DS-autobuze-statie). Acum minutele se rotunjesc în jos, deci o plecare la 23:15, privită la 23:14:31, apare „în 0 min”. Mock-ul nu are cazul. Variante posibile: „în 0 min” (cum e acum), „acum”, „sub 1 min”, sau minutele rotunjite în sus.
- **Who answers:** utilizatorul
- **Evidence:** pagina Gară (1051) pe feed-ul real, la build-ul punctului 8, 2026-11-25
- **Answer:** Sub un minut, pagina afișează „acum”. (Răspunsul utilizatorului, 2026-11-25.)
- **Recorded as:** o decizie propusă după build-ul punctului 8, în așteptarea aprobării
