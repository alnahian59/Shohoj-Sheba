-- English copy for the existing sample catalog. These are examples, not official instructions.
ALTER TABLE service_definitions ADD COLUMN description_en TEXT;
ALTER TABLE service_definitions ADD COLUMN checklist_en TEXT;
ALTER TABLE helplines ADD COLUMN description_en TEXT;

UPDATE service_definitions SET
    description_en = 'A sample path for passport services. You can write down your question in a private draft. This page is not official guidance; check the responsible government website for current rules before applying.',
    checklist_en = '1. Describe what you need in your own words. 2. Keep your draft reference and private tracking code. 3. Check current rules, fees, and required documents with the responsible authority.'
WHERE code = 'passport';

UPDATE service_definitions SET
    description_en = 'Sample guidance for birth registration. You can save your question and return to it later. This is not an official application or a verified list of required documents.',
    checklist_en = '1. Write down the help you need. 2. Keep the private code for your draft. 3. Check current rules and the application route with the responsible authority.'
WHERE code = 'birth-registration';

UPDATE service_definitions SET
    description_en = 'Sample guidance for land services. This site does not verify ownership, legal status, or official land records. You can keep a local draft of your question.',
    checklist_en = '1. Briefly describe your need or problem. 2. Keep your draft details and private code. 3. Check current rules and records with the responsible authority.'
WHERE code = 'land-service';

UPDATE service_definitions SET
    description_en = 'Sample guidance for death registration. You can write down questions about the process. This is not an official application.',
    checklist_en = '1. Note the deceased person''s name and date of death. 2. Check the current document list. 3. Contact the relevant union council or municipality.'
WHERE code = 'death-registration';

UPDATE service_definitions SET
    description_en = 'Sample guidance for a National ID (NID). You can note a question about a new card, correction, or replacement.',
    checklist_en = '1. Identify whether you need a new card, correction, or replacement. 2. Check which documents are needed. 3. Verify current rules on the Election Commission''s official website.'
WHERE code = 'nid';

UPDATE service_definitions SET
    description_en = 'Sample guidance for visa preparation. Requirements can differ by destination country and visa type.',
    checklist_en = '1. Choose the destination country and visa type. 2. Gather the current document list. 3. Check the relevant embassy or consulate website for current rules.'
WHERE code = 'visa';

UPDATE service_definitions SET
    description_en = 'Sample guidance for driving licenses. You can note a question about a new license, renewal, or an international driving permit.',
    checklist_en = '1. Identify the license type you need. 2. Prepare the relevant documents. 3. Verify current rules and fees on the official BRTA website.'
WHERE code = 'driving-license';

UPDATE service_definitions SET
    description_en = 'Sample guidance for municipal and city corporation services, such as trade licenses, holding tax, and citizen certificates.',
    checklist_en = '1. Identify the service you need. 2. Find your local municipality or city corporation office. 3. Verify documents and fees directly with that office.'
WHERE code = 'municipal-service';

UPDATE helplines SET description_en = 'Emergency calls for police, fire service, and ambulance support.'
WHERE name_en = 'National Emergency Service';
UPDATE helplines SET description_en = 'Government phone line for health advice and information.'
WHERE name_en = 'Health Helpline';
UPDATE helplines SET description_en = 'General questions about government services and information.'
WHERE name_en = 'National Information Helpline';
UPDATE helplines SET description_en = 'Report concerns about corruption.'
WHERE name_en = 'Anti-Corruption Commission';
UPDATE helplines SET description_en = 'Help and complaints concerning abuse of women and children.'
WHERE name_en = 'Women & Child Abuse Prevention';
UPDATE helplines SET description_en = 'Emergency help during fires and disasters.'
WHERE name_en = 'Fire Service & Civil Defence';
UPDATE helplines SET description_en = 'Information and complaints related to police services.'
WHERE name_en = 'Police Headquarters';
UPDATE helplines SET description_en = 'Information about passports and immigration.'
WHERE name_en = 'Immigration Helpline';
