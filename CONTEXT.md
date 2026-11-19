# CONTEXT — p07-autobuze vocabulary

<!-- graffino-agentic:begin v1 -->
> The approved terms for this project. Use each term exactly as defined, in the part
> of speech given. Never swap in a synonym; never use a word on an _Avoid_ line.
<!-- graffino-agentic:end -->

**Feed GTFS** (substantiv) — arhiva GTFS Schedule (ZIP) publicată pentru oraș; sursa tuturor datelor.
_Avoid_: dump, export, bază de date GTFS

**Import** (substantiv) — încărcarea completă a unui feed GTFS în baza de date a aplicației.
_Avoid_: sincronizare, sync, update de date

**Operator** (substantiv) — compania de transport din `agency.txt`.
_Avoid_: agenție, regie

**Stație** (substantiv) — locul unde călătorii urcă și coboară (`stops.txt`).
_Avoid_: oprire, haltă, stop

**Linie** (substantiv) — un serviciu cu nume sau număr, de ex. „24B” (`routes.txt`).
_Avoid_: rută, traseu

**Sens** (substantiv) — încotro merge o linie, numit după capătul de linie scris pe autobuz (`direction_id` și `trip_headsign` din `trips.txt`).
_Avoid_: direcție (pentru sensul unei linii)

**Traseu** (substantiv) — drumul unei linii într-un sens: stațiile în ordine și forma de pe hartă (`shapes.txt`).
_Avoid_: shape, itinerariu, rută

**Cursă** (substantiv) — un vehicul care parcurge un traseu o dată, la ore fixe (`trips.txt`).
_Avoid_: călătorie, tură, trip

**Plecare** (substantiv) — ora la care o cursă pleacă dintr-o stație (`stop_times.txt`).
_Avoid_: trecere, sosire (când e vorba de plecare)

**Calendar** (substantiv) — zilele în care circulă o cursă (`calendar.txt`, `calendar_dates.txt`).
_Avoid_: program, orar

**Următoarele plecări** (substantiv) — plecările viitoare dintr-o stație, de la ora curentă, după calendar.
_Avoid_: timp real, sosiri

**Planificator** (substantiv) — funcția care caută un itinerariu de la o origine la o destinație, cu maxim un transfer.
_Avoid_: router, navigator, motor de rutare

**Itinerariu** (substantiv) — rezultatul planificatorului: una sau două curse consecutive, cu stațiile de urcare și coborâre.
_Avoid_: rută, călătorie, traseu

**Transfer** (substantiv) — trecerea de pe o cursă pe alta în cadrul unui itinerariu.
_Avoid_: corespondență, schimbare

**Abatere** (substantiv) — diferența dintre ora din orar și ora reală a unei curse într-o stație, în minute.
_Avoid_: întârziere (ca termen general), deviere

**Raport de abatere** (substantiv) — o abatere cu linie, stație, oră și minute; sursa e „utilizator” (trimisă de un utilizator logat) sau „teren” (o observație de teren, D-025).
_Avoid_: sesizare, reclamație, feedback

**Observație de teren** (substantiv) — o abatere notată de autor la fața locului, în `field/observations.csv` (descrisă în FIELD.md, D-010); intră ca seed printre rapoartele de abatere, cu sursa „teren”.
_Avoid_: măsurătoare, notă

**Versiune de feed** (substantiv) — rezultatul unui import reușit, cu checksum; una singură e activă.
_Avoid_: snapshot, revizie, ediție

**Tip de zi** (substantiv) — lucrătoare, sâmbătă, sau duminică și sărbătoare legală.
_Avoid_: tip de serviciu, regim

**Favorită** (substantiv) — o stație salvată de un utilizator logat; plecările ei apar pe pagina „Acasă”.
_Avoid_: bookmark, stație preferată
