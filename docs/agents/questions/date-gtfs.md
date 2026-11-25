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
- **Status:** answered 2026-11-24
- **Blocks:** statutul licenței în DATA.md
- **Who answers:** utilizatorul
- **Answer:** Nu se mai cere confirmarea: autorul a renunțat la emailul către Tursib, pentru că nu e cerut de catalog. Licența rămâne cum e descrisă în DATA.md: nedeclarată în feed, folosire necomercială, educațională, cu atribuire „Tursib S.A.” (D-008).
- **Recorded as:** DATA.md → Licență

## Q-010 · Feed-ul Tursib conține shapes.txt?
- **Status:** answered 2026-11-25
- **Blocks:** vizualizarea traseelor: forma reală de pe hartă sau linii drepte între stații
- **Who answers:** utilizatorul, la descărcarea feed-ului
- **Answer:** Da. Arhiva `tursib.gtfs_2026-10-v4.9.zip` (descărcată 2026-11-25, SHA-256 `cb0ddad9…aa9f91`) are `shapes.txt` cu 84.954 de puncte, iar `trips.txt` are `shape_id` completat. Traseele se desenează după forma reală. Utilizatorul a cerut închiderea întrebării după verificare.
- **Recorded as:** DATA.md → Sursă

## Q-019 · Ce identificator stabil folosim pentru stații: stop_code sau stop_id?
- **Status:** answered 2026-11-25
- **Blocks:** D-020 (rapoarte și favorite care supraviețuiesc versiunilor); depinde dacă feed-ul Tursib are stop_code și dacă stop_id rămâne la fel între versiuni
- **Who answers:** utilizatorul, după descărcarea feed-ului
- **Answer:** `stop_id`, confirmat de utilizator pe 2026-11-25. `stop_key` (D-020, D-044) = `stop_id`. În arhiva din 2026-11-25 (v4.9), `stops.txt` nu are deloc coloana `stop_code` (0 din 512 rânduri), iar `parent_station` e gol la toate. `stop_id` e numeric (ex. `1051` = Gară) și e folosit în adresele publice ale Tursib (`stop_url` = `https://tursib.ro/statii/<stop_id>`), deci e de așteptat să rămână același între versiuni. Stabilitatea între două versiuni nu e verificată încă: se compară la primul re-import.
- **Recorded as:** aici; D-020 și D-044 trimit la Q-019 Consecință pentru D-031: fără `parent_station`, gruparea stațiilor se face după nume și distanță.
