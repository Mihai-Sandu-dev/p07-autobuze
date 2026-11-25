# Decisions — hartă, pagini, atribuire, ce nu intră

## D-005 · Harta folosește tile-uri OpenStreetMap, cu atribuire
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Fundalul hărții Leaflet vine din tile-urile OpenStreetMap. Harta afișează mereu atribuirea „© OpenStreetMap contributors”.
- **Why:** Alegerea utilizatorului; atribuirea e cerută de licența OSM.
- **Rejected:** alți furnizori de tile-uri.
- **Evidence:** răspunsul utilizatorului la Q-004, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-008 · Fiecare pagină afișează în subsol „Date de transport: Tursib S.A.”
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Subsolul fiecărei pagini afișează textul „Date de transport: Tursib S.A.”.
- **Why:** Atribuirea cerută pentru datele Tursib (DATA.md → Licență).
- **Rejected:** atribuire doar pe o pagină „Despre”.
- **Evidence:** cerința utilizatorului, 2026-11-18; DATA.md
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-022 · Pagina principală: hartă pentru vizitatori, „Acasă” pentru utilizatorii logați
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Pe pagina „/”, un vizitator nelogat vede harta cu stații și căutarea. Un utilizator logat vede „Acasă”: plecările de la stațiile favorite, cu harta dedesubt.
- **Why:** Răspunsul utilizatorului.
- **Rejected:** hartă pentru toți și „Acasă” pe o pagină separată.
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-023 · În afara scopului: alte orașe, rutare pe străzi, notificări, aplicație mobilă, bilete, alte limbi
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Proiectul acoperă doar Sibiu, cu un singur feed (Tursib). Nu calculează drumul pe jos pe străzi (doar distanța în linie dreaptă, D-012). Nu are notificări push sau email, aplicație mobilă nativă sau cumpărare de bilete. Interfața e doar în română. GTFS-Realtime e exclus prin D-004.
- **Why:** Răspunsul utilizatorului.
- **Rejected:** —
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-027 · Direcția vizuală: bază neutră, un accent, stil de panou doar la plecări și linii
- **Status:** superseded-by D-042
- **Date:** 2026-11-19
- **Rule:** Interfața are o bază neutră (alb / gri închis) și un singur accent, folosit doar pentru acțiuni. Stilul de panou de stație apare doar unde contează: orele de plecare sunt mari, cu cifre monospace, iar numărul liniei stă într-o etichetă colorată. Culoarea etichetei vine din `route_color` / `route_text_color` din feed; dacă textul nu are contrast de cel puțin 4,5:1, trece pe alb sau negru, care dintre ele contrastează mai bine. Nu se folosesc culorile sau logo-ul Tursib.
- **Why:** Alegerea utilizatorului. Aplicația nu e oficială Tursib și nu trebuie să pară; orele trebuie citite repede, ca pe panou.
- **Rejected:** stil de panou pe toată interfața; culorile Tursib.
- **Evidence:** răspunsul utilizatorului la designul din 2026-11-19 (după el, feed-ul Tursib are route_color / route_text_color; neverificat încă în feed)
- **Source:** user · confidence high
- **Owner note:** 2026-11-19: tokens aprobate cu corectura de contrast: fiecare culoare trece și pe --color-bg, și pe --color-surface (docs/design/DESIGN.md).

