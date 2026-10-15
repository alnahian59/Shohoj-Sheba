package bd.shohojsheba.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRequestInput(
        @NotBlank String reference,
        @NotBlank String trackingCode,
        @Size(max = 500) String question,
        @NotNull @Min(0) Long expectedVersion
) {
    @Override
    public String toString() {
        return "UpdateRequestInput[reference=" + reference + ", trackingCode=[REDACTED], "
                + "question=[REDACTED], expectedVersion=" + expectedVersion + "]";
    }
}
