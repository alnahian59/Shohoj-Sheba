package bd.shohojsheba.dto;

import java.time.Instant;

/** The tracking code is a bearer secret and appears only in this creation response. */
public record CreatedRequest(
        String reference,
        String trackingCode,
        String serviceCode,
        String state,
        Instant createdAt,
        long version
) {
    @Override
    public String toString() {
        return "CreatedRequest[reference=" + reference + ", trackingCode=[REDACTED], serviceCode="
                + serviceCode + ", state=" + state + ", createdAt=" + createdAt
                + ", version=" + version + "]";
    }
}
