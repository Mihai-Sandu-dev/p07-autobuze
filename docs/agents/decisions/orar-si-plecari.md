# Decisions — tipuri de zi, sărbători, următoarele plecări

## D-014 · Tipuri de zi: lucrătoare, sâmbătă, duminică și sărbătoare legală
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Aplicația folosește trei tipuri de zi: lucrătoare, sâmbătă, duminică sau sărbătoare legală. O sărbătoare legală se tratează ca duminică. Tipurile de zi sunt folosite în orarul complet de pe pagina liniei și în media abaterilor.
- **Why:** Răspunsul utilizatorului; fișa P07 cere „orarul complet pe tipuri de zi”.
- **Rejected:** două tipuri (lucrătoare / weekend); tipuri derivate din service_id-urile feed-ului.
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-015 · Lista sărbătorilor legale e în DB, doar pentru eticheta zilei și media abaterilor
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Un tabel din DB conține sărbătorile legale românești pentru 2026–2027, inclusiv Paștele și Rusaliile ortodoxe; se completează printr-o migrare Flyway. Lista dă doar eticheta tipului de zi (D-014) și intră în media abaterilor. Plecările se calculează numai din calendar.txt și calendar_dates.txt; lista nu le modifică niciodată.
- **Why:** Răspunsul utilizatorului. Feed-ul decide ce curse circulă; lista doar clasifică ziua.
- **Rejected:** sărbători deduse doar din excepțiile din calendar_dates.txt.
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-018 · Următoarele plecări: primele 3 pe fiecare linie și sens, completate cu ziua următoare
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Pagina stației arată, pentru fiecare linie și sens, următoarele 3 plecări de la ora curentă. Cursele de după miezul nopții ale zilei de serviciu anterioare (ore ≥ 24:00:00) intră în calcul. Dacă azi mai sunt mai puțin de 3, lista se completează cu plecările zilei următoare, după calendarul acelei zile, marcate „mâine”.
- **Why:** Răspunsul utilizatorului; twist-ul (2) din fișa P07.
- **Rejected:** toate plecările din următoarele 60 de minute; maxim 2 ore; doar ziua curentă.
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-26

## D-047 · Orele din feed sunt ora de pe ceas, ca pe panoul din stație
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Completează D-043. O oră din feed (de ex. `24:40:00` în ziua de serviciu 25.11) se citește ca ora locală de pe ceas (Europe/Bucharest), adunată la miezul nopții zilei de serviciu: 26.11, 00:40. Nu se folosește referința GTFS „prânz minus 12 ore”. Plecările se compară cu ora locală de pe ceas.
- **Why:** Răspunsul utilizatorului la /plan pentru punctul 7: așa apar orele pe panourile Tursib. Diferența față de referința GTFS apare doar în cele două zile pe an în care se schimbă ora.
- **Rejected:** referința GTFS „prânz minus 12 ore” (ar muta plecările cu o oră în zilele cu schimbarea orei, față de panou).
- **Evidence:** răspunsul utilizatorului la /plan pentru punctul 7, 2026-11-25
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-048 · Următoarele plecări nu arată cursele care se termină în stație
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Completează D-018. Lista „Următoarele plecări” a unei stații nu conține o cursă în stația în care cursa se termină, adică la ultimul `stop_sequence` al cursei, chiar dacă `stop_times` îi dă o oră de plecare acolo. Regula se aplică la pagina de stație (punctul 8). `Departures` (punctul 7) rămâne neschimbat; filtrul se adaugă la punctul 8.
- **Why:** Răspunsul utilizatorului la Q-024: așa arată panoul din stație; din capăt, cursa nu mai pleacă nicăieri.
- **Rejected:** afișarea tuturor rândurilor din `stop_times`, inclusiv capătul cursei.
- **Evidence:** răspunsul utilizatorului la Q-024, 2026-11-25
- **Source:** user · confidence high
- **Confirmed:** 2026-11-26


## D-051 · Un grup din „Următoarele plecări” = linie + capăt (trip_headsign)
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Completează D-018. Un grup din lista „Următoarele plecări” e o linie (`route_short_name`) cu un capăt (`trip_headsign`), ca pe autobuz (CONTEXT.md → Sens). O cursă scurtată, cu alt capăt pe același `direction_id`, are grupul ei, cu primele 3 plecări ale lui.
- **Why:** Răspunsul utilizatorului la /plan pentru punctul 8.
- **Rejected:** un grup pe linie și `direction_id`, cu capătul cel mai des întâlnit ca titlu.
- **Evidence:** răspunsul utilizatorului la /plan pentru punctul 8, 2026-11-25
- **Source:** user · confidence high
- **Confirmed:** 2026-11-26
