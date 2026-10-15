package bd.shohojsheba.dto;

import java.time.Instant;

public record RequestView(
        String reference,
        String serviceCode,
        String question,
        String state,
        Instant createdAt,
        long version
) {
    @Override
    public String toString() {
        return "RequestView[reference=" + reference + ", serviceCode=" + serviceCode
                + ", question=[REDACTED], state=" + state + ", createdAt=" + createdAt
                + ", version=" + version + "]";
    }
}
