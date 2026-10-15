package bd.shohojsheba;

import bd.shohojsheba.account.AccountPrincipal;
import bd.shohojsheba.account.AccountService;
import bd.shohojsheba.dto.CreatedRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StageOneWebIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired AccountService accounts;
    private AccountPrincipal principal;

    @BeforeEach
    void registerCitizen() {
        String email = "web-" + UUID.randomUUID() + "@example.test";
        accounts.register("Web Test Citizen", email, "correct horse battery staple", "correct horse battery staple");
        principal = (AccountPrincipal) accounts.loadUserByUsername(email);
    }

    @Test
    void publicPagesRenderGuidanceAndFormsWithCsrfToken() throws Exception {
        mvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("কোনো সরকারি দপ্তরে")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));

        mvc.perform(get("/services/passport"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("পাসপোর্ট")));

        mvc.perform(get("/drafts/new").with(user(principal)).param("service", "passport"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));

        mvc.perform(get("/resume").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void browserDraftCreationKeepsTrackingSecretOutOfRedirectUrl() throws Exception {
        MvcResult created = mvc.perform(post("/drafts").with(user(principal)).with(csrf())
                        .param("serviceCode", "passport")
                        .param("question", "Synthetic browser question"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/drafts/created"))
                .andExpect(flash().attributeExists("created"))
                .andReturn();

        CreatedRequest secret = (CreatedRequest) created.getFlashMap().get("created");
        mvc.perform(get("/drafts/created").with(user(principal)).flashAttrs(created.getFlashMap()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(content().string(containsString(secret.trackingCode())));

        mvc.perform(get("/drafts/created").with(user(principal)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void browserWriteWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/drafts").with(user(principal))
                        .param("serviceCode", "passport")
                        .param("question", "Synthetic browser question"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/resume").with(user(principal)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/drafts/update").with(user(principal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void reflectedUserTextIsNotStoredInBrowserCache() throws Exception {
        mvc.perform(post("/suggest").with(csrf())
                        .param("q", "Synthetic private question"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")));

        mvc.perform(post("/drafts").with(user(principal)).with(csrf())
                        .param("serviceCode", "passport")
                        .param("question", "x".repeat(501)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")));
    }

    @Test
    void browserResumeAndVersionCheckedEditProtectTheDraft() throws Exception {
        MvcResult created = mvc.perform(post("/drafts").with(user(principal)).with(csrf())
                        .param("serviceCode", "passport")
                        .param("question", "Original browser question"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        CreatedRequest secret = (CreatedRequest) created.getFlashMap().get("created");

        mvc.perform(post("/resume").with(user(principal)).with(csrf())
                        .param("reference", secret.reference())
                        .param("trackingCode", "incorrect-secret"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("রেফারেন্স ও কোড মেলেনি")))
                .andExpect(content().string(not(containsString("Original browser question"))));

        mvc.perform(post("/resume").with(user(principal)).with(csrf())
                        .param("reference", secret.reference())
                        .param("trackingCode", secret.trackingCode()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(content().string(containsString("Original browser question")));

        mvc.perform(post("/drafts/update").with(user(principal)).with(csrf())
                        .param("reference", secret.reference())
                        .param("trackingCode", secret.trackingCode())
                        .param("question", "Updated browser question")
                        .param("expectedVersion", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Updated browser question")));

        mvc.perform(post("/drafts/update").with(user(principal)).with(csrf())
                        .param("reference", secret.reference())
                        .param("trackingCode", secret.trackingCode())
                        .param("question", "Stale browser overwrite")
                        .param("expectedVersion", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("বদলে গেছে")));
    }
}
