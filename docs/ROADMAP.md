# ROADMAP · P07 „Când vine autobuzul?”

> **Pentru agent:** acesta e un plan de orientare: CE urmează și CÂND. Nu e un plan de implementare și nu conține decizii.
> - Fiecare bucată de mai jos începe cu `/plan`, separat. Planul tău decide CUM se face.
> - Detaliile tehnice de aici (de ex. „tabel de transferuri precalculat”) sunt sugestii. Dacă nu există deja o decizie D- aprobată, le propui ca PROPOSAL și aștepți aprobarea.
> - Un `/plan` acoperă o singură bucată din roadmap, nu mai multe.
> - Starea task-urilor (bifat / nebifat) e în `TODO.md`, nu aici.

## Termene
- **Livrare finală: joi, 3 decembrie 2026, 23:59** (ora de confirmat în Classroom).
- Checkpoint 1 (oficial P1): duminică, 22.11.2026, 23:59.
- Checkpoint 2 (intern): duminică, 29.11.2026.
- 30.11 și 01.12 sunt sărbători legale (zile libere). Sunt utile și ca test real pentru `calendar_dates`.

## Checkpoint 1 · du 22.11 (oficial)
1. Deploy „hello” pe Render + Neon, pe HTTPS.
2. Schema DB: migrarea cu tabelele GTFS + utilizatori, favorite, rapoarte, versiuni de feed + diagramă.
3. FIELD.md + field/observations.csv + pozele panourilor; cei 3 navetiști; emailul către Tursib.
4. README: scope, stack, variabile personale; TODO.md.

## Checkpoint 2 · du 29.11 (intern)
5. Import GTFS: job de descărcare cu versiune + checksum, insert în batch, tranzacțional, indecși expliciți; mini-feed de test.
6. Feed-ul real importat local și pe live; durata și numărul de rânduri documentate în DATA.md.
7. Twist 1+2: serviciile active într-o zi (calendar + calendar_dates) și orele ≥ 24:00. Teste pe mini-feed.
8. Pagina de stație: „următoarele plecări”, sub 300 ms pe feed-ul real, măsurat și documentat.
9. Harta Leaflet cu date din API, căutare după nume, „aproape de mine”.
10. Auth cu USER + ADMIN, rute protejate pe server; favorite + pagina „acasă”.
11. Pagina de linie: traseul pe hartă, stațiile în ordine, orarul pe tipuri de zi.

## Livrare · jo 03.12
12. (lu 30.11) Twist 3: planificatorul cu maxim un transfer (stații comune sau ≤ 150 m), sub 1 s, teste pe mini-feed.
13. (ma 01.12) Abateri raportate (media pe intervale orare, seed din observațiile de teren); admin: import, raport de diferențe, moderare.
14. (mi 02.12) Minim 10 teste (minim 4 pe twist), CI verde; API docs + CONTRACTS C-; README final; AI_LOG ≥ 10; SUBMISSION.md.
15. (jo 03.12) Video demo, verificare pe o clonă nouă, căutare de secrete, predare.

## Definition of Done (fișa P07)
- Feed-ul real e importat pe live; o stație reală arată plecările corecte acum (verificat cu panoul din stație).
- Cursele de după miezul nopții și excepțiile de calendar sunt corecte (teste pe mini-feed).
- Planificatorul găsește o rută cu transfer între 2 stații reale în sub 1 secundă.
- Re-importul produce un raport de diferențe.
- FIELD.md are cele 20 de observații, vizibile ca abateri în aplicație.

## Dacă rămânem în urmă
Tăiem, în ordinea asta: polish UI → orarul complet pe tipuri de zi → detaliile raportului de diferențe. Ce nu tăiem niciodată: deploy-ul live, testele de twist, datele de teren.
