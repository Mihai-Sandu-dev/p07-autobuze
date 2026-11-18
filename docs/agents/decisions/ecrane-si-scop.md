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
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Interfața are o bază neutră (alb / gri închis) și un singur accent, folosit doar pentru acțiuni. Stilul de panou de stație apare doar unde contează: orele de plecare sunt mari, cu cifre monospace, iar numărul liniei stă într-o etichetă colorată. Culoarea etichetei vine din `route_color` / `route_text_color` din feed; dacă textul nu are contrast de cel puțin 4,5:1, trece pe alb sau negru, care dintre ele contrastează mai bine. Nu se folosesc culorile sau logo-ul Tursib.
- **Why:** Alegerea utilizatorului. Aplicația nu e oficială Tursib și nu trebuie să pară; orele trebuie citite repede, ca pe panou.
- **Rejected:** stil de panou pe toată interfața; culorile Tursib.
- **Evidence:** răspunsul utilizatorului la designul din 2026-11-19 (după el, feed-ul Tursib are route_color / route_text_color; neverificat încă în feed)
- **Source:** user · confidence high
- **Owner note:** 2026-11-19: tokens aprobate cu corectura de contrast: fiecare culoare trece și pe --color-bg, și pe --color-surface (docs/design/DESIGN.md).
- **Confirmed:** 2026-11-19
