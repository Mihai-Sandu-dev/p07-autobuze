# Decisions — import, versiuni, identificatori

## D-003 · Re-importul creează o versiune nouă a feed-ului, atomic, cu raport de diferențe
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Fiecare import creează o versiune nouă a feed-ului GTFS, într-o singură tranzacție: fie se importă tot, fie nimic. Importul produce un raport de diferențe față de versiunea anterioară: stații noi, stații dispărute, trasee schimbate.
- **Why:** Cerință din catalog (raportul de diferențe). Un import eșuat nu trebuie să lase date pe jumătate.
- **Rejected:** înlocuirea datelor pe loc, fără versiuni (nu permite raportul de diferențe).
- **Evidence:** răspunsul utilizatorului la Q-002, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-004 · Doar orarul static GTFS; GTFS-Realtime e în afara scopului
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Următoarele plecări și planificatorul folosesc doar orarul static GTFS. Aplicația nu citește GTFS-Realtime. Abaterile de la orar se cunosc din rapoartele utilizatorilor și din observațiile de teren ale autorului.
- **Why:** Decizia utilizatorului: realtime e în afara scopului.
- **Rejected:** GTFS-Realtime.
- **Evidence:** răspunsul utilizatorului la Q-003, 2026-11-18
- **Source:** user · confidence high
- **Confirmed:** 2026-11-18

## D-019 · Jobul de import: zilnic și la cererea adminului; versiune nouă doar la checksum nou
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Jobul descarcă feed-ul o dată pe zi, noaptea, și oricând îl pornește adminul. Fiecare descărcare are un checksum. Dacă checksum-ul e același ca la versiunea activă, nu se creează o versiune nouă; se notează doar verificarea. Un import reușit devine activ imediat. În DB rămân versiunea activă și cea anterioară; cele mai vechi se șterg.
- **Why:** Răspunsul utilizatorului; fișa P07 cere job cu versiune și checksum. Completează D-003.
- **Rejected:** doar import manual; săptămânal; păstrarea tuturor versiunilor; activare manuală de către admin.
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-020 · Rapoartele și favoritele se leagă de identificatori stabili, nu de o versiune de feed
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Rapoartele de abatere și favoritele se leagă de identificatori care rămân la fel între versiuni (route_short_name pentru linie; stop_code sau stop_id pentru stație, vezi Q-019), nu de rândurile unei versiuni anume. Ștergerea unei versiuni vechi nu șterge rapoarte sau favorite.
- **Why:** Răspunsul utilizatorului: D-019 șterge versiunile vechi, iar datele utilizatorilor nu trebuie să se piardă.
- **Rejected:** chei străine spre rândurile unei versiuni.
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-031 · O stație = parent_station din feed; altfel, rândurile din stops.txt cu același nume la maxim 150 m
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** O stație din aplicație grupează rânduri din `stops.txt`: după `parent_station`, dacă feed-ul îl are; altfel, rândurile cu același nume aflate la cel mult 150 m unele de altele. Pagina de stație arată toate sensurile rândurilor grupate. Harta și stațiile A/B din planificator folosesc aceeași grupare. Regula se confirmă după descărcarea feed-ului (Q-019).
- **Why:** Răspunsul utilizatorului. Aceeași stație are câte un stop pe fiecare sens (fișa de teren, foaia Instrucțiuni A11); mock-ul paginii de stație arată mai multe sensuri.
- **Evidence:** răspunsul utilizatorului la recapitularea din 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-038 · Importul rulează pe Render, pornit de un cron din GitHub Actions printr-un endpoint protejat
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Completează D-019 și D-037. Importul rulează în aplicația de pe Render. Un workflow GitHub Actions programat zilnic, noaptea, apelează un endpoint de admin protejat cu un token secret (variabilă de mediu pe Render, secret în GitHub); serviciul se trezește și pornește importul. Endpoint-ul răspunde imediat, iar workflow-ul întreabă starea importului la fiecare minut, până la final, ca serviciul să nu adoarmă în timpul importului. Butonul adminului pornește același import. Datele merg spre Neon în flux, cu COPY, fără să țină feed-ul în memorie; JVM-ul are memoria limitată explicit (`-Xmx300m`).
- **Why:** Răspunsul utilizatorului la riscurile D-037: pe planul gratuit, Render adoarme serviciul după 15 min fără cereri și are puțin RAM; feed-ul are ~8 MB dezarhivat.
- **Rejected:** importul în GitHub Actions, direct pe Neon (rămâne rezerva); instanța Render plătită.
- **Risk:** dacă la primul import real, măsurat pe Render, memoria nu ajunge, importul trece în GitHub Actions (jarul în modul import, direct pe Neon).
- **Evidence:** răspunsul utilizatorului la /plan pentru scheletul proiectului, 2026-11-19; render.com/docs/free
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19
