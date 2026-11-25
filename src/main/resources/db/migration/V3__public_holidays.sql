-- V3: sărbătorile legale din România, 2026–2027 (D-015; Codul muncii, art. 139).
-- Dau doar tipul de zi (D-014) și intră în media abaterilor; plecările nu le folosesc
-- niciodată: ele vin numai din calendar și calendar_dates.
-- După ce ajunge pe Neon, nu se mai editează: corecturile și anii următori intră într-o migrare nouă.

create table public_holidays (
    date date primary key,
    name text not null
);

insert into public_holidays (date, name) values
    -- 2026 (16 date; Paștele ortodox: 12.04)
    ('2026-01-01', 'Anul Nou'),
    ('2026-01-02', 'Anul Nou'),
    ('2026-01-06', 'Botezul Domnului (Boboteaza)'),
    ('2026-01-07', 'Sfântul Ioan Botezătorul'),
    ('2026-01-24', 'Ziua Unirii Principatelor Române'),
    ('2026-04-10', 'Vinerea Mare'),
    ('2026-04-12', 'Paștele'),
    ('2026-04-13', 'Paștele'),
    ('2026-05-01', 'Ziua Muncii'),
    ('2026-05-31', 'Rusaliile'),
    ('2026-06-01', 'Rusaliile; Ziua Copilului'),
    ('2026-08-15', 'Adormirea Maicii Domnului'),
    ('2026-11-30', 'Sfântul Andrei'),
    ('2026-12-01', 'Ziua Națională a României'),
    ('2026-12-25', 'Crăciunul'),
    ('2026-12-26', 'Crăciunul'),
    -- 2027 (17 date; Paștele ortodox: 02.05)
    ('2027-01-01', 'Anul Nou'),
    ('2027-01-02', 'Anul Nou'),
    ('2027-01-06', 'Botezul Domnului (Boboteaza)'),
    ('2027-01-07', 'Sfântul Ioan Botezătorul'),
    ('2027-01-24', 'Ziua Unirii Principatelor Române'),
    ('2027-04-30', 'Vinerea Mare'),
    ('2027-05-01', 'Ziua Muncii'),
    ('2027-05-02', 'Paștele'),
    ('2027-05-03', 'Paștele'),
    ('2027-06-01', 'Ziua Copilului'),
    ('2027-06-20', 'Rusaliile'),
    ('2027-06-21', 'Rusaliile'),
    ('2027-08-15', 'Adormirea Maicii Domnului'),
    ('2027-11-30', 'Sfântul Andrei'),
    ('2027-12-01', 'Ziua Națională a României'),
    ('2027-12-25', 'Crăciunul'),
    ('2027-12-26', 'Crăciunul');
