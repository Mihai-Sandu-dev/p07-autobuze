# Open questions — rapoarte, date de teren, interviuri

## Q-011 · Rapoartele utilizatorilor despre abateri sunt o funcție a aplicației?
- **Status:** answered 2026-11-18
- **Blocks:** orice funcție de raportare a abaterilor (D-004 spune doar că abaterile vin din rapoarte și observații de teren)
- **Who answers:** utilizatorul
- **Answer:** Da, cerută de catalog: utilizatorii logați raportează o abatere (linie, stație, oră); pagina liniei arată abaterea medie pe intervale orare; adminul moderează; observațiile din FIELD.md intră ca seed.
- **Recorded as:** D-009

## Q-013 · Unde e FIELD.md și ce format au observațiile de teren?
- **Status:** answered 2026-11-18
- **Blocks:** seed-ul observațiilor de teren în rapoartele de abatere (D-009); FIELD.md nu există încă în repo
- **Who answers:** utilizatorul
- **Answer:** `FIELD.md` în root, lângă `DATA.md`; observațiile în `field/observations.csv` (antet `data,ora_reala,statie,linie,sens,ora_feed,abatere_min,observatii,poza`); pozele în `field/photos/<data>/`, ~1600 px. FIELD.md: alegerea liniilor și stațiilor, link la CSV, poze, „Interviuri navetiști”. Datele le completează autorul.
- **Recorded as:** D-010

## Q-015 · Ce format au valorile din field/observations.csv?
- **Status:** answered 2026-11-19
- **Blocks:** seed-ul din observații (D-009, D-010): formatul pentru `data` și ore, valorile pentru `sens`, ce conține `poza` (cale sau nume de fișier)
- **Who answers:** utilizatorul
- **Answer:** În CSV și în DB: data YYYY-MM-DD, ora HH:MM, abaterea întreg cu semn în minute (+4, -1, 0). Formatul românesc rămâne doar în xlsx. Coloana `poza`: Q-021.
- **Recorded as:** D-024

## Q-016 · Cum apar observațiile de teren în aplicație față de rapoartele utilizatorilor?
- **Status:** answered 2026-11-19
- **Blocks:** seed-ul (D-009): etichetă „observație de teren” cu link spre FIELD.md, sau raport obișnuit al unui cont de sistem
- **Who answers:** utilizatorul
- **Answer:** Ca rapoarte de abatere cu sursa „teren”, în aceeași medie; vizibile ca abateri în aplicație (Definition of Done).
- **Recorded as:** D-025

## Q-017 · Câte poze cu panouri există și de la ce stații?
- **Status:** answered 2026-11-26
- **Blocks:** FIELD.md și `field/photos/` (D-010). În interviu: „6 poze”, apoi „poze și la Casa Armatei, Andrei Șaguna, Poliție”. Foaia „Stații” are 6 stații (fără acestea 3); nume de fișier doar la Metro (`118_Metro_17-11-2026_07-18`, fără extensie); la Metro scrie doar linia 118, deși observația 20 e cu linia 11. Pozele nu sunt încă în repo.
- **Who answers:** utilizatorul (completează foaia „Stații”)
- **Answer:** 8 poze din 2026-11-17, de la toate cele 6 stații din foaia „Stații” (Metro, Teatru, Piața Cibin 3, Zidul Cetății, Turnișor, Ambient 1; câte 2 la Piața Cibin 3 și Zidul Cetății), în `field/photos/2026-11-17/`, legate din FIELD.md și din coloana `poza`. Catalogul cere poze la minim 6 stații, deci Casa Armatei, Andrei Șaguna și Poliție nu au nevoie de poze. A 9-a poză (Teatru, ecran stins, cu reflexiile a două persoane) nu intră în repo.

## Q-018 · Când sunt gata celelalte 2 interviuri cu navetiști?
- **Status:** answered 2026-11-26
- **Blocks:** secțiunea „Interviuri navetiști” din FIELD.md; fișa P07 cere 3, foaia „Navetiști” are 1 (L.B., 2026-11-18)
- **Who answers:** utilizatorul
- **Answer:** Gata: foaia „Navetiști” din fișa actualizată are 3 discuții (L.B. 2026-11-18, H. 2026-11-19, S. B 2026-11-26), trecute în FIELD.md exact cum sunt scrise.

## Q-020 · Unde stă tabelul de corespondență teren → feed și ce coloane are?
- **Status:** open
- **Blocks:** seed-ul (D-021)
- **Who answers:** utilizatorul, la /plan

## Q-021 · Ce conține coloana `poza` din field/observations.csv?
- **Status:** answered 2026-11-19
- **Blocks:** legătura dintre o observație și poza panoului (D-010): cale relativă, nume de fișier sau gol
- **Who answers:** utilizatorul
- **Answer:** Calea relativă a pozei cu panoul (ex. `field/photos/2026-11-17/2026-11-17_metro_panou.jpg`, în subfolderul zilei din D-010) sau gol dacă observația nu are poză.
- **Recorded as:** D-026

## Q-023 · Când adminul șterge un raport de abatere, rândul dispare sau se marchează ca șters?
- **Status:** answered 2026-11-24
- **Blocks:** moderarea rapoartelor (D-016)
- **Who answers:** utilizatorul
- **Answer:** Ștergere definitivă (DELETE), fără marcare.
- **Recorded as:** D-045

