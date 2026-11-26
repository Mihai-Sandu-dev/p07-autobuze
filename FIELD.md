# FIELD — date de teren pentru P07

> Generat pe 2026-11-19 din `field/Date_Teren_Autobuze.xlsx`, cu datele exact cum sunt în
> fișă (D-010, D-024). Actualizat pe 2026-11-26 din fișa nouă (aceleași observații, plus navetiștii
> 2 și 3) și cu pozele autorului. Datele de teren le completează doar autorul: un agent nu
> inventează observații, interviuri sau poze.

## De ce aceste linii și stații
„Am ales liniile cu care circul zilnic (118 dimineața, spre facultate; 2 și 11 după-amiaza) și stații în care aceste linii se intersectează (Turnișor, Ambient 1, Teatru, Zidul Cetății), ca să observ mai multe linii în aceeași stație și să am cazuri reale de transfer pentru planificator.”

Linii (6): 2, 11, 19, 21, 24, 118. Stații (9): Metro, Teatru, Piața Cibin 3, Zidul Cetății,
Turnișor, Ambient 1, Casa Armatei, Andrei Șaguna, Poliție.

Minimurile din fișa P07: cel puțin 3 linii, 6 stații și 20 de observații (atinse: 6, 9, 20).

## Observații
Fișierul pentru seed: [`field/observations.csv`](field/observations.csv). Format: dată
YYYY-MM-DD, oră HH:MM, abatere întreg cu semn, în minute (D-024). Abaterea = ora reală − ora
din feed; pozitiv = întârziere (fișa, comentariul din H1). Toate cele 20 de abateri din CSV
au fost recalculate din ore și se potrivesc cu coloana H din fișă.

Nr. 13 lipsește din fișă (rând gol); numerotarea de mai jos e cea din fișă.

| Nr | Data | Linia | Sens | Stația | Ora reală | Ora din feed | Abatere (min) |
|---|---|---|---|---|---|---|---|
| 1 | 2026-11-17 | 118 | Gușterița | Metro | 07:23 | 07:24 | -1 |
| 2 | 2026-11-17 | 118 | Gușterița | Teatru | 07:39 | 07:35 | +4 |
| 3 | 2026-11-17 | 2 | Piața Cibin | Teatru | 14:56 | 14:45 | +11 |
| 4 | 2026-11-17 | 2 | Piața Cibin | Piața Cibin 3 | 15:11 | 15:10 | +1 |
| 5 | 2026-11-17 | 2 | Zona Industrială Est - Neveon | Piața Cibin 3 | 16:14 | 16:10 | +4 |
| 6 | 2026-11-17 | 2 | Zona Industrială Est - Neveon | Zidul Cetății | 16:36 | 16:30 | +6 |
| 7 | 2026-11-17 | 19 | Cartier Magnolia - Marquardt | Zidul Cetății | 16:40 | 16:36 | +4 |
| 8 | 2026-11-17 | 19 | Cartier Magnolia - Marquardt | Turnișor | 16:52 | 16:44 | +8 |
| 9 | 2026-11-17 | 11 | Aumovio | Turnișor | 16:56 | 16:51 | +5 |
| 10 | 2026-11-17 | 11 | Aumovio | Ambient 1 | 16:59 | 16:53 | +6 |
| 11 | 2026-11-17 | 21 | Marquardt | Turnișor | 16:56 | 16:47 | +9 |
| 12 | 2026-11-17 | 21 | Marquardt | Ambient 1 | 16:58 | 16:49 | +9 |
| 14 | 2026-11-18 | 118 | Gușterița | Metro | 07:24 | 07:24 | 0 |
| 15 | 2026-11-18 | 118 | Gușterița | Casa Armatei | 07:41 | 07:33 | +8 |
| 16 | 2026-11-18 | 24 | A. Șaguna | Casa Armatei | 07:43 | 07:34 | +9 |
| 17 | 2026-11-18 | 24 | A. Șaguna | Andrei Șaguna | 07:46 | 07:36 | +10 |
| 18 | 2026-11-18 | 11 | Aeroport Terminal | Poliție | 10:03 | 10:02 | +1 |
| 19 | 2026-11-18 | 11 | Aeroport Terminal | Ambient 1 | 10:14 | 10:13 | +1 |
| 20 | 2026-11-18 | 11 | Calea Cisnădiei | Metro | 13:59 | 13:59 | 0 |
| 21 | 2026-11-18 | 11 | Calea Cisnădiei | Casa Armatei | 14:12 | 14:08 | +4 |

Legătura cu feed-ul (stop_id, route_id, direction_id) se face printr-un tabel de
corespondență completat de autor (D-021; locul și formatul: Q-020).

## Poze cu panourile din stații
Pozele din 2026-11-17 stau în `field/photos/2026-11-17/` (D-010): maxim 1600 px, fără metadate
EXIF sau GPS. Stația fiecărei poze a fost confirmată de autor. În CSV, coloana `poza` are prima
poză a stației; observațiile de la Casa Armatei, Andrei Șaguna și Poliție nu au poză (D-026).

| Stația | Poze |
|---|---|
| Metro | [panou](field/photos/2026-11-17/2026-11-17_metro_panou.jpg) |
| Teatru | [panou](field/photos/2026-11-17/2026-11-17_teatru_panou.jpg) |
| Piața Cibin 3 | [panou](field/photos/2026-11-17/2026-11-17_piata-cibin-3_panou.jpg), [panou 2](field/photos/2026-11-17/2026-11-17_piata-cibin-3_panou-2.jpg) |
| Zidul Cetății | [panou](field/photos/2026-11-17/2026-11-17_zidul-cetatii_panou.jpg), [panou 2](field/photos/2026-11-17/2026-11-17_zidul-cetatii_panou-2.jpg) |
| Turnișor | [panou](field/photos/2026-11-17/2026-11-17_turnisor_panou.jpg) |
| Ambient 1 | [panou](field/photos/2026-11-17/2026-11-17_ambient-1_panou.jpg) |

Ce spune foaia „Stații” din fișă (neschimbat):

| Nr | Stația | Liniile care opresc aici (din fișă) | Poză făcută? | Nume fișier |
|---|---|---|---|---|
| 1 | Metro | 118 | Da | `118_Metro_17-11-2026_07-18` |
| 2 | Teatru | 2, 118 | Da | |
| 3 | Piața Cibin 3 | 2 | Da | |
| 4 | Zidul Cetății | 2, 19 | Da | |
| 5 | Turnișor | 11, 19, 21 | Da | |
| 6 | Ambient 1 | 11, 21 | Da | |

Casa Armatei, Andrei Șaguna și Poliție (din observații) nu apar în foaie și nu au poză: catalogul
cere poze la minim 6 stații (Q-017).

## Interviuri navetiști
Fișa P07 cere 3 discuții; repo-ul e public, deci doar inițiale și rol.

### 1. L.B., student an 3, zilnic cu linia 2 (2026-11-18, acord: Da)
> „Să știu când vine următorul autobuz, să știu dacă merg pe jos sau nu. Să pot vedea
> întregul traseu al autobuzelor care trec prin stația mea.”

### 2. H., Student an 3, zilnic pe o linie (2026-11-19, acord: Da)
> "Să poți să vezi atât traseul autobuzului până la destinația ta, cât și traseul complet până la capătul de linie."

### 3. S. B, Zilnic (2026-11-26, acord: Da)
> Să vezi traseul dintr-un capăt până la celălalt, și programul autobuzelor pentru a manageria timpul mai eficient. Mă ajută prin a înțelege mai bine orașul și fiecare locație în parte unde este, prin hartă, văzând unde sunt stațiile.
