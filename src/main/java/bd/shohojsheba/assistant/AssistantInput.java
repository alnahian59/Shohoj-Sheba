package bd.shohojsheba.assistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantInput(@NotBlank @Size(max = 500) String text) {
    @Override
    public String toString() {
        return "AssistantInput[text=[REDACTED]]";
    }
}
