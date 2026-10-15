package bd.shohojsheba.request;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistanceRequestRepository extends JpaRepository<AssistanceRequest, String> {
    Optional<AssistanceRequest> findByReferenceAndOwner_Id(String reference, String ownerId);

    List<AssistanceRequest> findByOwner_IdOrderByCreatedAtDesc(String ownerId);
}
