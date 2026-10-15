package bd.shohojsheba;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LanguageNavigationIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void languageChoicePersistsAcrossPagesAndRejectsExternalReturnPath() throws Exception {
        MvcResult englishChoice = mvc.perform(get("/language")
                        .param("lang", "en")
                        .param("returnTo", "/chat"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/chat"))
                .andExpect(cookie().exists("SHOHOJ_LANGUAGE"))
                .andReturn();

        Cookie englishCookie = englishChoice.getResponse().getCookie("SHOHOJ_LANGUAGE");
        mvc.perform(get("/chat").cookie(englishCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign in")))
                .andExpect(content().string(containsString("Ask a question.")));
        mvc.perform(post("/chat").cookie(englishCookie).with(csrf()).param("text", " "))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Write a question between 1 and 500 characters.")));
        mvc.perform(get("/language")
                        .cookie(englishCookie)
                        .param("lang", "bn")
                        .param("returnTo", "/drafts/new?service=passport"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/drafts/new?service=passport"));

        mvc.perform(get("/language")
                        .cookie(englishCookie)
                        .param("lang", "bn")
                        .param("returnTo", "//outside.example"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/services"))
                .andExpect(cookie().exists("SHOHOJ_LANGUAGE"));
    }
}
