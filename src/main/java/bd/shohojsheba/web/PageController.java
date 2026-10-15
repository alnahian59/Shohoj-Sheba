package bd.shohojsheba.web;

import bd.shohojsheba.account.AccountPrincipal;
import bd.shohojsheba.catalog.CatalogService;
import bd.shohojsheba.catalog.ServiceNotFoundException;
import bd.shohojsheba.dto.CreatedRequest;
import bd.shohojsheba.dto.RequestView;
import bd.shohojsheba.intent.IntentResolver;
import bd.shohojsheba.request.AssistanceRequestService;
import bd.shohojsheba.request.InvalidRequestException;
import bd.shohojsheba.request.RequestNotFoundException;
import bd.shohojsheba.request.StaleRequestException;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PageController {
    private static final int MAX_QUESTION_LENGTH = 500;
    private final CatalogService catalog;
    private final IntentResolver intentResolver;
    private final AssistanceRequestService requests;

    public PageController(CatalogService catalog, IntentResolver intentResolver,
            AssistanceRequestService requests) {
        this.catalog = catalog;
        this.intentResolver = intentResolver;
        this.requests = requests;
    }

    @GetMapping("/")
    public String entry(@AuthenticationPrincipal AccountPrincipal principal) {
        return principal == null ? "redirect:/login" : "redirect:/services";
    }

    @GetMapping("/services")
    public String home(Model model) {
        model.addAttribute("services", catalog.listEnabled());
        return "home";
    }

    @PostMapping("/suggest")
    public String suggest(@RequestParam(name = "q", defaultValue = "") String query,
                          Model model, HttpServletResponse response) {
        noStore(response);
        model.addAttribute("services", catalog.listEnabled());
        String text = query.trim();
        model.addAttribute("query", text);
        if (text.isEmpty()) {
            model.addAttribute("suggestionError", UiCopy.text("আপনার প্রয়োজন কয়েকটি কথায় লিখুন।",
                    "Describe what you need in a few words."));
        } else if (text.length() > MAX_QUESTION_LENGTH) {
            model.addAttribute("suggestionError", UiCopy.text("সর্বোচ্চ ৫০০ অক্ষরে লিখুন।",
                    "Use no more than 500 characters."));
        } else {
            model.addAttribute("suggestions", intentResolver.suggest(text));
        }
        return "home";
    }

    @GetMapping("/services/{code}")
    public String guide(@PathVariable String code, Model model) {
        model.addAttribute("guide", catalog.getEnabled(code));
        return "guide";
    }

    @GetMapping("/drafts/new")
    public String newDraft(@RequestParam(name = "service", defaultValue = "") String serviceCode,
                           Model model) {
        model.addAttribute("guide", catalog.getEnabled(serviceCode));
        return "new-draft";
    }

    @PostMapping("/drafts")
    public String createDraft(@RequestParam(name = "serviceCode", defaultValue = "") String serviceCode,
                              @RequestParam(name = "question", defaultValue = "") String question,
                              @AuthenticationPrincipal AccountPrincipal principal,
                              Model model, RedirectAttributes redirectAttributes,
                              HttpServletResponse response) {
        noStore(response);
        var guide = catalog.getEnabled(serviceCode);
        String text = question.trim();
        if (text.length() > MAX_QUESTION_LENGTH) {
            model.addAttribute("guide", guide);
            model.addAttribute("question", text);
            model.addAttribute("formError", UiCopy.text("সর্বোচ্চ ৫০০ অক্ষরে লিখুন।",
                    "Use no more than 500 characters."));
            return "new-draft";
        }
        try {
            CreatedRequest created = requests.create(serviceCode, text, principal.id());
            // Spring MVC flash attributes live in the server session for this single redirect.
            redirectAttributes.addFlashAttribute("created", created);
            return "redirect:/drafts/created";
        } catch (InvalidRequestException ex) {
            model.addAttribute("guide", guide);
            model.addAttribute("question", text);
            model.addAttribute("formError", UiCopy.text("খসড়া তৈরি করা যায়নি। লেখাটি যাচাই করে আবার চেষ্টা করুন।",
                    "Could not create the draft. Check your text and try again."));
            return "new-draft";
        }
    }

    @GetMapping("/drafts/created")
    public String draftCreated(Model model, HttpServletResponse response) {
        noStore(response);
        if (!model.containsAttribute("created")) {
            return "redirect:/";
        }
        return "draft-created";
    }

    @GetMapping("/resume")
    public String resumeForm(HttpServletResponse response) {
        noStore(response);
        return "resume";
    }

    @PostMapping("/resume")
    public String resume(@RequestParam(name = "reference", defaultValue = "") String reference,
                         @RequestParam(name = "trackingCode", defaultValue = "") String trackingCode,
                         @AuthenticationPrincipal AccountPrincipal principal,
                         Model model, HttpServletResponse response) {
        noStore(response);
        if (reference.isBlank() || trackingCode.isBlank()) {
            model.addAttribute("formError", UiCopy.text("রেফারেন্স ও ব্যক্তিগত কোড দুটোই লিখুন।",
                    "Enter both the reference and private code."));
            return "resume";
        }
        try {
            return showDraft(requests.claimLegacy(reference.trim(), trackingCode.trim(), principal.id()),
                    trackingCode.trim(), model);
        } catch (RequestNotFoundException | InvalidRequestException ex) {
            model.addAttribute("formError", UiCopy.text("রেফারেন্স ও কোড মেলেনি। তথ্য দুটি যাচাই করে আবার চেষ্টা করুন।",
                    "The reference and code did not match. Check both and try again."));
            return "resume";
        }
    }

    @PostMapping("/drafts/update")
    public String updateDraft(@RequestParam(name = "reference", defaultValue = "") String reference,
                              @RequestParam(name = "trackingCode", defaultValue = "") String trackingCode,
                              @RequestParam(name = "question", defaultValue = "") String question,
                              @RequestParam(name = "expectedVersion") long expectedVersion,
                              @AuthenticationPrincipal AccountPrincipal principal,
                              Model model, HttpServletResponse response) {
        noStore(response);
        if (question.trim().length() > MAX_QUESTION_LENGTH) {
            return showDraftWithError(reference, trackingCode, question, principal.id(), model,
                    UiCopy.text("সর্বোচ্চ ৫০০ অক্ষরে লিখুন।", "Use no more than 500 characters."));
        }
        try {
            RequestView updated = requests.update(reference.trim(), trackingCode.trim(),
                    question.trim(), expectedVersion, principal.id());
            model.addAttribute("success", UiCopy.text("আপনার স্থানীয় খসড়া সংরক্ষিত হয়েছে।",
                    "Your local draft was saved."));
            return showDraft(updated, trackingCode.trim(), model);
        } catch (StaleRequestException ex) {
            model.addAttribute("formError", UiCopy.text("খসড়াটি বদলে গেছে। রেফারেন্স ও কোড দিয়ে আবার খুলুন।",
                    "This draft has changed. Reopen it with the reference and code."));
            return "resume";
        } catch (RequestNotFoundException ex) {
            model.addAttribute("formError", UiCopy.text("রেফারেন্স ও কোড মেলেনি। তথ্য দুটি যাচাই করে আবার চেষ্টা করুন।",
                    "The reference and code did not match. Check both and try again."));
            return "resume";
        } catch (InvalidRequestException ex) {
            return showDraftWithError(reference, trackingCode, question, principal.id(), model,
                    UiCopy.text("লেখাটি সংরক্ষণ করা যায়নি। যাচাই করে আবার চেষ্টা করুন।",
                            "Could not save the text. Check it and try again."));
        }
    }

    @GetMapping("/my/drafts")
    public String myDrafts(@AuthenticationPrincipal AccountPrincipal principal,
            Model model, HttpServletResponse response) {
        noStore(response);
        model.addAttribute("drafts", requests.listOwned(principal.id()));
        model.addAttribute("serviceTitles", catalog.listEnabled().stream()
                .collect(Collectors.toMap(service -> service.code(),
                        service -> UiCopy.english() ? service.titleEn() : service.titleBn())));
        return "my-drafts";
    }

    @GetMapping("/drafts/{reference}")
    public String ownDraft(@PathVariable String reference,
            @AuthenticationPrincipal AccountPrincipal principal,
            Model model, HttpServletResponse response) {
        noStore(response);
        return showDraft(requests.getOwned(reference, principal.id()), null, model);
    }

    @PostMapping("/drafts/{reference}/update")
    public String updateOwnDraft(@PathVariable String reference,
            @RequestParam(name = "question", defaultValue = "") String question,
            @RequestParam(name = "expectedVersion") long expectedVersion,
            @AuthenticationPrincipal AccountPrincipal principal,
            Model model, HttpServletResponse response) {
        noStore(response);
        if (question.trim().length() > MAX_QUESTION_LENGTH) {
            return showOwnedDraftWithError(reference, question, principal.id(), model,
                    UiCopy.text("সর্বোচ্চ ৫০০ অক্ষরে লিখুন।", "Use no more than 500 characters."));
        }
        try {
            RequestView updated = requests.updateOwned(reference, question.trim(),
                    expectedVersion, principal.id());
            model.addAttribute("success", UiCopy.text("আপনার স্থানীয় খসড়া সংরক্ষিত হয়েছে।",
                    "Your local draft was saved."));
            return showDraft(updated, null, model);
        } catch (StaleRequestException exception) {
            return showOwnedDraftWithError(reference, question, principal.id(), model,
                    UiCopy.text("খসড়াটি বদলে গেছে। আবার খুলে নতুন সংস্করণে সম্পাদনা করুন।",
                            "This draft has changed. Reopen it and edit the latest version."));
        } catch (InvalidRequestException exception) {
            return showOwnedDraftWithError(reference, question, principal.id(), model,
                    UiCopy.text("লেখাটি সংরক্ষণ করা যায়নি। যাচাই করে আবার চেষ্টা করুন।",
                            "Could not save the text. Check it and try again."));
        }
    }

    @GetMapping("/helplines")
    public String helplines(Model model) {
        model.addAttribute("helplines", catalog.listHelplines());
        return "helplines";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    private String showDraftWithError(String reference, String trackingCode, String question,
                                      String ownerId,
                                      Model model, String error) {
        try {
            RequestView draft = requests.lookup(reference.trim(), trackingCode.trim(), ownerId);
            model.addAttribute("question", question);
            model.addAttribute("formError", error);
            return showDraft(draft, trackingCode.trim(), model);
        } catch (RequestNotFoundException | InvalidRequestException ex) {
            model.addAttribute("formError", UiCopy.text("রেফারেন্স ও কোড মেলেনি। তথ্য দুটি যাচাই করে আবার চেষ্টা করুন।",
                    "The reference and code did not match. Check both and try again."));
            return "resume";
        }
    }

    private String showOwnedDraftWithError(String reference, String question, String ownerId,
            Model model, String error) {
        RequestView draft = requests.getOwned(reference, ownerId);
        model.addAttribute("question", question);
        model.addAttribute("formError", error);
        return showDraft(draft, null, model);
    }

    private String showDraft(RequestView draft, String trackingCode, Model model) {
        model.addAttribute("draft", draft);
        model.addAttribute("trackingCode", trackingCode);
        model.addAttribute("serviceTitle", catalog.listEnabled().stream()
                .filter(service -> service.code().equals(draft.serviceCode()))
                .map(service -> UiCopy.english() ? service.titleEn() : service.titleBn())
                .findFirst().orElse(draft.serviceCode()));
        return "draft";
    }

    private static void noStore(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
    }

    @ExceptionHandler(ServiceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String serviceNotFound(Model model) {
        model.addAttribute("errorTitle", UiCopy.text("সেবাটি পাওয়া যায়নি", "Service not found"));
        model.addAttribute("errorMessage", UiCopy.text("সেবার তালিকা থেকে আবার শুরু করুন।",
                "Start again from the service list."));
        return "site-error";
    }

    @ExceptionHandler(RequestNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String requestNotFound(Model model, HttpServletResponse response) {
        noStore(response);
        model.addAttribute("errorTitle", UiCopy.text("খসড়াটি পাওয়া যায়নি", "Draft not found"));
        model.addAttribute("errorMessage", UiCopy.text("আপনার খসড়ার তালিকা থেকে আবার চেষ্টা করুন।",
                "Try again from your drafts list."));
        return "site-error";
    }
}
