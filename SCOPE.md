# SCOPE — P07 „Când vine autobuzul?”

> Ce intră în proiect și ce nu. Sursa: fișa P07 din catalog, interviul din 2026-11-19
> și `docs/agents/`. Regulile detaliate sunt în decizii (D-); ce nu știm e în
> întrebări (Q-). Un punct marcat așteaptă aprobarea; nu se construiește
> pe el până atunci.

## Pe scurt
Numele aplicației: „Staționar — când vine autobuzul?”, scurt „Staționar” (D-041). Titlul proiectului din catalog
rămâne P07 „Când vine autobuzul?”.

Aplicație web pentru transportul public din Sibiu, pe feed-ul GTFS al Tursib S.A.
(`DATA.md`): import complet, căutare de stații pe hartă, următoarele plecări,
vizualizarea traseelor, un planificator cu maxim un transfer, favorite și abateri
raportate de utilizatori. Fișa: categoria B, dificultate ★★★★★, ~45 h, 1 student.

## Variabilele proiectului
| Variabilă | Valoare | Unde |
|---|---|---|
| Orașul și feed-ul | Sibiu, Tursib S.A., https://tursib.ro/trasee/gtfs | `DATA.md`, Q-001 |
| Distanța maximă pentru transfer pe jos | 150 m, în linie dreaptă | D-012 |
| Timpul minim de transfer | 2 min + mersul pe jos la 1,2 m/s (150 m ≈ 2 min) | D-012 |
| Traseele observațiilor de teren | liniile 2, 11, 19, 21, 24, 118 | `field/Date_Teren_Autobuze.xlsx` |

## Roluri
| Rol | Poate |
|---|---|
| Vizitator (nelogat) | harta, căutarea de stații, pagina de stație, pagina de linie, planificatorul |
| USER (oricine se înregistrează) | în plus: favorite, pagina „Acasă”, rapoarte de abatere (D-011, D-009) |
| ADMIN | în plus: import feed, raportul de diferențe, ștergerea rapoartelor greșite; primul admin vine din `.env` (D-011) |

## Ecrane
| Ecran | Ce arată | Reguli |
|---|---|---|
| `/` pentru vizitator; `/harta` pentru toți | harta cu stații, căutare după nume și „aproape de mine” (geolocația din browser; cele mai apropiate 3 stații în 1 km) | D-022, D-030, D-034, D-005 |
| `/` pentru utilizator logat („Acasă”) | plecările de la stațiile favorite, harta dedesubt | D-022 |
| Pagina de stație | următoarele 3 plecări pe fiecare linie și sens, cu excepțiile de calendar; un buton de raport de abatere pe fiecare grup de linie și sens | D-018, D-017, D-028 |
| Pagina de linie | traseul pe hartă, stațiile în ordine, orarul complet pe tipuri de zi, abaterea medie pe intervale de o oră | D-014, D-016; Q-010 (shapes) |
| Planificator | de la stația A la stația B, acum sau la o dată și oră alese (în perioada de valabilitate a feed-ului; continuă cu ziua următoare); primele 3 itinerarii: plecare, sosire, linii, stația de transfer, timpul de așteptare | D-002, D-012, D-013, D-033 |
| Înregistrare, login | email și parolă | D-011 |
| Admin: import | pornește jobul; versiuni și checksum-uri | D-003, D-019 |
| Admin: raport de diferențe | stații noi, stații dispărute, trasee schimbate | D-003 |
| Admin: rapoarte | lista rapoartelor; ștergerea unui raport greșit | D-016 |
| Pagina de eroare | o pagină comună pentru 403, 404 și 500, fără detalii tehnice | DS-autobuze-eroare |
| Toate paginile | subsol „Date de transport: Tursib S.A.”; atribuirea OSM pe hartă | D-008, D-005 |

Designul fiecărui ecran se face în pasul următor (`docs/design/`).

## Date
- **Feed:** GTFS Schedule, doar static (D-004). Import complet: agency, routes, trips,
  stops, stop_times, calendar, calendar_dates, shapes dacă există (Q-010).
- **Job:** zilnic și la cererea adminului; checksum; versiune nouă doar la feed nou;
  import tranzacțional; noua versiune e activă imediat; se păstrează 2 versiuni
  (D-003, D-019).
