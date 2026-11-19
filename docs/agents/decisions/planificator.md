# Decisions — itinerarii și transfer

## D-002 · Planificatorul caută itinerarii cu maxim un transfer
- **Status:** approved
- **Date:** 2026-11-18
- **Rule:** Planificatorul întoarce doar itinerarii cu o cursă directă sau cu exact un transfer; niciodată două sau mai multe.
- **Why:** Cerință din catalog: „un planificator simplu cu maxim un transfer”.
- **Rejected:** rutare multi-transfer (de ex. RAPTOR complet) — în afara scopului cerut.
- **Evidence:** cerința utilizatorului la setup, 2026-11-18 (nu există cod încă)
- **Source:** user · confidence high
- **Owner note:** cerință din catalog; se păstrează.
- **Confirmed:** 2026-11-18

## D-012 · Transfer: stații la maxim 150 m; timp minim 2 min plus mersul pe jos la 1,2 m/s
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Un transfer se face în aceeași stație sau între două stații aflate la cel mult 150 m în linie dreaptă (PostGIS). Timpul minim de transfer este 2 minute, plus timpul de mers pe jos între stații: distanța în linie dreaptă împărțită la 1,2 m/s.
- **Why:** Variabilele proiectului din fișa P07 („sub 150 m”); răspunsul la Q-005.
- **Rejected:** 100 m; viteza de 1 m/s; distanța reală pe stradă (vezi D-020).
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-013 · Planificatorul arată primele 3 itinerarii, ordonate după sosire
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Planificatorul (de la stația A la stația B, acum sau la o oră dată) întoarce cel mult 3 itinerarii, ordonate după ora de sosire, cea mai devreme prima. Fiecare arată plecarea, sosirea, liniile, stația de transfer și timpul de așteptare.
- **Why:** Răspunsul la Q-005; câmpurile afișate vin din fișa P07.
- **Rejected:** un singur itinerariu; itinerariile directe afișate înaintea celor cu transfer.
- **Evidence:** interviul de scope, 2026-11-19
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-033 · Planificatorul: data și ora alese în perioada de valabilitate a feed-ului; continuă cu ziua următoare
- **Status:** proposed
- **Date:** 2026-11-19
- **Rule:** Completează D-002 și D-013. Utilizatorul alege data și ora plecării (implicit: acum), în perioada de valabilitate a versiunii active a feed-ului. Dacă în ziua aleasă nu mai e niciun itinerariu, căutarea continuă cu ziua următoare, ca la D-018.
- **Why:** Răspunsul utilizatorului; SCOPE.md spunea doar „acum sau la o oră dată”.
- **Evidence:** răspunsul utilizatorului la recapitularea din 2026-11-19
- **Source:** user · confidence high
