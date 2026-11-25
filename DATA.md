# DATA — datele GTFS ale aplicației

## Sursă
- Oraș: Sibiu. Operator: Tursib S.A.
- Feed GTFS oficial: https://tursib.ro/trasee/gtfs
- Copie de rezervă: https://mobilitydatabase.org/feeds/gtfs/mdb-2099
- Arhiva verificată la 25.11.2026: `tursib.gtfs_2026-10-v4.9.zip` (1,66 MB; `feed_version` v4.9),
  la care redirecționează adresa oficială. Conține `shapes.txt` (84.954 de puncte; Q-010 închisă).
  `stop_times.txt` are 93.218 rânduri; în total, 183.887 de rânduri în 9 fișiere.
  `stops.txt` (512 rânduri) nu are `stop_code`, iar `parent_station` e gol (Q-019).

## Licență
- Feed-ul nu are o licență declarată: câmpul de licență din `feed_info.txt` e gol (verificat la 18.11.2026).
- Termenii site-ului tursib.ro (https://tursib.ro/page/terms) permit folosirea informațiilor în scop personal, necomercial. Dreptul de autor e al TURSIB SA.
- La 29.01.2026 (nr. 525), Tursib a acordat o permisiune scrisă pentru OpenStreetMap, cu condiția atribuirii „Tursib S.A.” (https://wiki.openstreetmap.org/wiki/File:Permisiune_de_utilizare_a_datelor_format_GTFS.pdf).
- Proiectul folosește datele necomercial, în scop educațional, cu atribuire (D-008).

## Probleme găsite
<!-- se completează la import -->

## Curățare
<!-- se completează la import -->
