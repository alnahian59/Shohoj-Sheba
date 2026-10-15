package bd.shohojsheba.intent;

import bd.shohojsheba.catalog.CatalogService;
import bd.shohojsheba.dto.ServiceSuggestion;
import bd.shohojsheba.dto.ServiceSummary;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

/** A small, explainable starting point for Bangla and English typed requests. */
@Service
public class KeywordIntentResolver implements IntentResolver {
    private static final Map<String, List<String>> KEYWORDS = Map.ofEntries(
            Map.entry("passport", List.of("পাসপোর্ট", "ই-পাসপোর্ট", "ই পাসপোর্ট", "passport", "e-passport")),
            Map.entry("birth-registration", List.of("জন্ম", "জন্মনিবন্ধন", "জন্ম সনদ", "birth", "birth certificate")),
            Map.entry("land-service", List.of("জমি", "ভূমি", "খতিয়ান", "নামজারি",
                    "land", "property", "mutation")),
            Map.entry("death-registration", List.of("মৃত্যু", "মৃত্যু নিবন্ধন", "মৃত্যু সনদ",
                    "death", "death certificate", "death registration")),
            Map.entry("nid", List.of("জাতীয় পরিচয়পত্র", "এনআইডি", "ভোটার আইডি", "পরিচয়পত্র",
                    "nid", "national id", "voter id")),
            Map.entry("visa", List.of("ভিসা", "দূতাবাস", "কনস্যুলেট",
                    "visa", "embassy", "consulate", "travel")),
            Map.entry("driving-license", List.of("ড্রাইভিং লাইসেন্স", "ড্রাইভিং", "গাড়ি চালানো", "বিআরটিএ",
                    "driving license", "driving", "brta", "driver")),
            Map.entry("municipal-service", List.of("পৌরসভা", "সিটি কর্পোরেশন", "ট্রেড লাইসেন্স", "হোল্ডিং ট্যাক্স",
                    "নাগরিক সনদ", "municipal", "city corporation", "trade license", "holding tax"))
    );

    private final CatalogService catalog;

    public KeywordIntentResolver(CatalogService catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<ServiceSuggestion> suggest(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String query = normalize(text);
        if (query.length() > 500) {
            query = query.substring(0, 500);
        }
        final String normalizedQuery = query;
        return catalog.listEnabled().stream()
                .map(service -> new RankedService(service, score(service, normalizedQuery)))
                .filter(item -> item.score() > 0)
                .sorted(Comparator.comparingInt(RankedService::score).reversed()
                        .thenComparing(item -> item.service().code()))
                .limit(3)
                .map(item -> new ServiceSuggestion(item.service().code(), item.service().titleBn(),
                        item.service().titleEn()))
                .toList();
    }

    private static int score(ServiceSummary service, String query) {
        int score = 0;
        for (String keyword : KEYWORDS.getOrDefault(service.code(), List.of())) {
            if (query.contains(normalize(keyword))) {
                score += keyword.length();
            }
        }
        String titleBn = normalize(service.titleBn());
        String titleEn = normalize(service.titleEn());
        if (query.contains(titleBn) || query.contains(titleEn)) {
            score += 20;
        }
        return score;
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT).strip();
    }

    private record RankedService(ServiceSummary service, int score) {
    }
}
