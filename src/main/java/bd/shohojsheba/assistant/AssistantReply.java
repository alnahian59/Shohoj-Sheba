package bd.shohojsheba.assistant;

import bd.shohojsheba.dto.ServiceSuggestion;
import java.util.List;

public record AssistantReply(String replyBn, String replyEn, List<ServiceSuggestion> suggestions) {
}
