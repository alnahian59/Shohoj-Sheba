package bd.shohojsheba.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceDefinitionRepository extends JpaRepository<ServiceDefinition, String> {
    List<ServiceDefinition> findByEnabledTrueOrderByCodeAsc();

    Optional<ServiceDefinition> findByCodeAndEnabledTrue(String code);
}
