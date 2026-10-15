package bd.shohojsheba.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "helplines")
public class Helpline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name_bn", nullable = false, length = 200)
    private String nameBn;

    @Column(name = "name_en", nullable = false, length = 200)
    private String nameEn;

    @Column(name = "number", nullable = false, length = 30)
    private String number;

    @Column(name = "description_bn", nullable = false, columnDefinition = "text")
    private String descriptionBn;

    @Column(name = "description_en", columnDefinition = "text")
    private String descriptionEn;

    @Column(name = "category", nullable = false, length = 64)
    private String category;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "reviewed_on")
    private LocalDate reviewedOn;

    protected Helpline() {
    }

    public Integer getId() {
        return id;
    }

    public String getNameBn() {
        return nameBn;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNumber() {
        return number;
    }

    public String getDescriptionBn() {
        return descriptionBn;
    }

    public String getDescriptionEn() {
        return descriptionEn == null || descriptionEn.isBlank() ? descriptionBn : descriptionEn;
    }

    public String getCategory() {
        return category;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public LocalDate getReviewedOn() {
        return reviewedOn;
    }
}
