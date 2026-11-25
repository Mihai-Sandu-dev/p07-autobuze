package ro.autobuze.gtfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Fișierele GTFS importate și coloanele lor în DB (V2__schema.sql, D-044), în ordinea în care
 * se încarcă: fiecare tabel vine după cele spre care are chei străine. Coloanele din DB au
 * numele din GTFS, cu excepția orelor din stop_times (D-043).
 */
enum GtfsTable {

    AGENCY("agency.txt", "agency", true,
            new Column("agency_id", "agency_id", Column.EMPTY_IF_MISSING, false),
            text("agency_name", true), text("agency_url", true), text("agency_timezone", true),
            text("agency_lang", false), text("agency_phone", false)),
    STOPS("stops.txt", "stops", true,
            text("stop_id", true), text("stop_code", false), text("stop_name", true),
            text("stop_lat", true), text("stop_lon", true), text("location_type", false),
            text("parent_station", false)),
    ROUTES("routes.txt", "routes", true,
            text("route_id", true), text("agency_id", false), text("route_short_name", false),
            text("route_long_name", false), text("route_type", true), text("route_color", false),
            text("route_text_color", false)),
    CALENDAR("calendar.txt", "calendar", false,
            text("service_id", true), text("monday", true), text("tuesday", true), text("wednesday", true),
            text("thursday", true), text("friday", true), text("saturday", true), text("sunday", true),
            date("start_date"), date("end_date")),
    CALENDAR_DATES("calendar_dates.txt", "calendar_dates", false,
            text("service_id", true), date("date"), text("exception_type", true)),
    SHAPES("shapes.txt", "shapes", false,
            text("shape_id", true), text("shape_pt_sequence", true), text("shape_pt_lat", true),
            text("shape_pt_lon", true), text("shape_dist_traveled", false)),
    TRIPS("trips.txt", "trips", true,
            text("trip_id", true), text("route_id", true), text("service_id", true),
            text("trip_headsign", false), text("direction_id", false), text("shape_id", false)),
    STOP_TIMES("stop_times.txt", "stop_times", true,
            text("trip_id", true), text("stop_sequence", true), text("stop_id", true),
            new Column("arrival_sec", "arrival_time", Column.SECONDS, false),
            new Column("departure_sec", "departure_time", Column.SECONDS, false),
            text("pickup_type", false), text("drop_off_type", false));

    private static final CSVFormat GTFS_CSV = CSVFormat.RFC4180.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .get();

    private final String file;
    private final String table;
    private final boolean required;
    private final List<Column> columns;

    GtfsTable(String file, String table, boolean required, Column... columns) {
        this.file = file;
        this.table = table;
        this.required = required;
        this.columns = List.of(columns);
    }

    /** Numele fișierului din arhivă, de ex. {@code stop_times.txt}. */
    String file() {
        return file;
    }

    /** Tabelul din DB. */
    String table() {
        return table;
    }

    /** Fișierul trebuie să existe în feed. calendar și calendar_dates: cel puțin unul (verificat la import). */
    boolean required() {
        return required;
    }

    /** Coloanele din DB în ordinea valorilor din {@link #read}, fără {@code feed_version_id}. */
    List<String> dbColumns() {
        return columns.stream().map(Column::db).toList();
    }

    /**
     * Citește fișierul în flux și trimite fiecare rând, ca valori pentru {@link #dbColumns()}
     * (gol → null). Coloanele necunoscute se ignoră; lipsa unei coloane obligatorii oprește importul.
     *
     * @return numărul de rânduri citite
     */
    long read(Reader in, Consumer<List<String>> sink) {
        try (CSVParser csv = GTFS_CSV.parse(withoutBom(in))) {
            List<String> header = csv.getHeaderNames();
            for (Column c : columns) {
                if (c.required() && !header.contains(c.gtfs())) {
                    throw new GtfsException(file + ": lipsește coloana obligatorie " + c.gtfs());
                }
            }
            long count = 0;
            for (CSVRecord record : csv) {
                List<String> values = new ArrayList<>(columns.size());
                for (Column c : columns) {
                    String raw = record.isMapped(c.gtfs()) && record.isSet(c.gtfs()) ? record.get(c.gtfs()) : null;
                    try {
                        values.add(c.convert().apply(raw == null || raw.isEmpty() ? null : raw));
                    } catch (IllegalArgumentException e) {
                        // rândul 1 e antetul
                        throw new GtfsException(file + ", rândul " + (record.getRecordNumber() + 1)
                                + ", coloana " + c.gtfs() + ": " + e.getMessage(), e);
                    }
                }
                sink.accept(values);
                count++;
            }
            return count;
        } catch (IOException | UncheckedIOException e) {
            // la citire sau la scrierea în COPY (de ex. o cheie străină greșită)
            throw new GtfsException(file + ": " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            // Commons CSV: ghilimele neînchise sau alt CSV stricat
            throw new GtfsException(file + ": CSV greșit (" + e.getMessage() + ")", e);
        }
    }

    /** Commons CSV nu sare peste BOM-ul UTF-8, pe care unele feed-uri îl au la început. */
    private static Reader withoutBom(Reader in) throws IOException {
        BufferedReader reader = new BufferedReader(in, 1 << 16);
        reader.mark(1);
        if (reader.read() != '﻿') {
            reader.reset();
        }
        return reader;
    }

    private static Column text(String name, boolean required) {
        return new Column(name, name, UnaryOperator.identity(), required);
    }

    private static Column date(String name) {
        return new Column(name, name, GtfsValues::date, true);
    }

    /** O coloană din DB, coloana GTFS din care vine și conversia valorii (null = gol). */
    private record Column(String db, String gtfs, UnaryOperator<String> convert, boolean required) {

        static final UnaryOperator<String> SECONDS = v -> {
            Integer s = GtfsValues.seconds(v);
            return s == null ? null : s.toString();
        };

        // Un singur operator fără agency_id: cheia devine '' (V2__schema.sql)
        static final UnaryOperator<String> EMPTY_IF_MISSING = v -> v == null ? "" : v;
    }
}
