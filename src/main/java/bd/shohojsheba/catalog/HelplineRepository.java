package bd.shohojsheba.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelplineRepository extends JpaRepository<Helpline, Integer> {
    List<Helpline> findByEnabledTrueOrderByDisplayOrderAsc();
}
