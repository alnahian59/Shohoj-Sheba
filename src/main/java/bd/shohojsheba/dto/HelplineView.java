package bd.shohojsheba.dto;

import java.time.LocalDate;

public record HelplineView(
        int id,
        String nameBn,
        String nameEn,
        String number,
        String descriptionBn,
        String descriptionEn,
        String category,
        String sourceUrl,
        LocalDate reviewedOn
) {
}
