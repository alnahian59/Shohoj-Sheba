package bd.shohojsheba;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FloatingChatWidgetHttpIntegrationTest {
    @LocalServerPort int port;

    @Test
    void firstGuestRequestStreamsTheCompleteServicesPageAndChatForm() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/services"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        // A new client sends no session cookie, so rendering the form must create its CSRF token.
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Content-Type").orElse(""))
                    .startsWith("text/html");
            assertThat(response.body())
                    .contains("data-testid=\"chat-widget\"")
                    .contains("action=\"/services/chat\"")
                    .contains("name=\"_csrf\"")
                    .contains("id=\"services-heading\"");
            assertThat(response.body().stripTrailing()).endsWith("</html>");
        }
    }
}
