package bd.shohojsheba.catalog;

import bd.shohojsheba.dto.HelplineView;
import bd.shohojsheba.dto.ServiceGuide;
import bd.shohojsheba.dto.ServiceSummary;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
    private final ServiceDefinitionRepository repository;
    private final HelplineRepository helplineRepository;

    public CatalogService(ServiceDefinitionRepository repository,
            HelplineRepository helplineRepository) {
        this.repository = repository;
        this.helplineRepository = helplineRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceSummary> listEnabled() {
        return repository.findByEnabledTrueOrderByCodeAsc().stream()
                .map(service -> new ServiceSummary(service.getCode(), service.getTitleBn(),
                        service.getTitleEn(), service.getDescriptionBn(), service.getDescriptionEn()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceGuide getEnabled(String code) {
        String normalized = code == null ? "" : code.strip().toLowerCase(Locale.ROOT);
        ServiceDefinition service = repository.findByCodeAndEnabledTrue(normalized)
                .orElseThrow(ServiceNotFoundException::new);
        return new ServiceGuide(service.getCode(), service.getTitleBn(), service.getTitleEn(),
                service.getDescriptionBn(), service.getDescriptionEn(), service.getChecklistBn(),
                service.getChecklistEn());
    }

    @Transactional(readOnly = true)
    public List<HelplineView> listHelplines() {
        return helplineRepository.findByEnabledTrueOrderByDisplayOrderAsc().stream()
                .map(h -> new HelplineView(h.getId(), h.getNameBn(), h.getNameEn(),
                        h.getNumber(), h.getDescriptionBn(), h.getDescriptionEn(), h.getCategory(),
                        h.getSourceUrl(), h.getReviewedOn()))
                .toList();
    }
}
