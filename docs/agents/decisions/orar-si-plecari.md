# Decisions — tipuri de zi, sărbători, următoarele plecări

## D-014 · Tipuri de zi: lucrătoare, sâmbătă, duminică și sărbătoare legală
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Aplicația folosește trei tipuri de zi: lucrătoare, sâmbătă, duminică sau sărbătoare legală. O sărbătoare legală se tratează ca duminică. Tipurile de zi sunt folosite în orarul complet de pe pagina liniei și în media abaterilor.
- **Why:** Răspunsul utilizatorului; fișa P07 cere „orarul complet pe tipuri de zi”.
- **Rejected:** două tipuri (lucrătoare / weekend); tipuri derivate din service_id-urile feed-ului.
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-015 · Lista sărbătorilor legale e în DB, doar pentru eticheta zilei și media abaterilor
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Un tabel din DB conține sărbătorile legale românești pentru 2026–2027, inclusiv Paștele și Rusaliile ortodoxe; se completează printr-o migrare Flyway. Lista dă doar eticheta tipului de zi (D-014) și intră în media abaterilor. Plecările se calculează numai din calendar.txt și calendar_dates.txt; lista nu le modifică niciodată.
- **Why:** Răspunsul utilizatorului. Feed-ul decide ce curse circulă; lista doar clasifică ziua.
- **Rejected:** sărbători deduse doar din excepțiile din calendar_dates.txt.
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-018 · Următoarele plecări: primele 3 pe fiecare linie și sens, completate cu ziua următoare
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Pagina stației arată, pentru fiecare linie și sens, următoarele 3 plecări de la ora curentă. Cursele de după miezul nopții ale zilei de serviciu anterioare (ore ≥ 24:00:00) intră în calcul. Dacă azi mai sunt mai puțin de 3, lista se completează cu plecările zilei următoare, după calendarul acelei zile, marcate „mâine”.
- **Why:** Răspunsul utilizatorului; twist-ul (2) din fișa P07.
- **Rejected:** toate plecările din următoarele 60 de minute; maxim 2 ore; doar ziua curentă.
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19
