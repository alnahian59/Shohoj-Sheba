package bd.shohojsheba.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.LocaleResolver;

@ControllerAdvice(basePackages = "bd.shohojsheba.web")
public class LanguagePageModel {
    private final LocaleResolver localeResolver;

    public LanguagePageModel(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @ModelAttribute
    public void languageNavigation(HttpServletRequest request, Model model) {
        model.addAttribute("english", "en".equals(localeResolver.resolveLocale(request).getLanguage()));
        String path = request.getServletPath();
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            path += "?" + query;
        }
        model.addAttribute("currentPath", LanguageController.safeReturnPath(path));
    }
}
