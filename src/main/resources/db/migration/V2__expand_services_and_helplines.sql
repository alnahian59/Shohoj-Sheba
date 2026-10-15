-- V2: Expand the service catalogue with additional citizen services
-- and add a helplines table for emergency and public contacts.

INSERT INTO service_definitions (code, title_bn, title_en, description_bn, checklist_bn, enabled) VALUES
('death-registration', 'মৃত্যু নিবন্ধন সহায়তা', 'Death registration guidance',
 'এটি মৃত্যু নিবন্ধন সেবার নমুনা নির্দেশনা। মৃত্যু নিবন্ধন সম্পর্কে আপনার প্রশ্ন লিখে রাখতে পারেন। এটি সরকারি আবেদন নয়।',
 '১. মৃত ব্যক্তির নাম ও মৃত্যুর তারিখ নোট করুন। ২. প্রয়োজনীয় কাগজপত্রের তালিকা যাচাই করুন। ৩. সংশ্লিষ্ট ইউনিয়ন পরিষদ বা পৌরসভায় যোগাযোগ করুন।', TRUE),
('nid', 'জাতীয় পরিচয়পত্র সহায়তা', 'National ID card guidance',
 'এটি জাতীয় পরিচয়পত্র (NID) সম্পর্কে নমুনা নির্দেশনা। নতুন NID, সংশোধন বা প্রতিস্থাপনের বিষয়ে প্রশ্ন লিখে রাখতে পারেন।',
 '১. আপনার প্রয়োজনটি নির্দিষ্ট করুন (নতুন/সংশোধন/প্রতিস্থাপন)। ২. প্রয়োজনীয় কাগজপত্র জানুন। ৩. নির্বাচন কমিশনের সরকারি ওয়েবসাইটে বর্তমান নিয়ম যাচাই করুন।', TRUE),
('visa', 'ভিসা সহায়তা', 'Visa guidance',
 'এটি ভিসা আবেদন ও প্রস্তুতি সম্পর্কে নমুনা নির্দেশনা। গন্তব্য দেশ অনুযায়ী প্রয়োজনীয় তথ্য ভিন্ন হতে পারে।',
 '১. গন্তব্য দেশ ও ভিসার ধরন নির্ধারণ করুন। ২. প্রয়োজনীয় কাগজপত্রের তালিকা সংগ্রহ করুন। ৩. সংশ্লিষ্ট দূতাবাস বা কনস্যুলেটের ওয়েবসাইটে বর্তমান নিয়ম যাচাই করুন।', TRUE),
('driving-license', 'ড্রাইভিং লাইসেন্স সহায়তা', 'Driving license guidance',
 'এটি ড্রাইভিং লাইসেন্স সম্পর্কে নমুনা সহায়তা। নতুন লাইসেন্স, নবায়ন বা আন্তর্জাতিক লাইসেন্সের বিষয়ে প্রশ্ন রাখতে পারেন।',
 '১. লাইসেন্সের ধরন নির্ধারণ করুন (নতুন/নবায়ন/আন্তর্জাতিক)। ২. প্রয়োজনীয় কাগজপত্র প্রস্তুত করুন। ৩. বিআরটিএ-র সরকারি ওয়েবসাইটে বর্তমান নিয়ম ও ফি যাচাই করুন।', TRUE),
('municipal-service', 'পৌরসেবা', 'Municipal services guidance',
 'এটি পৌরসভা ও সিটি কর্পোরেশন সেবা সম্পর্কে নমুনা নির্দেশনা। ট্রেড লাইসেন্স, হোল্ডিং ট্যাক্স, নাগরিক সনদ ইত্যাদি বিষয়ে প্রশ্ন রাখতে পারেন।',
 '১. কোন সেবাটি প্রয়োজন তা নির্দিষ্ট করুন। ২. স্থানীয় পৌরসভা বা সিটি কর্পোরেশনের কার্যালয়ের ঠিকানা জানুন। ৩. প্রয়োজনীয় কাগজপত্র ও ফি সরাসরি কার্যালয়ে যাচাই করুন।', TRUE);

-- Helplines table for emergency and public service contacts
CREATE TABLE helplines (
    id SERIAL PRIMARY KEY,
    name_bn VARCHAR(200) NOT NULL,
    name_en VARCHAR(200) NOT NULL,
    number VARCHAR(30) NOT NULL,
    description_bn TEXT NOT NULL,
    category VARCHAR(64) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO helplines (name_bn, name_en, number, description_bn, category, display_order) VALUES
('জাতীয় জরুরি সেবা', 'National Emergency Service', '999', 'পুলিশ, ফায়ার সার্ভিস ও অ্যাম্বুলেন্সের জন্য জরুরি কল।', 'emergency', 1),
('স্বাস্থ্য বাতায়ন', 'Health Helpline', '16263', 'স্বাস্থ্য পরামর্শ ও তথ্যের জন্য সরকারি হেল্পলাইন।', 'health', 2),
('জাতীয় তথ্য বাতায়ন', 'National Information Helpline', '333', 'সরকারি সেবা ও তথ্য সম্পর্কে সাধারণ জিজ্ঞাসার জন্য।', 'information', 3),
('দুর্নীতি দমন কমিশন', 'Anti-Corruption Commission', '106', 'দুর্নীতি সংক্রান্ত অভিযোগ জানাতে।', 'complaint', 4),
('মহিলা ও শিশু নির্যাতন প্রতিরোধ', 'Women & Child Abuse Prevention', '10921', 'নারী ও শিশু নির্যাতনের বিরুদ্ধে সাহায্য ও অভিযোগ।', 'protection', 5),
('ফায়ার সার্ভিস ও সিভিল ডিফেন্স', 'Fire Service & Civil Defence', '16163', 'অগ্নিকাণ্ড ও দুর্যোগকালীন জরুরি সাহায্যের জন্য।', 'emergency', 6),
('পুলিশ হেডকোয়ার্টার্স', 'Police Headquarters', '01৭৬৬ ৬৭ ৮৯৯৯', 'পুলিশ সংক্রান্ত তথ্য ও অভিযোগের জন্য।', 'law', 7),
('ইমিগ্রেশন হেল্পলাইন', 'Immigration Helpline', '16507', 'পাসপোর্ট ও ইমিগ্রেশন সংক্রান্ত তথ্যের জন্য।', 'immigration', 8);
