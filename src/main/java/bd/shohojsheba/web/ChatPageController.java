package bd.shohojsheba.web;

import bd.shohojsheba.assistant.AssistantService;
import bd.shohojsheba.assistant.AssistantReply;
import bd.shohojsheba.catalog.CatalogService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** A public, server-rendered chat turn. Questions are never written to a database. */
@Controller
public class ChatPageController {
    private final AssistantService assistant;
    private final CatalogService catalog;

    public ChatPageController(AssistantService assistant, CatalogService catalog) {
        this.assistant = assistant;
        this.catalog = catalog;
    }

    @GetMapping("/chat")
    public String chat(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return "chat";
    }

    @PostMapping("/chat")
    public String ask(@RequestParam(name = "text", defaultValue = "") String text,
            @RequestParam(name = "previous", required = false) List<String> previous,
            Model model, HttpServletResponse response) {
        return answer(text, previous, model, response, "chat");
    }

    @PostMapping("/services/chat")
    public String askFromServices(@RequestParam(name = "text", defaultValue = "") String text,
            @RequestParam(name = "previous", required = false) List<String> previous,
            Model model, HttpServletResponse response) {
        model.addAttribute("services", catalog.listEnabled());
        model.addAttribute("widgetOpen", true);
        return answer(text, previous, model, response, "home");
    }

    private String answer(String text, List<String> previous, Model model,
            HttpServletResponse response, String view) {
        response.setHeader("Cache-Control", "no-store");
        List<ChatTurn> turns = new ArrayList<>();
        if (previous != null) {
            for (String earlier : previous.subList(Math.max(0, previous.size() - 4), previous.size())) {
                if (earlier == null) {
                    continue;
                }
                String priorQuestion = earlier.strip();
                if (valid(priorQuestion)) {
                    turns.add(new ChatTurn(priorQuestion, assistant.reply(priorQuestion)));
                }
            }
        }
        model.addAttribute("turns", turns);
        String question = text.strip();
        if (!valid(question)) {
            model.addAttribute("formError", UiCopy.text("১ থেকে ৫০০ অক্ষরের মধ্যে আপনার প্রশ্নটি লিখুন।",
                    "Write a question between 1 and 500 characters."));
            if (question.length() <= 500) {
                model.addAttribute("question", question);
            }
            return view;
        }
        turns.add(new ChatTurn(question, assistant.reply(question)));
        return view;
    }

    private static boolean valid(String text) {
        return !text.isEmpty() && text.length() <= 500
                && text.codePoints().noneMatch(Character::isISOControl);
    }

    public record ChatTurn(String question, AssistantReply answer) {
    }
}