- **Date ale utilizatorilor** (rapoarte, favorite): legate de identificatori stabili,
  nu de o versiune (D-020; Q-019).
- **Tipuri de zi:** lucrătoare, sâmbătă, duminică sau sărbătoare legală (D-014). Lista
  sărbătorilor din DB dă doar eticheta zilei și intră în media abaterilor; plecările
  vin numai din calendar.txt și calendar_dates.txt (D-014, D-015).
- **Seed din teren:** observațiile din `field/observations.csv` intră în rapoartele de
  abatere (D-009, D-010), legate de feed printr-un tabel de corespondență completat de
  autor (D-021; Q-020). Intră ca rapoarte de abatere cu sursa „teren” (D-025).
  Format în CSV și DB: dată YYYY-MM-DD, oră HH:MM, abatere întreg cu semn (D-024).

## Cerințe tehnice (din fișă)
- Importul a 200.000+ rânduri durează minute, nu ore: insert în batch, indecși
  definiți explicit, fără ORM „un rând pe dată”.
- „Următoarele plecări” răspunde sub 300 ms pe feed-ul real, măsurat și documentat.
- Planificatorul găsește o rută cu transfer între 2 stații reale în sub 1 secundă,
  fără să scaneze toate cursele la fiecare cerere (ceva precalculat).
- Harta Leaflet citește datele din API-ul aplicației, nu din fișiere statice.

## Twist-ul: testat pe un mini-feed construit de autor
1. **Calendarul:** serviciile active într-o zi, cu intervale de valabilitate și
   excepții adăugate sau scoase (sărbători legale).
2. **Miezul nopții:** curse cu ore ca 25:10:00; la 00:30, „următoarele plecări”
   combină serviciile de azi cu cele de ieri (D-018).
3. **Transferul:** stații comune sau sub 150 m; plecări fezabile cu timpul minim de
   transfer (D-012).

## Definition of done (din fișă)
- Feed-ul real e importat pe live; o stație reală arată plecările corecte acum,
  verificat cu panoul din stație.
- Cursele de după miezul nopții și excepțiile de calendar sunt corecte (teste pe mini-feed).
- Planificatorul găsește o rută cu transfer între 2 stații reale în sub 1 secundă.
- Re-importul produce un raport de diferențe.
- FIELD.md are cele 20 de observații și ele sunt vizibile ca abateri în aplicație.

La susținere: „următoarele plecări” la 00:30 într-o zi de luni care e sărbătoare
legală; ce e precalculat pentru planificator și când se invalidează; cum s-au
importat 200.000 de rânduri și cât a durat.

## Datele de teren (din `field/Date_Teren_Autobuze.xlsx`, neschimbate)
- **Observații:** 20, din 17 și 18.11.2026, numerotate 1–12 și 14–21 (rândul 13 e gol);
  abateri între −1 și +11 minute.
- **Linii (6):** 2, 11, 19, 21, 24, 118.
- **Stații (9):** Metro, Teatru, Piața Cibin 3, Zidul Cetății, Turnișor, Ambient 1,
  Casa Armatei, Andrei Șaguna, Poliție.
- **Poze cu panouri:** foaia „Stații” are 6 stații marcate „Da”; restul e Q-017.
  Pozele nu sunt încă în repo.
- **Navetiști:** 1 din 3 (L.B., student anul 3, zilnic cu linia 2): „Să știu când
  vine următorul autobuz, să știu dacă merg pe jos sau nu. Să pot vedea întregul
  traseu al autobuzelor care trec prin stația mea.” Celelalte 2: Q-018.

## În afara scopului
- GTFS-Realtime (D-004).
- Alte orașe sau mai multe feed-uri; rutarea pe jos pe străzi; notificări push sau
  email; aplicație mobilă nativă; cumpărare de bilete; altă limbă decât româna
  (D-023).

## Încă deschis
Q-010 (shapes.txt), Q-017 (poze), Q-018 (interviuri), Q-019
(identificatorul stabil al stațiilor), Q-020 (tabelul de corespondență). Găzduirea e
Render + Neon (D-037); riscurile planurilor gratuite se rezolvă în primul /plan, înainte
de 2026-11-22.
