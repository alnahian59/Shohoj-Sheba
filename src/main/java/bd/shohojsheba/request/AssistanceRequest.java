package bd.shohojsheba.request;

import bd.shohojsheba.account.AppUser;
import bd.shohojsheba.catalog.ServiceDefinition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "assistance_requests")
public class AssistanceRequest {
    @Id
    @Column(name = "reference", nullable = false, length = 36)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_code", referencedColumnName = "code", nullable = false)
    private ServiceDefinition service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private AppUser owner;

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 32)
    private RequestState state;

    @Column(name = "tracking_hash", nullable = false, length = 64)
    private String trackingHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected AssistanceRequest() {
    }

    AssistanceRequest(String reference, ServiceDefinition service, AppUser owner, String question,
            String trackingHash, Instant createdAt) {
        this.reference = reference;
        this.service = service;
        this.owner = owner;
        this.question = question;
        this.state = RequestState.DRAFT;
        this.trackingHash = trackingHash;
        this.createdAt = createdAt;
    }

    public String getReference() {
        return reference;
    }

    public String getServiceCode() {
        return service.getCode();
    }

    public String getOwnerId() {
        return owner == null ? null : owner.getId();
    }

    public String getQuestion() {
        return question;
    }

    public RequestState getState() {
        return state;
    }

    public String getTrackingHash() {
        return trackingHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getVersion() {
        return version == null ? 0 : version;
    }

    void updateQuestion(String question) {
        this.question = question;
    }

    void assignOwner(AppUser owner) {
        if (this.owner != null) {
            throw new IllegalStateException("Draft already belongs to an account");
        }
        this.owner = owner;
    }
}
