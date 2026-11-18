# Open questions — feed, licență, identificatori

## Q-001 · Ce oraș și ce feed GTFS (URL, licență, conține shapes.txt)?
- **Status:** answered 2026-11-18
- **Blocks:** importul, datele de test, vizualizarea traseelor
- **Who answers:** utilizatorul
- **Answer:** Sibiu, operator Tursib S.A. Feed oficial: https://tursib.ro/trasee/gtfs ; copie de rezervă: https://mobilitydatabase.org/feeds/gtfs/mdb-2099 . Licența: vezi DATA.md și Q-009. Dacă există `shapes.txt`: Q-010.
- **Recorded as:** DATA.md, CONTRACTS.md (External)

## Q-002 · Ce face un import nou cu datele existente?
- **Status:** answered 2026-11-18
- **Blocks:** designul importului
- **Who answers:** utilizatorul
- **Answer:** Re-importul creează o versiune nouă a feed-ului, într-o singură tranzacție (tot sau nimic), și produce raportul de diferențe cerut de catalog (stații noi / dispărute, trasee schimbate).
- **Recorded as:** D-003

## Q-003 · Următoarele plecări vin doar din orar (static) sau și din GTFS-Realtime?
- **Status:** answered 2026-11-18
- **Blocks:** sursa pentru următoarele plecări
- **Who answers:** utilizatorul
- **Answer:** Doar orarul static GTFS; GTFS-Realtime e în afara scopului. Abaterile vin din rapoartele utilizatorilor și din observațiile de teren ale autorului.
- **Recorded as:** D-004

## Q-009 · Confirmă Tursib folosirea datelor GTFS în acest proiect?
- **Status:** open
- **Blocks:** statutul licenței în DATA.md
- **Who answers:** utilizatorul, după răspunsul Tursib la cererea către office@tursib.ro (data trimiterii: de completat)

## Q-010 · Feed-ul Tursib conține shapes.txt?
- **Status:** open
- **Blocks:** vizualizarea traseelor: forma reală de pe hartă sau linii drepte între stații
- **Who answers:** utilizatorul, la descărcarea feed-ului

## Q-019 · Ce identificator stabil folosim pentru stații: stop_code sau stop_id?
- **Status:** open
- **Blocks:** D-020 (rapoarte și favorite care supraviețuiesc versiunilor); depinde dacă feed-ul Tursib are stop_code și dacă stop_id rămâne la fel între versiuni
- **Who answers:** utilizatorul, după descărcarea feed-ului
