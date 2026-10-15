package bd.shohojsheba;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FloatingChatWidgetIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @Test
    void servicesHomeHasCompactChatLauncherInsteadOfHeroAssistantForm() throws Exception {
        mvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-testid=\"chat-widget\"")))
                .andExpect(content().string(containsString("data-testid=\"chat-launcher\"")))
                .andExpect(content().string(containsString("class=\"floating-chat-panel\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(not(containsString("class=\"hero-task\""))))
                .andExpect(content().string(not(containsString("আপনার কী সেবা দরকার?"))));
    }

    @Test
    void guestCanAskFromWidgetAndSeeServiceLinkWithoutSavingQuestion() throws Exception {
        String question = "passport help " + UUID.randomUUID();
        Integer before = jdbc.queryForObject("SELECT COUNT(*) FROM assistance_requests", Integer.class);

        mvc.perform(post("/services/chat").with(csrf()).param("text", question))
                .andExpect(status().isOk())
                .andExpect(model().attribute("widgetOpen", true))
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(content().string(containsString("data-testid=\"chat-widget\"")))
                .andExpect(content().string(containsString(question)))
                .andExpect(content().string(containsString("/services/passport")))
                .andExpect(content().string(containsString("পাসপোর্ট")));

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM assistance_requests", Integer.class))
                .isEqualTo(before);
    }

    @Test
    void invalidWidgetQuestionShowsErrorWithoutEchoingOversizedInput() throws Exception {
        mvc.perform(post("/services/chat").with(csrf()).param("text", " "))
                .andExpect(status().isOk())
                .andExpect(model().attribute("widgetOpen", true))
                .andExpect(content().string(containsString("১ থেকে ৫০০ অক্ষরের মধ্যে")));

        String oversized = "x".repeat(501);
        mvc.perform(post("/services/chat").with(csrf()).param("text", oversized))
                .andExpect(status().isOk())
                .andExpect(model().attribute("widgetOpen", true))
                .andExpect(content().string(containsString("১ থেকে ৫০০ অক্ষরের মধ্যে")))
                .andExpect(content().string(not(containsString(oversized))));
    }

    @Test
    void widgetSubmissionRequiresCsrfToken() throws Exception {
        mvc.perform(post("/services/chat").param("text", "passport"))
                .andExpect(status().isForbidden());
    }
}
