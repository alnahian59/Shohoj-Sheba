package bd.shohojsheba;

import bd.shohojsheba.account.AccountPrincipal;
import bd.shohojsheba.account.AccountService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired AccountService accounts;

    @Test
    void guestCanBrowseButCannotCreateOrReadDrafts() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mvc.perform(get("/services"))
                .andExpect(status().isOk());
        mvc.perform(get("/my/drafts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
        mvc.perform(post("/api/requests").with(csrf())
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of(
                                "serviceCode", "passport", "question", "Guest question"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"));
        mvc.perform(post("/drafts").with(csrf())
                        .param("serviceCode", "passport")
                        .param("question", "Guest question"))
                .andExpect(status().is3xxRedirection());
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM assistance_requests WHERE question = ?",
                Integer.class, "Guest question")).isZero();
    }

    @Test
    void registrationHashesPasswordAndLoginLogoutUseSession() throws Exception {
        String email = "Mixed." + UUID.randomUUID() + "@Example.Test";
        String password = "correct horse battery staple";
        mvc.perform(post("/register").with(csrf())
                        .param("displayName", "Test Citizen")
                        .param("email", email)
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        String stored = jdbc.queryForObject("SELECT password_hash FROM app_users WHERE email = ?",
                String.class, email.toLowerCase());
        assertThat(stored).startsWith("$2").isNotEqualTo(password);

        MvcResult login = mvc.perform(post("/login").with(csrf())
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/services"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();
        mvc.perform(get("/my/drafts").session(session))
                .andExpect(status().isOk());
        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?loggedOut"));
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void anotherAccountCannotReadOrEditDraftEvenWithTrackingCode() throws Exception {
        AccountPrincipal alice = citizen("Alice");
        AccountPrincipal bob = citizen("Bob");
        JsonNode created = body(mvc.perform(postJson("/api/requests", alice, Map.of(
                        "serviceCode", "passport", "question", "Alice private question")))
                .andExpect(status().isCreated()).andReturn());
        String reference = created.required("reference").asText();
        String token = created.required("trackingCode").asText();

        mvc.perform(get("/api/requests/" + reference).with(user(bob)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/drafts/" + reference).with(user(bob)))
                .andExpect(status().isNotFound());
        mvc.perform(postJson("/api/requests/lookup", bob, Map.of(
                        "reference", reference, "trackingCode", token)))
                .andExpect(status().isNotFound());
        mvc.perform(postJson("/api/requests/update", bob, Map.of(
                        "reference", reference, "trackingCode", token,
                        "question", "Unauthorized edit", "expectedVersion", 0)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/requests").with(user(bob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mvc.perform(get("/api/requests/" + reference).with(user(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("Alice private question"));
    }

    @Test
    void legacyDraftRequiresOriginalTokenBeforeClaimAndBecomesAccountOwned() throws Exception {
        AccountPrincipal alice = citizen("Legacy Alice");
        AccountPrincipal bob = citizen("Legacy Bob");
        String reference = UUID.randomUUID().toString();
        String token = "A".repeat(43);
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
        jdbc.update("""
                INSERT INTO assistance_requests
                    (reference, service_code, question, state, tracking_hash, created_at, version)
                VALUES (?, ?, ?, ?, ?, ?, 0)
                """, reference, "passport", "Older local draft", "DRAFT", hash,
                Timestamp.from(Instant.now()));

        mvc.perform(postJson("/api/requests/claim", alice, Map.of(
                        "reference", reference, "trackingCode", "B".repeat(43))))
                .andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject(
                "SELECT owner_id FROM assistance_requests WHERE reference = ?",
                String.class, reference)).isNull();

        mvc.perform(postJson("/api/requests/claim", alice, Map.of(
                        "reference", reference, "trackingCode", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("Older local draft"));
        assertThat(jdbc.queryForObject(
                "SELECT owner_id FROM assistance_requests WHERE reference = ?",
                String.class, reference)).isEqualTo(alice.id());

        mvc.perform(postJson("/api/requests/claim", bob, Map.of(
                        "reference", reference, "trackingCode", token)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/requests/" + reference).with(user(alice)))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedDraftWriteStillRequiresCsrf() throws Exception {
        AccountPrincipal alice = citizen("CSRF Test");
        mvc.perform(post("/api/requests").with(user(alice))
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of(
                                "serviceCode", "passport", "question", "Protected question"))))
                .andExpect(status().isForbidden());
    }

    private AccountPrincipal citizen(String name) {
        String email = UUID.randomUUID() + "@example.test";
        accounts.register(name, email, "correct horse battery staple", "correct horse battery staple");
        return (AccountPrincipal) accounts.loadUserByUsername(email);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder postJson(
            String path, AccountPrincipal principal, Object body) throws Exception {
        return post(path).with(user(principal)).with(csrf())
                .contentType("application/json")
                .content(json.writeValueAsBytes(body));
    }

    private JsonNode body(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsByteArray());
    }
}
