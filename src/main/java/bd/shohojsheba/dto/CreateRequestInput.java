package bd.shohojsheba.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRequestInput(
        @NotBlank @Size(max = 64) String serviceCode,
        @Size(max = 500) String question
) {
    @Override
    public String toString() {
        return "CreateRequestInput[serviceCode=" + serviceCode + ", question=[REDACTED]]";
    }
}
