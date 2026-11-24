-- V2: schema aplicației (punctul 2 din ROADMAP; docs/schema.md).
-- După ce ajunge pe Neon, nu se mai editează: orice schimbare e o migrare nouă.
--
-- Tabelele GTFS (D-044): numele și coloanele din specificația GTFS Schedule, cheia
-- (feed_version_id, id-ul din GTFS); se șterg în cascadă odată cu versiunea de feed.
-- Orele din feed (D-043): int, secunde de la începutul zilei de serviciu (25:10:00 → 90600).
-- Datele utilizatorilor se leagă de feed doar prin route_short_name și stop_key (D-020),
-- fără chei străine spre tabelele GTFS, ca să rămână când se șterge o versiune (D-019).

-- Pe Neon, rolul owner poate crea extensia; local și în teste, imaginea postgis/postgis o are.
create extension if not exists postgis;

-- Versiune de feed: rezultatul unui import reușit, cu checksum; una singură e activă.
create table feed_versions (
    id          bigint generated always as identity primary key,
    checksum    text        not null,
    source_url  text        not null,
    imported_at timestamptz not null default now(),
    active      boolean     not null default false
);
create unique index feed_versions_one_active on feed_versions (active) where active;

-- Operator (agency.txt). Fără agency_id în feed, importul scrie '' (un singur operator).
create table agency (
    feed_version_id bigint not null references feed_versions on delete cascade,
    agency_id       text   not null,
    agency_name     text   not null,
    agency_url      text   not null,
    agency_timezone text   not null,
    agency_lang     text,
    agency_phone    text,
    primary key (feed_version_id, agency_id)
);

-- Rândurile din stops.txt; o stație grupează unul sau mai multe (D-031).
create table stops (
    feed_version_id bigint           not null references feed_versions on delete cascade,
    stop_id         text             not null,
    stop_code       text,
    stop_name       text             not null,
    stop_lat        double precision not null,
    stop_lon        double precision not null,
    location_type   smallint,
    parent_station  text,
    geog            geography(Point, 4326)
                    generated always as (ST_SetSRID(ST_MakePoint(stop_lon, stop_lat), 4326)::geography) stored,
    primary key (feed_version_id, stop_id)
);
create index stops_geog on stops using gist (geog);

-- Linie (routes.txt).
create table routes (
    feed_version_id  bigint   not null references feed_versions on delete cascade,
    route_id         text     not null,
    agency_id        text,
    route_short_name text,
    route_long_name  text,
    route_type       smallint not null,
    route_color      text,
    route_text_color text,
    primary key (feed_version_id, route_id)
);

-- Calendar (calendar.txt): zilele în care circulă un serviciu.
create table calendar (
    feed_version_id bigint  not null references feed_versions on delete cascade,
    service_id      text    not null,
    monday          boolean not null,
    tuesday         boolean not null,
    wednesday       boolean not null,
    thursday        boolean not null,
    friday          boolean not null,
    saturday        boolean not null,
    sunday          boolean not null,
    start_date      date    not null,
    end_date        date    not null,
    primary key (feed_version_id, service_id)
);

-- Excepțiile de calendar (calendar_dates.txt): 1 = adăugat, 2 = scos.
-- Fără cheie străină spre calendar: GTFS permite un service_id doar aici.
create table calendar_dates (
    feed_version_id bigint   not null references feed_versions on delete cascade,
    service_id      text     not null,
    date            date     not null,
    exception_type  smallint not null check (exception_type in (1, 2)),
    primary key (feed_version_id, service_id, date)
);

-- Forma traseului pe hartă (shapes.txt); poate rămâne gol (Q-010).
create table shapes (
    feed_version_id     bigint           not null references feed_versions on delete cascade,
    shape_id            text             not null,
    shape_pt_sequence   integer          not null,
    shape_pt_lat        double precision not null,
    shape_pt_lon        double precision not null,
    shape_dist_traveled double precision,
    primary key (feed_version_id, shape_id, shape_pt_sequence)
);

-- Cursă (trips.txt); sensul e direction_id + trip_headsign.
create table trips (
    feed_version_id bigint   not null,
    trip_id         text     not null,
    route_id        text     not null,
    service_id      text     not null,
    trip_headsign   text,
    direction_id    smallint check (direction_id in (0, 1)),
    shape_id        text,
    primary key (feed_version_id, trip_id),
    foreign key (feed_version_id) references feed_versions on delete cascade,
    foreign key (feed_version_id, route_id) references routes on delete cascade
);
create index trips_route on trips (feed_version_id, route_id);

-- Plecare (stop_times.txt); orele în secunde de la începutul zilei de serviciu (D-043).
create table stop_times (
    feed_version_id bigint   not null,
    trip_id         text     not null,
    stop_sequence   integer  not null,
    stop_id         text     not null,
    arrival_sec     integer,
    departure_sec   integer,
    pickup_type     smallint,
    drop_off_type   smallint,
    primary key (feed_version_id, trip_id, stop_sequence),
    foreign key (feed_version_id) references feed_versions on delete cascade,
    foreign key (feed_version_id, trip_id) references trips on delete cascade,
    foreign key (feed_version_id, stop_id) references stops on delete cascade
);
create index stop_times_stop_departure on stop_times (feed_version_id, stop_id, departure_sec);

-- Utilizator (D-011, D-032): email unic fără diferență între litere mari și mici.
create table users (
    id            bigint generated always as identity primary key,
    email         text        not null,
    password_hash text        not null,
    role          text        not null default 'USER' check (role in ('USER', 'ADMIN')),
    created_at    timestamptz not null default now()
);
create unique index users_email_lower on users (lower(email));

-- Favorită: cheia stabilă a unui rând al stației (stop_key, Q-019); gruparea (D-031) la citire.
create table favorites (
    id         bigint generated always as identity primary key,
    user_id    bigint      not null references users on delete cascade,
    stop_key   text        not null,
    created_at timestamptz not null default now(),
    unique (user_id, stop_key)
);

-- Raport de abatere (D-009, D-017, D-025): sursa „utilizator” (cu user_id) sau „teren” (fără).
-- Orele în secunde de la începutul zilei de serviciu (D-043); deviation_min cu semn (D-024).
create table deviation_reports (
    id               bigint generated always as identity primary key,
    source           text        not null check (source in ('utilizator', 'teren')),
    user_id          bigint      references users,
    route_short_name text        not null,
    stop_key         text        not null,
    direction_id     smallint    check (direction_id in (0, 1)),
    service_date     date        not null,
    feed_time_sec    integer     not null,
    actual_time_sec  integer     not null,
    deviation_min    integer     not null,
    created_at       timestamptz not null default now(),
    check ((source = 'utilizator') = (user_id is not null))
);
