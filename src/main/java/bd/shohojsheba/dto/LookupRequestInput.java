package bd.shohojsheba.dto;

import jakarta.validation.constraints.NotBlank;

public record LookupRequestInput(
        @NotBlank String reference,
        @NotBlank String trackingCode
) {
    @Override
    public String toString() {
        return "LookupRequestInput[reference=" + reference + ", trackingCode=[REDACTED]]";
    }
}
