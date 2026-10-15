package bd.shohojsheba.intent;

import bd.shohojsheba.dto.ServiceSuggestion;
import java.util.List;

/** Suggests catalogue entries from typed text; it makes no official determination. */
public interface IntentResolver {
    List<ServiceSuggestion> suggest(String text);
}
