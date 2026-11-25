package ro.autobuze.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ro.autobuze.TestcontainersConfiguration;
import ro.autobuze.gtfs.FeedServer;

/**
 * Endpoint-ul de import (C-001, D-038): pornește importul și răspunde imediat; starea se
 * întreabă până la final. Descărcarea e ținută în așteptare ca testul să prindă importul pornit.
 */
@SpringBootTest(properties = "autobuze.import.token=test-token")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ImportControllerIT {

    private static final String AUTH = "Bearer test-token";
    private static final FeedServer FEED = FeedServer.get();

    @DynamicPropertySource
    static void feedUrl(DynamicPropertyRegistry registry) {
        registry.add("autobuze.import.feed-url", FEED::feedUrl);
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void startReturnsImmediatelyRejectsASecondStartAndReportsTheResult() throws Exception {
        jdbc.execute("truncate feed_versions restart identity cascade");
        FEED.serve(ro.autobuze.gtfs.MiniFeed.zip());
        FEED.hold();
        try {
            mvc.perform(post("/admin/import").header("Authorization", AUTH))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.state").value("RUNNING"))
                    .andExpect(jsonPath("$.startedAt").isNotEmpty());
            // cât rulează, un al doilea import nu pornește
            mvc.perform(post("/admin/import").header("Authorization", AUTH))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.state").value("RUNNING"));
            mvc.perform(get("/admin/import").header("Authorization", AUTH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("RUNNING"));
        } finally {
            FEED.release();
        }

        String state = "RUNNING";
        for (int i = 0; i < 300 && "RUNNING".equals(state); i++) {
            Thread.sleep(100);
            state = com.jayway.jsonpath.JsonPath.read(mvc.perform(get("/admin/import").header("Authorization", AUTH))
                    .andReturn().getResponse().getContentAsString(), "$.state");
        }

        mvc.perform(get("/admin/import").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("DONE"))
                .andExpect(jsonPath("$.feedVersionId").isNumber())
                .andExpect(jsonPath("$.checksum").isNotEmpty())
                .andExpect(jsonPath("$.rows.stop_times").value(12))
                .andExpect(jsonPath("$.finishedAt").isNotEmpty())
                .andExpect(jsonPath("$.durationMs").isNumber());
    }

    @Test
    void wrongTokenIsUnauthorized() throws Exception {
        mvc.perform(post("/admin/import").header("Authorization", "Bearer greșit"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/admin/import"))
                .andExpect(status().isUnauthorized());
    }
}
