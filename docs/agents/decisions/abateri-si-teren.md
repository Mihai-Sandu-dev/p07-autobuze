# Decisions — rapoarte de abatere și date de teren

## D-009 · Rapoartele de abatere: utilizatorii logați raportează, adminul moderează
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Un utilizator logat raportează o abatere pentru o linie, într-o stație, la o oră (ex. „a venit cu 7 minute întârziere”). Pagina liniei arată abaterea medie raportată, pe intervale orare. Adminul moderează rapoartele. Observațiile de teren din FIELD.md intră ca seed în aceleași date.
- **Why:** Cerință din catalog. Completează D-004: abaterile vin din rapoarte, nu din GTFS-Realtime.
- **Rejected:** rapoarte anonime (doar utilizatori logați); un depozit separat pentru observațiile de teren.
- **Evidence:** răspunsul utilizatorului la Q-011, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-010 · Datele de teren: FIELD.md în root, observațiile în field/observations.csv
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** `FIELD.md` stă în root, lângă `DATA.md`, și conține: de ce au fost alese liniile și stațiile, linkul la CSV, pozele și secțiunea „Interviuri navetiști”. Observațiile stau în `field/observations.csv`, cu antetul `data,ora_reala,statie,linie,sens,ora_feed,abatere_min,observatii,poza`. Pozele stau în `field/photos/<data>/`, micșorate la ~1600 px. Datele de teren le completează doar autorul: un agent nu inventează observații, interviuri sau poze.
- **Why:** Răspunsul utilizatorului la Q-013. Observațiile sunt seed pentru rapoartele de abatere (D-009), deci formatul trebuie fixat.
- **Rejected:** observații direct în FIELD.md, fără CSV.
- **Evidence:** răspunsul utilizatorului la Q-013, 2026-11-18 (fișierele nu există încă)
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-016 · Abaterea medie pe linie: pe intervale de o oră, separat pe tip de zi
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Pagina liniei arată abaterea medie raportată pe intervale de o oră (07:00–07:59 ...), separat pentru fiecare tip de zi (D-014). Un raport intră în medie imediat ce e trimis. Adminul poate șterge un raport greșit; un raport șters iese din medie.
- **Why:** Răspunsurile utilizatorului: moderare după publicare, nu înainte.
- **Rejected:** aprobare înainte de publicare; media pe toate zilele; doar ultimele 30 de zile.
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-017 · Raportul de abatere: utilizatorul alege o plecare din orar și scrie ora reală
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Din pagina stației, utilizatorul logat alege o plecare din orar (linie, sens, ora din feed) și scrie ora reală de sosire. Aplicația calculează abaterea în minute: ora reală minus ora din feed (pozitiv = întârziere), ca în fișa de teren.
- **Why:** Răspunsul utilizatorului; aceeași formulă ca în Date_Teren_Autobuze.xlsx (H1).
- **Rejected:** abaterea scrisă direct în minute.
- **Evidence:** interviul de scope, 2026-11-19; field/Date_Teren_Autobuze.xlsx, foaia Observații, comentariul H1
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-028 · Raportul de abatere pornește din grupul de linie și sens; doar plecări din ultimele 60 de minute
- **Status:** proposed
- **Date:** 2026-11-19
- **Rule:** Completează D-017. Pe pagina stației, utilizatorul logat vede un singur buton „Raportează o abatere” pe fiecare grup de linie și sens, nu un link pe fiecare plecare. Vizitatorul nu vede butonul, doar rândul cu autentificarea (D-009). Formularul arată plecările acelei linii și acelui sens din stație, cu ora din feed în ultimele 60 de minute; utilizatorul alege una și scrie ora reală. Plecările viitoare nu se pot raporta.
- **Why:** Răspunsul utilizatorului la mock-ul DS-autobuze-statie; înlocuiește răspunsul anterior (link pe fiecare plecare), care permitea raportarea unor plecări care nu au avut loc încă.
- **Rejected:** link „Raportează” pe fiecare plecare.
- **Evidence:** răspunsul utilizatorului, 2026-11-19; docs/design/mocks/DS-autobuze-statie.html
- **Source:** user · confidence high

## D-021 · Corespondența dintre datele de teren și feed: un tabel completat de autor
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Observațiile de teren rămân scrise ca pe teren (stație, linie, sens). Un tabel de corespondență separat, completat de autor după ce vede feed-ul, leagă fiecare nume de pe teren de stop_id, route_id și sens (direction_id). Seed-ul folosește doar acest tabel; un rând fără corespondență nu se încarcă și apare în raportul seed-ului. Locul și formatul tabelului: Q-020.
- **Why:** Răspunsul la Q-015. Numele de pe teren nu sunt unice în feed (aceeași stație are două sensuri).
- **Rejected:** potrivire automată după nume.
- **Evidence:** interviul de scope, 2026-11-19; field/Date_Teren_Autobuze.xlsx, foaia Instrucțiuni (A11)
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-024 · Formatul datelor de teren în CSV și DB: dată YYYY-MM-DD, oră HH:MM, abatere întreg cu semn
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** În `field/observations.csv` și în DB, data se scrie YYYY-MM-DD și ora HH:MM. Abaterea e un întreg cu semn, în minute (+4, -1, 0). Formatul românesc (17.11.2026) rămâne doar în `field/Date_Teren_Autobuze.xlsx`.
- **Why:** Răspunsul la Q-015: un format neambiguu, ușor de citit de seed.
- **Rejected:** formatul românesc din xlsx și în CSV.
- **Evidence:** răspunsul utilizatorului la Q-015, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-025 · Observațiile de teren intră ca rapoarte de abatere cu sursa „teren”
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Seed-ul încarcă observațiile din `field/observations.csv` ca rapoarte de abatere obișnuite, cu sursa „teren”. Ele intră în aceeași medie ca rapoartele utilizatorilor (D-016) și sunt vizibile ca abateri în aplicație.
- **Why:** Răspunsul la Q-016; Definition of Done din fișa P07: „FIELD.md are cele 20 de observații și sunt vizibile ca abateri în aplicație”.
- **Rejected:** un depozit separat pentru observațiile de teren; rapoarte fără sursă, ale unui cont de sistem.
- **Evidence:** răspunsul utilizatorului la Q-016, 2026-11-19; SCOPE.md → Definition of done
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-026 · Coloana `poza`: calea relativă a pozei cu panoul stației, sau goală
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** În `field/observations.csv`, coloana `poza` conține calea relativă (de la rădăcina repo-ului) a pozei cu panoul stației, în subfolderul zilei din D-010, de ex. `field/photos/2026-11-17/2026-11-17_metro_panou.jpg`. Dacă observația nu are poză, coloana rămâne goală.
- **Why:** Răspunsul la Q-021: seed-ul și FIELD.md leagă observația de poză fără să ghicească numele fișierului.
- **Rejected:** doar numele fișierului, fără cale.
- **Evidence:** răspunsul utilizatorului la Q-021, 2026-11-19
- **Source:** user · confidence high
- **Owner note:** 2026-11-19: pozele rămân în subfolderul zilei (D-010), nu direct în `field/photos/`.
- **Confirmed:** 2026-11-19
