package bd.shohojsheba.config;

import java.time.Duration;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

@Configuration
public class LocaleConfig {
    @Bean
    LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("SHOHOJ_LANGUAGE");
        resolver.setDefaultLocale(Locale.forLanguageTag("bn"));
        resolver.setCookiePath("/");
        resolver.setCookieHttpOnly(true);
        resolver.setCookieSameSite("Lax");
        resolver.setCookieMaxAge(Duration.ofDays(365));
        return resolver;
    }
}
