package bd.shohojsheba.assistant;

import bd.shohojsheba.catalog.CatalogService;
import bd.shohojsheba.dto.ServiceSuggestion;
import bd.shohojsheba.dto.ServiceSummary;
import bd.shohojsheba.intent.IntentResolver;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Grounded service navigation for typed questions and browser speech transcripts.
 * It produces only catalogue suggestions and fixed guidance; visitor text is not saved.
 */
@Service
public class AssistantService {
    private static final Map<String, Signals> SIGNALS = Map.ofEntries(
            Map.entry("passport", new Signals(
                    List.of("পাসপোর্ট", "ই পাসপোর্ট"),
                    List.of("passport", "passports", "e passport", "epassport", "pass port",
                            "travel document", "pasport"))),
            Map.entry("birth-registration", new Signals(
                    List.of("জন্ম", "জন্মনিবন্ধন", "জন্ম সনদ"),
                    List.of("birth", "birth certificate", "birth registration", "jonmo", "jonmo nibondhon",
                            "jonmo shonod", "jonmo sonod"))),
            Map.entry("land-service", new Signals(
                    List.of("জমি", "ভূমি", "খতিয়ান", "খতিয়ান", "নামজারি", "দলিল"),
                    List.of("land", "property", "mutation", "khatian", "namjari", "jomi", "bhumi"))),
            Map.entry("death-registration", new Signals(
                    List.of("মৃত্যু", "মৃত্যু নিবন্ধন", "মৃত্যু সনদ"),
                    List.of("death", "death certificate", "death registration", "mrittu", "mrityu"))),
            Map.entry("nid", new Signals(
                    List.of("জাতীয় পরিচয়পত্র", "জাতীয় পরিচয়পত্র", "এনআইডি", "ভোটার আইডি",
                            "ভোটার পরিচয়পত্র", "ভোটার পরিচয়পত্র"),
                    List.of("nid", "national id", "national identity card", "voter id", "voter card"))),
            Map.entry("visa", new Signals(
                    List.of("ভিসা", "দূতাবাস", "কনস্যুলেট"),
                    List.of("visa", "embassy", "consulate", "bhisa"))),
            Map.entry("driving-license", new Signals(
                    List.of("ড্রাইভিং", "বিআরটিএ", "চালকের লাইসেন্স", "গাড়ি চালানো", "গাড়ি চালানো"),
                    List.of("driving", "driver license", "driver licence", "brta", "gari chalano"))),
            Map.entry("municipal-service", new Signals(
                    List.of("পৌরসভা", "সিটি কর্পোরেশন", "সিটি করপোরেশন", "ট্রেড লাইসেন্স",
                            "হোল্ডিং ট্যাক্স", "নাগরিক সনদ"),
                    List.of("municipal", "city corporation", "trade license", "trade licence",
                            "holding tax", "citizen certificate", "pourashava")))
    );

    private static final String VERIFY_SOURCE =
            "বর্তমান নিয়ম, ফি ও প্রয়োজনীয় কাগজপত্র সংশ্লিষ্ট সরকারি উৎসে যাচাই করুন।";
    private static final String VERIFY_SOURCE_EN =
            "Check current rules, fees, and required documents with the responsible government source.";
    private final CatalogService catalog;
    private final IntentResolver intentResolver;

    public AssistantService(CatalogService catalog, IntentResolver intentResolver) {
        this.catalog = catalog;
        this.intentResolver = intentResolver;
    }

