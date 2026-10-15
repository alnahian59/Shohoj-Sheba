CREATE TABLE service_definitions (
    code VARCHAR(64) PRIMARY KEY,
    title_bn VARCHAR(200) NOT NULL,
    title_en VARCHAR(200) NOT NULL,
    description_bn TEXT NOT NULL,
    checklist_bn TEXT NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TABLE assistance_requests (
    reference VARCHAR(36) PRIMARY KEY,
    service_code VARCHAR(64) NOT NULL REFERENCES service_definitions(code),
    question VARCHAR(500) NOT NULL,
    state VARCHAR(32) NOT NULL,
    tracking_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_assistance_requests_service ON assistance_requests(service_code);

-- Demonstration copy only. It contains no official rules, fees or required documents.
INSERT INTO service_definitions (code, title_bn, title_en, description_bn, checklist_bn, enabled) VALUES
('passport', 'পাসপোর্ট সহায়তা', 'Passport guidance',
 'এটি পাসপোর্ট সেবার একটি নমুনা পথ। আপনার প্রয়োজন লিখে একটি ব্যক্তিগত খসড়া তৈরি করতে পারেন। এই পৃষ্ঠার লেখা সরকারি নির্দেশনা নয়; আবেদন করার আগে সংশ্লিষ্ট সরকারি ওয়েবসাইটে বর্তমান নিয়ম যাচাই করুন।',
 '১. আপনার প্রয়োজনটি নিজের ভাষায় লিখুন। ২. খসড়ার রেফারেন্স ও গোপন ট্র্যাকিং কোড সংরক্ষণ করুন। ৩. সরকারি নিয়ম, ফি ও প্রয়োজনীয় কাগজপত্র দায়িত্বপ্রাপ্ত কর্তৃপক্ষের উৎস থেকে যাচাই করুন।', TRUE),
('birth-registration', 'জন্ম নিবন্ধন সহায়তা', 'Birth registration guidance',
 'এটি জন্ম নিবন্ধন বিষয়ে নমুনা সহায়তা। আপনার প্রশ্ন লিখে রাখতে পারেন এবং পরে ফিরে আসতে পারেন। এটি সরকারি আবেদন বা যাচাইকৃত প্রয়োজনীয় কাগজপত্রের তালিকা নয়।',
 '১. কোন বিষয়ে সাহায্য প্রয়োজন তা লিখুন। ২. খসড়ার গোপন কোড সংরক্ষণ করুন। ৩. বর্তমান নিয়ম ও আবেদনপথ দায়িত্বপ্রাপ্ত কর্তৃপক্ষের উৎস থেকে যাচাই করুন।', TRUE),
('land-service', 'জমি ও ভূমি সেবা', 'Land and property guidance',
 'এটি ভূমি সেবা নিয়ে নমুনা নির্দেশনা। এখানে জমির মালিকানা, আইনি অবস্থা বা সরকারি রেকর্ড যাচাই করা হয় না। আপনার প্রশ্ন লিখে স্থানীয় খসড়া রাখতে পারেন।',
 '১. প্রয়োজন বা সমস্যাটি সংক্ষেপে লিখুন। ২. খসড়ার তথ্য ও গোপন কোড সংরক্ষণ করুন। ৩. সংশ্লিষ্ট কর্তৃপক্ষের বর্তমান নিয়ম ও রেকর্ড নিজে যাচাই করুন।', TRUE);
