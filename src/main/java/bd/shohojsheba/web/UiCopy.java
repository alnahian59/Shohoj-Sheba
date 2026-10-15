package bd.shohojsheba.web;

import org.springframework.context.i18n.LocaleContextHolder;

/** Selects server-generated page copy using the visitor's language cookie. */
final class UiCopy {
    private UiCopy() {
    }

    static boolean english() {
        return "en".equals(LocaleContextHolder.getLocale().getLanguage());
    }

    static String text(String bangla, String english) {
        return english() ? english : bangla;
    }
}