    public AssistantReply reply(String text) {
        String query = normalize(text);
        if (isEmergency(query)) {
            return new AssistantReply("জরুরি বিপদে থাকলে বাংলাদেশে জাতীয় জরুরি সেবা 999-এ কল করুন। আরও তথ্যের জন্য হেল্পলাইন পৃষ্ঠা দেখুন।",
                    "If you are in immediate danger in Bangladesh, call the national emergency service at 999. See the helplines page for more information.",
                    List.of());
        }
        Set<String> baselineCodes = intentResolver.suggest(text).stream()
                .map(ServiceSuggestion::code)
                .collect(java.util.stream.Collectors.toSet());
        List<ServiceSummary> enabled = catalog.listEnabled();
        Map<String, Integer> scores = new HashMap<>();
        for (ServiceSummary service : enabled) {
            int score = score(query, SIGNALS.get(service.code()));
            if (score > 0) {
                scores.put(service.code(), score + (baselineCodes.contains(service.code()) ? 1 : 0));
            }
        }

        if (scores.isEmpty()) {
            addGenericChoices(query, enabled, scores);
        }

        List<ServiceSuggestion> suggestions = enabled.stream()
                .filter(service -> scores.containsKey(service.code()))
                .sorted(Comparator.<ServiceSummary>comparingInt(service -> scores.get(service.code()))
                        .reversed().thenComparing(ServiceSummary::code))
                .limit(3)
                .map(service -> new ServiceSuggestion(service.code(), service.titleBn(), service.titleEn()))
                .toList();

        if (suggestions.isEmpty()) {
            return new AssistantReply("প্রশ্নটি কোন সেবার সঙ্গে মেলে তা বুঝতে পারিনি। সেবার নাম বা বিষয়টি আরও নির্দিষ্ট করে লিখুন, অথবা সেবার তালিকা দেখুন। "
                    + VERIFY_SOURCE,
                    "I could not match your question to a service. Try a more specific service name or topic, or browse the service list. "
                            + VERIFY_SOURCE_EN, suggestions);
        }
        if (suggestions.size() > 1) {
            return new AssistantReply("আপনার কথায় একাধিক সেবার ইঙ্গিত আছে। নিচের সেবাগুলোর মধ্যে যেটি প্রয়োজন, তার নির্দেশিকা খুলুন। "
                    + VERIFY_SOURCE,
                    "Your question may relate to more than one service. Open the guide that matches your need below. "
                            + VERIFY_SOURCE_EN, suggestions);
        }
        return new AssistantReply(suggestions.getFirst().titleBn()
                + " বিষয়ে একটি নমুনা নির্দেশিকা পাওয়া গেছে। নিচের লিংক থেকে নির্দেশিকাটি দেখুন। "
                + VERIFY_SOURCE,
                "I found a sample guide for " + suggestions.getFirst().titleEn()
                        + ". Open the guide using the link below. " + VERIFY_SOURCE_EN,
                suggestions);
    }

    private static int score(String query, Signals signals) {
        if (signals == null) {
            return 0;
        }
        int score = 0;
        for (String cue : signals.bangla()) {
            if (query.contains(normalize(cue))) {
                score = Math.max(score, cue.length());
            }
        }
        for (String cue : signals.english()) {
            String normalizedCue = normalize(cue);
            if (Pattern.compile("(?<![a-z])" + Pattern.quote(normalizedCue) + "(?![a-z])")
                    .matcher(query).find()) {
                score = Math.max(score, normalizedCue.length());
            }
        }
        return score;
    }

    private static void addGenericChoices(String query, List<ServiceSummary> enabled,
            Map<String, Integer> scores) {
        List<String> choices = new ArrayList<>();
        if (query.contains("লাইসেন্স") || containsEnglish(query, "license")
                || containsEnglish(query, "licence")) {
            choices.addAll(List.of("driving-license", "municipal-service"));
        } else if (query.contains("নিবন্ধন") || containsEnglish(query, "registration")) {
            choices.addAll(List.of("birth-registration", "death-registration"));
        }
        for (ServiceSummary service : enabled) {
            if (choices.contains(service.code())) {
                scores.put(service.code(), 1);
            }
        }
    }

    private static boolean containsEnglish(String query, String phrase) {
        return Pattern.compile("(?<![a-z])" + Pattern.quote(phrase) + "(?![a-z])")
                .matcher(query).find();
    }

    private static boolean isEmergency(String query) {
        return List.of("আগুন লেগেছে", "দুর্ঘটনা", "জরুরি সাহায্য", "বিপদে আছি",
                        "অ্যাম্বুলেন্স", "পুলিশ ডাকুন").stream().anyMatch(query::contains)
                || List.of("fire emergency", "accident", "need ambulance", "call police",
                        "emergency help", "fire service number").stream()
                        .anyMatch(phrase -> containsEnglish(query, phrase));
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\p{Punct}।]+", " ")
                .replaceAll("\\s+", " ")
                .strip();
    }

    private record Signals(List<String> bangla, List<String> english) {
    }
}
