package bd.shohojsheba.dto;

import jakarta.validation.constraints.Size;

public record UpdateOwnedRequestInput(
        @Size(max = 500) String question,
        long expectedVersion
) {
    @Override
    public String toString() {
        return "UpdateOwnedRequestInput[question=[REDACTED], expectedVersion=" + expectedVersion + "]";
    }
}
