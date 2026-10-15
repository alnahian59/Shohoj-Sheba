-- Keep public contact numbers tied to an official source and review date.
-- V2 is already applied in deployed databases, so corrections belong here.
ALTER TABLE helplines ADD COLUMN source_url VARCHAR(500);
ALTER TABLE helplines ADD COLUMN reviewed_on DATE;

UPDATE helplines SET source_url = 'https://bangladesh.gov.bd/', reviewed_on = DATE '2026-10-14'
WHERE name_en IN ('National Emergency Service', 'National Information Helpline');

UPDATE helplines SET source_url = 'https://16263.dghs.gov.bd/', reviewed_on = DATE '2026-10-14'
WHERE name_en = 'Health Helpline';

UPDATE helplines SET source_url = 'https://hsd.gov.bd/', reviewed_on = DATE '2026-10-14'
WHERE name_en = 'Anti-Corruption Commission';

UPDATE helplines SET number = '109', source_url = 'https://bangladesh.gov.bd/',
    reviewed_on = DATE '2026-10-14'
WHERE name_en = 'Women & Child Abuse Prevention';

UPDATE helplines SET number = '102', source_url = 'https://bangladesh.gov.bd/',
    reviewed_on = DATE '2026-10-14'
WHERE name_en = 'Fire Service & Civil Defence';

-- The V2 phone number contains mixed Bengali and Latin digits. The other
-- number lacks a current official source. Neither should be displayed.
UPDATE helplines SET enabled = FALSE
WHERE name_en IN ('Police Headquarters', 'Immigration Helpline');

ALTER TABLE helplines ADD CONSTRAINT chk_enabled_helpline_source
    CHECK (enabled = FALSE OR (source_url IS NOT NULL AND reviewed_on IS NOT NULL));
