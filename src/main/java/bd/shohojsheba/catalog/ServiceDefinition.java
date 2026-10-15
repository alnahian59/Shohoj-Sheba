package bd.shohojsheba.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_definitions")
public class ServiceDefinition {
    @Id
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "title_bn", nullable = false, length = 200)
    private String titleBn;

    @Column(name = "title_en", nullable = false, length = 200)
    private String titleEn;

    @Column(name = "description_bn", nullable = false, columnDefinition = "text")
    private String descriptionBn;

    @Column(name = "description_en", columnDefinition = "text")
    private String descriptionEn;

    @Column(name = "checklist_bn", nullable = false, columnDefinition = "text")
    private String checklistBn;

    @Column(name = "checklist_en", columnDefinition = "text")
    private String checklistEn;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    protected ServiceDefinition() {
    }

    public String getCode() {
        return code;
    }

    public String getTitleBn() {
        return titleBn;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public String getDescriptionBn() {
        return descriptionBn;
    }

    public String getDescriptionEn() {
        return descriptionEn == null || descriptionEn.isBlank() ? descriptionBn : descriptionEn;
    }

    public String getChecklistBn() {
        return checklistBn;
    }

    public String getChecklistEn() {
        return checklistEn == null || checklistEn.isBlank() ? checklistBn : checklistEn;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
