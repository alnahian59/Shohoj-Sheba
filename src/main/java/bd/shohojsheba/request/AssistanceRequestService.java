package bd.shohojsheba.request;

import bd.shohojsheba.account.AppUserRepository;
import bd.shohojsheba.catalog.ServiceDefinition;
import bd.shohojsheba.catalog.ServiceDefinitionRepository;
import bd.shohojsheba.catalog.ServiceNotFoundException;
import bd.shohojsheba.dto.CreatedRequest;
import bd.shohojsheba.dto.RequestView;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssistanceRequestService {
    private static final Pattern REFERENCE = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    private static final Pattern TRACKING_CODE = Pattern.compile("[A-Za-z0-9_-]{43}");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AssistanceRequestRepository requests;
    private final ServiceDefinitionRepository services;
    private final AppUserRepository users;

    public AssistanceRequestService(AssistanceRequestRepository requests,
            ServiceDefinitionRepository services, AppUserRepository users) {
        this.requests = requests;
        this.services = services;
        this.users = users;
    }

    @Transactional
    public CreatedRequest create(String serviceCode, String question, String ownerId) {
        String normalizedCode = normalizeServiceCode(serviceCode);
        String normalizedQuestion = normalizeQuestion(question);
        ServiceDefinition service = services.findByCodeAndEnabledTrue(normalizedCode)
                .orElseThrow(ServiceNotFoundException::new);

        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        String trackingCode = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String reference = UUID.randomUUID().toString();
        AssistanceRequest request = new AssistanceRequest(reference, service,
                users.getReferenceById(ownerId), normalizedQuestion,
                sha256Hex(trackingCode), Instant.now());
        requests.saveAndFlush(request);
        return new CreatedRequest(request.getReference(), trackingCode, request.getServiceCode(),
                request.getState().name(), request.getCreatedAt(), request.getVersion());
    }

    @Transactional(readOnly = true)
    public RequestView lookup(String reference, String trackingCode, String ownerId) {
        return view(authorizeOwnedByToken(reference, trackingCode, ownerId));
    }

    @Transactional(readOnly = true)
    public List<RequestView> listOwned(String ownerId) {
        return requests.findByOwner_IdOrderByCreatedAtDesc(ownerId).stream()
                .map(AssistanceRequestService::view)
                .toList();
    }

    @Transactional(readOnly = true)
    public RequestView getOwned(String reference, String ownerId) {
        return view(ownedRequest(reference, ownerId));
    }

    @Transactional
    public RequestView update(String reference, String trackingCode, String question,
            long expectedVersion, String ownerId) {
        return updateRequest(authorizeOwnedByToken(reference, trackingCode, ownerId),
                question, expectedVersion);
    }

    @Transactional
    public RequestView updateOwned(String reference, String question, long expectedVersion,
            String ownerId) {
        return updateRequest(ownedRequest(reference, ownerId), question, expectedVersion);
    }

    @Transactional
    public RequestView claimLegacy(String reference, String trackingCode, String ownerId) {
        AssistanceRequest request = requestWithToken(reference, trackingCode);
        if (request.getOwnerId() != null && !request.getOwnerId().equals(ownerId)) {
            throw new RequestNotFoundException();
        }
        if (request.getOwnerId() == null) {
            request.assignOwner(users.getReferenceById(ownerId));
            try {
                requests.saveAndFlush(request);
            } catch (OptimisticLockingFailureException exception) {
                throw new StaleRequestException();
            }
        }
        return view(request);
    }

    private RequestView updateRequest(AssistanceRequest request, String question,
            long expectedVersion) {
        if (expectedVersion < 0) {
            throw new InvalidRequestException();
        }
        if (request.getVersion() != expectedVersion) {
            throw new StaleRequestException();
        }
        request.updateQuestion(normalizeQuestion(question));
        try {
            requests.saveAndFlush(request);
        } catch (OptimisticLockingFailureException exception) {
            throw new StaleRequestException();
        }
        return view(request);
    }

    private AssistanceRequest ownedRequest(String reference, String ownerId) {
        String normalizedReference = normalizedReference(reference);
        return requests.findByReferenceAndOwner_Id(normalizedReference, ownerId)
                .orElseThrow(RequestNotFoundException::new);
    }

    private AssistanceRequest authorizeOwnedByToken(String reference, String trackingCode,
            String ownerId) {
        AssistanceRequest request = ownedRequest(reference, ownerId);
        verifyToken(request, trackingCode);
        return request;
    }

    private AssistanceRequest requestWithToken(String reference, String trackingCode) {
        AssistanceRequest request = requests.findById(normalizedReference(reference))
                .orElseThrow(RequestNotFoundException::new);
        verifyToken(request, trackingCode);
        return request;
    }

    private static String normalizedReference(String reference) {
        String normalizedReference = reference == null ? "" : reference.strip();
        if (!REFERENCE.matcher(normalizedReference).matches()) {
            throw new RequestNotFoundException();
        }
        return normalizedReference;
    }

    private static void verifyToken(AssistanceRequest request, String trackingCode) {
        if (trackingCode == null || !TRACKING_CODE.matcher(trackingCode).matches()) {
            throw new RequestNotFoundException();
        }
        byte[] actualHash = sha256(trackingCode);
        byte[] expectedHash = HexFormat.of().parseHex(request.getTrackingHash());
        if (!MessageDigest.isEqual(expectedHash, actualHash)) {
            throw new RequestNotFoundException();
        }
    }

    private static String normalizeServiceCode(String code) {
        if (code == null || code.isBlank() || code.length() > 64) {
            throw new InvalidRequestException();
        }
        return code.strip().toLowerCase(Locale.ROOT);
    }

    private static String normalizeQuestion(String question) {
        String normalized = question == null ? "" : question.strip();
        if (normalized.length() > 500 || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new InvalidRequestException();
        }
        return normalized;
    }

    private static String sha256Hex(String value) {
        return HexFormat.of().formatHex(sha256(value));
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static RequestView view(AssistanceRequest request) {
        return new RequestView(request.getReference(), request.getServiceCode(), request.getQuestion(),
                request.getState().name(), request.getCreatedAt(), request.getVersion());
    }
}
