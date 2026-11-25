package ro.autobuze.gtfs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Maparea noastră din fișierele GTFS spre coloanele din DB (D-044). Citirea CSV-ului e a
 * bibliotecii Commons CSV (D-046); aici se testează doar ce facem noi cu rândurile.
 */
class GtfsTableTest {

    @Test
    void stopTimesMapsColumnsByHeaderAndConvertsTimes() {
        // BOM, coloane în altă ordine, o coloană necunoscută, arrival_time lipsă
        List<List<String>> rows = read(GtfsTable.STOP_TIMES,
                "﻿stop_sequence,trip_id,departure_time,stop_id,necunoscuta\n2,T1,25:10:00,S1,x\n");

        assertThat(GtfsTable.STOP_TIMES.dbColumns()).containsExactly(
                "trip_id", "stop_sequence", "stop_id", "arrival_sec", "departure_sec", "pickup_type", "drop_off_type");
        assertThat(rows).containsExactly(Arrays.asList("T1", "2", "S1", null, "90600", null, null));
    }

    @Test
    void emptyValuesBecomeNull() {
        List<List<String>> rows = read(GtfsTable.TRIPS,
                "route_id,service_id,trip_id,trip_headsign,direction_id,shape_id\nR1,LV,T1,,,\n");

        assertThat(rows).containsExactly(Arrays.asList("T1", "R1", "LV", null, null, null));
    }

    @Test
    void quotedStopNameKeepsCommaAndQuotesWithWindowsLineEnds() {
        List<List<String>> rows = read(GtfsTable.STOPS,
                "stop_id,stop_name,stop_lat,stop_lon\r\nS1,\"Piața Cibin, peron \"\"2\"\"\",45.8003,24.1475\r\n");

        assertThat(GtfsTable.STOPS.dbColumns()).containsExactly(
                "stop_id", "stop_code", "stop_name", "stop_lat", "stop_lon", "location_type", "parent_station");
        assertThat(rows).containsExactly(
                Arrays.asList("S1", null, "Piața Cibin, peron \"2\"", "45.8003", "24.1475", null, null));
    }

    @Test
    void calendarDatesBecomeIsoDates() {
        List<List<String>> rows = read(GtfsTable.CALENDAR_DATES,
                "service_id,date,exception_type\nLV,20261201,2\n");

        assertThat(rows).containsExactly(List.of("LV", "2026-12-01", "2"));
    }

    @Test
    void agencyWithoutAgencyIdGetsEmptyId() {
        // Un singur operator, fără agency_id: cheia devine '' (V2__schema.sql)
        List<List<String>> rows = read(GtfsTable.AGENCY,
                "agency_name,agency_url,agency_timezone\nTursib,https://tursib.ro,Europe/Bucharest\n");

        assertThat(rows).containsExactly(
                Arrays.asList("", "Tursib", "https://tursib.ro", "Europe/Bucharest", null, null));
    }

    @Test
    void missingRequiredColumnNamesFileAndColumn() {
        assertThatThrownBy(() -> read(GtfsTable.STOPS, "stop_id,stop_name,stop_lon\nS1,Gară,24.16\n"))
                .isInstanceOf(GtfsException.class)
                .hasMessageContaining("stops.txt")
                .hasMessageContaining("stop_lat");
    }

    @Test
    void badValueNamesFileAndLine() {
        assertThatThrownBy(() -> read(GtfsTable.STOP_TIMES,
                "trip_id,arrival_time,departure_time,stop_id,stop_sequence\n"
                        + "T1,07:00:00,07:00:00,S1,1\nT1,ab:cd,07:10:00,S2,2\n"))
                .isInstanceOf(GtfsException.class)
                .hasMessageContaining("stop_times.txt")
                .hasMessageContaining("rândul 3")
                .hasMessageContaining("ab:cd");
    }

    private static List<List<String>> read(GtfsTable table, String csv) {
        List<List<String>> rows = new ArrayList<>();
        long count = table.read(new StringReader(csv), rows::add);
        assertThat(count).isEqualTo(rows.size());
        return rows;
    }
}
