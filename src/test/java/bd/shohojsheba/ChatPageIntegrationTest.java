package bd.shohojsheba;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatPageIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @Test
    void guestCanChatThroughJavaRenderedFormWithoutSavingQuestion() throws Exception {
        mvc.perform(get("/chat"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));

        String question = "Synthetic passport question " + System.nanoTime();
        Integer before = jdbc.queryForObject("SELECT COUNT(*) FROM assistance_requests", Integer.class);
        mvc.perform(post("/chat").with(csrf()).param("text", question))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(content().string(containsString(question)))
                .andExpect(content().string(containsString("পাসপোর্ট")));
        mvc.perform(post("/chat").with(csrf())
                        .param("previous", question)
                        .param("text", "জন্ম নিবন্ধন করতে চাই"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(question)))
                .andExpect(content().string(containsString("জন্ম নিবন্ধন করতে চাই")));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM assistance_requests", Integer.class))
                .isEqualTo(before);
    }

    @Test
    void chatFormRequiresCsrfToken() throws Exception {
        mvc.perform(post("/chat").param("text", "passport"))
                .andExpect(status().isForbidden());
    }
}
