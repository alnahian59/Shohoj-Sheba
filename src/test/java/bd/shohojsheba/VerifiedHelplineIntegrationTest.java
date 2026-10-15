package bd.shohojsheba;

import bd.shohojsheba.catalog.CatalogService;
import bd.shohojsheba.dto.HelplineView;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class VerifiedHelplineIntegrationTest {
    @Autowired CatalogService catalog;

    @Test
    void publicHelplinesContainOnlyReviewedOfficialContacts() {
        Map<String, HelplineView> contacts = catalog.listHelplines().stream()
                .collect(Collectors.toMap(HelplineView::nameEn, contact -> contact));

        assertThat(contacts).hasSize(6)
                .containsKeys("National Emergency Service", "Health Helpline",
                        "National Information Helpline", "Anti-Corruption Commission",
                        "Women & Child Abuse Prevention", "Fire Service & Civil Defence")
                .doesNotContainKeys("Police Headquarters", "Immigration Helpline");
        assertThat(contacts.get("Women & Child Abuse Prevention").number()).isEqualTo("109");
        assertThat(contacts.get("Fire Service & Civil Defence").number()).isEqualTo("102");
        assertThat(contacts.values()).allSatisfy(contact -> {
            assertThat(contact.sourceUrl()).startsWith("https://");
            assertThat(contact.reviewedOn()).isEqualTo(LocalDate.of(2026, 10, 14));
        });
    }
}
