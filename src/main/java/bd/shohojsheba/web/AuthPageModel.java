package bd.shohojsheba.web;

import bd.shohojsheba.account.AccountPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "bd.shohojsheba.web")
public class AuthPageModel {
    @ModelAttribute
    public void accountNavigation(@AuthenticationPrincipal AccountPrincipal principal, Model model) {
        model.addAttribute("authenticated", principal != null);
        model.addAttribute("currentUserName", principal == null ? null : principal.displayName());
    }
}
