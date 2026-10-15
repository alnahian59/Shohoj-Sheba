package bd.shohojsheba.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuggestionInput(
        @NotBlank @Size(max = 500) String text
) {
    @Override
    public String toString() {
        return "SuggestionInput[text=[REDACTED]]";
    }
}
