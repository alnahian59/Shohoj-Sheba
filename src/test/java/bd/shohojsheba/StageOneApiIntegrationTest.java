package bd.shohojsheba;

import bd.shohojsheba.account.AccountPrincipal;
import bd.shohojsheba.account.AccountService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StageOneApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired AccountService accounts;
    private AccountPrincipal principal;

    @BeforeEach
    void registerCitizen() {
        String email = "api-" + UUID.randomUUID() + "@example.test";
        accounts.register("API Test Citizen", email, "correct horse battery staple", "correct horse battery staple");
        principal = (AccountPrincipal) accounts.loadUserByUsername(email);
    }

    @Test
    void catalogueServesOnlyEnabledServicesAndPublishedDemoGuides() throws Exception {
        mvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'passport')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.code == 'birth-registration')]").isNotEmpty());

        mvc.perform(get("/api/services/passport/guide"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("passport"))
                .andExpect(jsonPath("$.descriptionBn").isNotEmpty())
                .andExpect(jsonPath("$.checklistBn").isNotEmpty());

        mvc.perform(get("/api/services/unknown-service/guide"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void disabledServiceIsAbsentFromCatalogueGuideAndDraftCreation() throws Exception {
        jdbc.update("""
                INSERT INTO service_definitions
                    (code, title_bn, title_en, description_bn, checklist_bn, enabled)
                VALUES (?, ?, ?, ?, ?, FALSE)
                """, "disabled-test", "পরীক্ষা", "Disabled test", "নমুনা", "নমুনা");

        mvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'disabled-test')]").isEmpty());
        mvc.perform(get("/api/services/disabled-test/guide"))
                .andExpect(status().isNotFound());
        mvc.perform(postJson("/api/requests", Map.of(
                "serviceCode", "disabled-test", "question", "Synthetic question")))
                .andExpect(status().isNotFound());
    }

    @Test
    void typedIntentMatchesBanglaAndEnglishPassportRequests() throws Exception {
        mvc.perform(postJson("/api/suggestions", Map.of("text", "আমি পাসপোর্ট করতে চাই")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'passport')]").isNotEmpty());

        mvc.perform(postJson("/api/suggestions", Map.of("text", "Need passport help")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'passport')]").isNotEmpty());
    }

    @Test
    void createReturnsOneTimeTrackingSecretAndLookupReturnsOnlyDraftData() throws Exception {
        JsonNode created = createRequest("Synthetic passport question");
        String reference = created.required("reference").asText();
        String trackingCode = created.required("trackingCode").asText();

        assertThat(UUID.fromString(reference)).isNotNull();
        assertThat(trackingCode).hasSize(43);
        assertThat(created.required("state").asText()).isEqualTo("DRAFT");
        assertThat(created.required("version").asLong()).isZero();

        JsonNode viewed = body(mvc.perform(postJson("/api/requests/lookup", Map.of(
                "reference", reference, "trackingCode", trackingCode)))
                .andExpect(status().isOk())
                .andReturn());

        assertThat(viewed.required("reference").asText()).isEqualTo(reference);
        assertThat(viewed.required("question").asText()).isEqualTo("Synthetic passport question");
        assertThat(viewed.required("serviceCode").asText()).isEqualTo("passport");
        assertThat(viewed.has("trackingCode")).isFalse();
        assertThat(viewed.has("trackingHash")).isFalse();
    }

    @Test
    void citizenCanStartAnEmptyDraftAndAddAQuestionLater() throws Exception {
        JsonNode created = createRequest("");
        String reference = created.required("reference").asText();
        String trackingCode = created.required("trackingCode").asText();

        JsonNode emptyDraft = body(mvc.perform(postJson("/api/requests/lookup", Map.of(
                "reference", reference, "trackingCode", trackingCode)))
                .andExpect(status().isOk()).andReturn());
        assertThat(emptyDraft.required("question").asText()).isEmpty();

        JsonNode updated = body(mvc.perform(postJson("/api/requests/update", Map.of(
                "reference", reference,
                "trackingCode", trackingCode,
                "question", "Question added after returning",
                "expectedVersion", 0)))
                .andExpect(status().isOk()).andReturn());
        assertThat(updated.required("question").asText()).isEqualTo("Question added after returning");
        assertThat(updated.required("version").asLong()).isEqualTo(1);
    }

    @Test
    void databaseStoresHashInsteadOfTrackingSecret() throws Exception {
        JsonNode created = createRequest("Synthetic private draft");
        String reference = created.required("reference").asText();
        String trackingCode = created.required("trackingCode").asText();

        String storedHash = jdbc.queryForObject(
                "SELECT tracking_hash FROM assistance_requests WHERE reference = ?",
                String.class, reference);
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(trackingCode.getBytes(StandardCharsets.UTF_8));

        assertThat(storedHash).isEqualTo(HexFormat.of().formatHex(digest));
        assertThat(storedHash).doesNotContain(trackingCode);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM assistance_requests WHERE reference = ? AND tracking_hash = ?",
                Integer.class, reference, trackingCode)).isZero();
    }

    @Test
    void invalidAndCrossRequestTokensCannotRevealAnotherDraft() throws Exception {
        JsonNode first = createRequest("First synthetic question");
        JsonNode second = createRequest("Second synthetic question");

        mvc.perform(postJson("/api/requests/lookup", Map.of(
                "reference", first.required("reference").asText(),
                "trackingCode", second.required("trackingCode").asText())))
                .andExpect(status().isNotFound());
        mvc.perform(postJson("/api/requests/lookup", Map.of(
                "reference", first.required("reference").asText(),
                "trackingCode", "incorrect-secret")))
                .andExpect(status().isNotFound());
        mvc.perform(postJson("/api/requests/lookup", Map.of(
                "reference", UUID.randomUUID().toString(),
                "trackingCode", first.required("trackingCode").asText())))
                .andExpect(status().isNotFound());
        mvc.perform(postJson("/api/requests/update", Map.of(
                "reference", first.required("reference").asText(),
                "trackingCode", second.required("trackingCode").asText(),
                "question", "Unauthorized overwrite",
                "expectedVersion", 0)))
                .andExpect(status().isNotFound());

        JsonNode firstView = body(mvc.perform(postJson("/api/requests/lookup", Map.of(
                "reference", first.required("reference").asText(),
                "trackingCode", first.required("trackingCode").asText())))
                .andExpect(status().isOk()).andReturn());
        assertThat(firstView.required("question").asText()).isEqualTo("First synthetic question");

        JsonNode secondView = body(mvc.perform(postJson("/api/requests/lookup", Map.of(
                "reference", second.required("reference").asText(),
                "trackingCode", second.required("trackingCode").asText())))
                .andExpect(status().isOk()).andReturn());
        assertThat(secondView.required("question").asText()).isEqualTo("Second synthetic question");
    }

    @Test
    void invalidInputIsRejectedBeforeCreatingDraft() throws Exception {
        Integer before = jdbc.queryForObject("SELECT COUNT(*) FROM assistance_requests", Integer.class);

        mvc.perform(postJson("/api/requests", Map.of(
                "serviceCode", "", "question", "Synthetic question")))
                .andExpect(status().isBadRequest());
        mvc.perform(postJson("/api/requests", Map.of(
                "serviceCode", "passport", "question", "a".repeat(501))))
                .andExpect(status().isBadRequest());
        mvc.perform(postJson("/api/suggestions", Map.of("text", " ")))
                .andExpect(status().isBadRequest());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM assistance_requests", Integer.class))
                .isEqualTo(before);
    }

    @Test
    void staleVersionCannotOverwriteAConfirmedDraftEdit() throws Exception {
        JsonNode created = createRequest("Original synthetic question");
        String reference = created.required("reference").asText();
        String trackingCode = created.required("trackingCode").asText();

        JsonNode updated = body(mvc.perform(postJson("/api/requests/update", Map.of(
                "reference", reference,
                "trackingCode", trackingCode,
                "question", "New synthetic question",
                "expectedVersion", 0)))
                .andExpect(status().isOk()).andReturn());
        assertThat(updated.required("version").asLong()).isEqualTo(1);
        assertThat(updated.has("trackingCode")).isFalse();

        mvc.perform(postJson("/api/requests/update", Map.of(
                "reference", reference,
                "trackingCode", trackingCode,
                "question", "Stale overwrite",
                "expectedVersion", 0)))
                .andExpect(status().isConflict());

        JsonNode viewed = body(mvc.perform(postJson("/api/requests/lookup", Map.of(
                "reference", reference, "trackingCode", trackingCode)))
                .andExpect(status().isOk()).andReturn());
        assertThat(viewed.required("question").asText()).isEqualTo("New synthetic question");
        assertThat(viewed.required("version").asLong()).isEqualTo(1);
    }

    @Test
    void writeEndpointsRequireCsrfToken() throws Exception {
        mvc.perform(post("/api/requests")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "serviceCode", "passport", "question", "Synthetic question"))))
                .andExpect(status().isForbidden());
    }

    private JsonNode createRequest(String question) throws Exception {
        MvcResult result = mvc.perform(postJson("/api/requests", Map.of(
                        "serviceCode", "passport", "question", question)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andReturn();
        return body(result);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder postJson(
            String path, Object requestBody) throws Exception {
        return post(path).with(user(principal)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(requestBody));
    }

    private JsonNode body(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsByteArray());
    }
}
