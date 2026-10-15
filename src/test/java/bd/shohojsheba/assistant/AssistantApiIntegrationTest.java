package bd.shohojsheba.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssistantApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    @Test
    void answersBanglaAndEnglishQuestionsFromTheEnabledCatalogue() throws Exception {
        mvc.perform(ask("আমি পাসপোর্ট করতে চাই"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(jsonPath("$.suggestions[0].code").value("passport"))
                .andExpect(jsonPath("$.replyBn", containsString("সরকারি উৎসে যাচাই করুন")));

        mvc.perform(ask("How can I get a birth certificate?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions[0].code").value("birth-registration"));

        mvc.perform(ask("Amar jonmo nibondhon dorkar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions[0].code").value("birth-registration"));
    }

    @Test
    void presentsChoicesForAmbiguousQuestionsAndDoesNotInventAnAnswer() throws Exception {
        mvc.perform(ask("Do I need a passport or birth certificate?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions.length()").value(2))
                .andExpect(jsonPath("$.replyBn", containsString("একাধিক সেবার ইঙ্গিত")));

        mvc.perform(ask("I need a license"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions.length()").value(2));

        mvc.perform(ask("How do I fix my electricity bill on an island?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions.length()").value(0))
                .andExpect(jsonPath("$.replyBn", containsString("আরও নির্দিষ্ট করে লিখুন")));
    }

    @Test
    void explicitEmergencyQuestionPointsToTheNationalEmergencyNumber() throws Exception {
        mvc.perform(ask("আগুন লেগেছে, জরুরি সাহায্য দরকার"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replyBn", containsString("999")))
                .andExpect(jsonPath("$.suggestions.length()").value(0));
    }

    @Test
    @Transactional
    void disabledServiceIsNotSuggested() throws Exception {
        jdbc.update("UPDATE service_definitions SET enabled = FALSE WHERE code = 'passport'");
        mvc.perform(ask("পাসপোর্ট করতে চাই"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions.length()").value(0));
    }

    @Test
    void visitorQuestionIsNotStoredAndCsrfIsRequired() throws Exception {
        Integer before = jdbc.queryForObject("SELECT COUNT(*) FROM assistance_requests", Integer.class);
        mvc.perform(ask("Private passport question"))
                .andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM assistance_requests", Integer.class))
                .isEqualTo(before);

        mvc.perform(post("/api/assistant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("text", "passport"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsBlankAndOversizedQuestions() throws Exception {
        mvc.perform(ask("  "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc.perform(ask("a".repeat(501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder ask(String text)
            throws Exception {
        return post("/api/assistant").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(Map.of("text", text)));
    }
}
