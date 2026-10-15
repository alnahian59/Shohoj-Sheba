package bd.shohojsheba.web;

import bd.shohojsheba.account.AccountPrincipal;
import bd.shohojsheba.account.AccountService;
import bd.shohojsheba.account.RegistrationException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final AccountService accounts;

    public AuthController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping("/login")
    public String login(@RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "registered", required = false) String registered,
            @AuthenticationPrincipal AccountPrincipal principal,
            Model model, HttpServletResponse response) {
        noStore(response);
        if (principal != null) {
            return "redirect:/services";
        }
        model.addAttribute("loginError", error != null);
        model.addAttribute("registered", registered != null);
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(@AuthenticationPrincipal AccountPrincipal principal,
            HttpServletResponse response) {
        noStore(response);
        return principal == null ? "register" : "redirect:/services";
    }

    @PostMapping("/register")
    public String register(@RequestParam(name = "displayName", defaultValue = "") String displayName,
            @RequestParam(name = "email", defaultValue = "") String email,
            @RequestParam(name = "password", defaultValue = "") String password,
            @RequestParam(name = "confirmPassword", defaultValue = "") String confirmPassword,
            Model model, HttpServletResponse response) {
        noStore(response);
        try {
            accounts.register(displayName, email, password, confirmPassword);
            return "redirect:/login?registered";
        } catch (RegistrationException exception) {
            model.addAttribute("formError", UiCopy.english()
                    ? exception.getMessageEn() : exception.getMessage());
            model.addAttribute("displayName", displayName.strip());
            model.addAttribute("email", email.strip());
            return "register";
        }
    }

    private static void noStore(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
    }
}
