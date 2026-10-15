package bd.shohojsheba.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;

@Controller
public class LanguageController {
    private final LocaleResolver localeResolver;

    public LanguageController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @GetMapping("/language")
    public String selectLanguage(@RequestParam String lang,
            @RequestParam(defaultValue = "/services") String returnTo,
            HttpServletRequest request, HttpServletResponse response) {
        Locale selected = switch (lang) {
            case "bn" -> Locale.forLanguageTag("bn");
            case "en" -> Locale.ENGLISH;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown language");
        };
        localeResolver.setLocale(request, response, selected);
        return "redirect:" + safeReturnPath(returnTo);
    }

    static String safeReturnPath(String path) {
        if (path == null || path.length() > 1500 || !path.startsWith("/")
                || path.startsWith("//") || path.contains("\\")) {
            return "/services";
        }
        try {
            URI target = new URI(path);
            String localPath = target.getRawPath();
            String query = target.getRawQuery();
            if (!target.isAbsolute() && target.getRawAuthority() == null && target.getRawFragment() == null
                    && localPath != null && localPath.matches("/[A-Za-z0-9/_-]*")
                    && !localPath.equals("/language")
                    && (query == null || query.matches("[A-Za-z0-9._~!$&'()*+,;=:@/?%-]*"))) {
                return path;
            }
        } catch (URISyntaxException ignored) {
            // Invalid or unsafe return targets go back to the public services page.
        }
        return "/services";
    }
}