## D-030 · Harta e și la /harta, pentru toți; „Hartă” din antet duce acolo
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Completează D-022. Pagina hărții (stații, căutare, „aproape de mine”) există și la `/harta`, pentru vizitatori și pentru utilizatorii logați. Linkul „Hartă” din antet duce la `/harta`. Pagina „/” rămâne ca în D-022.
- **Why:** Răspunsul utilizatorului: un utilizator logat vede „Acasă” la „/” și avea nevoie de o cale spre hartă.
- **Evidence:** răspunsul utilizatorului la recapitularea din 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-034 · „Aproape de mine”: cele mai apropiate 3 stații, pe o rază de maxim 1 km
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Butonul „aproape de mine” de pe hartă arată cele mai apropiate 3 stații (D-031) aflate la cel mult 1 km de locația utilizatorului. Dacă nu e niciuna, pagina arată un mesaj.
- **Why:** Răspunsul utilizatorului; mock-ul DS-autobuze-harta arăta deja 3 stații.
- **Evidence:** răspunsul utilizatorului la recapitularea din 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-039 · Tema: după sistem, cu un buton light/dark în antet, ținut în browser
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Tema urmează sistemul (`prefers-color-scheme`). Un buton din antet comută între light și dark; alegerea se ține în browser (`localStorage`) și are prioritate față de sistem. Butonul are două stări; nu există o opțiune „înapoi la sistem”.
- **Why:** Răspunsul utilizatorului la /plan pentru scheletul proiectului.
- **Rejected:** doar după sistem, fără buton; trei stări (sistem / light / dark).
- **Evidence:** răspunsul utilizatorului, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-041 · Numele aplicației: „Staționar — când vine autobuzul?”, scurt „Staționar”
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Numele aplicației e „Staționar — când vine autobuzul?”; numele scurt, „Staționar”, se folosește unde e loc puțin. Antetul arată mereu „Staționar”; `<title>`, `<h1>` și README folosesc numele complet. Titlul proiectului din catalog, P07 „Când vine autobuzul?”, rămâne acolo unde e numit proiectul. Mock-urile confirmate rămân `approved`: se schimbă doar textul aprobat prin această decizie, nu designul.
- **Why:** Alegerea autorului; „Staționar” e un joc de cuvinte voit (stație + „-ar”). Numele complet nu încape în antet la 360 px lângă iconiță și butonul de temă.
- **Rejected:** antetul cu numele complet de la 600 px în sus și scurt pe mobil; păstrarea numelui „Când vine autobuzul?”.
- **Evidence:** răspunsul autorului, 2026-11-19 (înlocuiește răspunsul 1 la mock-ul DS-autobuze-statie: „Numele rămâne «Când vine autobuzul?»”)
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-042 · Direcția vizuală: bază ușor colorată „rece”, umbre moi și mișcare lină; orele rămân de contrast maxim
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Înlocuiește D-027. Interfața are o bază ușor colorată, varianta „rece” (cer + accent azur): fundal și suprafețe nuanțate, în dark fără negru pur. Rămân din D-027: un singur accent, folosit doar pentru acțiuni; stilul de panou de stație doar la plecări și linii (ore mari, monospace; numărul liniei în eticheta colorată din `route_color` / `route_text_color`, cu regula de contrast); fără culorile sau logo-ul Tursib. Orele de plecare au o culoare proprie de contrast maxim (`--color-time`) și nu au nicio animație. Cardurile și butoanele au umbre moi, stratificate; butoanele și cardurile au hover și focus animate, cardurile apar cu un fade scurt, iar tema se comută lin; toate se opresc la `prefers-reduced-motion`. Contrastul rămâne: text ≥ 4,5:1 și borduri ≥ 3:1, pe bg și pe surface, în ambele teme.
- **Why:** Alegerea autorului: un design mai viu și mai primitor, fără să schimbe organizarea și poziția elementelor; „varianta bună e cea care rămâne ușor de citit dintr-o privire în stație”.
- **Rejected:** varianta „cald” (nisip + teracotă); varianta „salvie” (verde pal + petrol); baza neutră din D-027.
- **Evidence:** `docs/design/mocks/DS-autobuze-statie.variante.html` (coloana „B · Rece”, cu valorile light și dark și contrastul calculat în pagină); răspunsul autorului, 2026-11-19
- **Source:** user · confidence high
- **Owner note:** 2026-11-19: aprobată și propunerea de tokens (culorile variantei „rece”, `--color-time`, `--shadow-card`, `--shadow-card-hover`, `--shadow-button`, `--shadow-button-hover`, `--ease-out` și regulile de mișcare). Până la /plan-ul care le aplică, DESIGN.md și `tokens.css` rămân neschimbate (altfel pică TokensTest); valorile sunt în mock-ul de mai sus.
- **Confirmed:** 2026-11-19


## D-049 · Subsolul spune că aplicația e neoficială, lângă atribuirea Tursib
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Completează D-008. Subsolul fiecărei pagini afișează, lângă „Date de transport: Tursib S.A.”, textul „Aplicație neoficială. Nu este afiliată cu Tursib S.A.”. Cu el, paginile reale nu mai au nevoie de `noindex`; pagina temporară „hello” îl păstrează până la hartă (D-022).
- **Why:** Răspunsul utilizatorului la Q-022: un motor de căutare sau un asistent AI nu trebuie să prezinte aplicația drept serviciul Tursib.
- **Rejected:** `noindex` pe paginile reale până la un răspuns.
- **Evidence:** răspunsul utilizatorului la /plan pentru punctul 8, 2026-11-25
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-050 · Paginile cu plecări se randează pe server; același serviciu dă endpoint-ul JSON
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Completează D-001. Paginile cu plecări (pagina de stație; mai târziu „Acasă”) se randează pe server, cu Thymeleaf, fără JS pentru date. Același serviciu dă și endpoint-ul JSON (C-002), pentru hartă și alți consumatori. Stările „se încarcă” și „eroare cu Reîncearcă” din mock nu se aplică acestor pagini.
- **Why:** Răspunsul utilizatorului la /plan pentru punctul 8: mai puțin cod și mai puține teste decât o pagină care citește JSON-ul din browser.
- **Rejected:** pagina goală care citește endpoint-ul JSON cu JS.
- **Evidence:** răspunsul utilizatorului la /plan pentru punctul 8, 2026-11-25
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25
