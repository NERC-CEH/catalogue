-- Registers the files under fixtures/upload/{eidchub,supporting-documents} for the records the
-- local data preview uses, as Hubbub's own hashing job would leave them: VALID, with a SHA-256.
-- Runs after hubbub_backup.sql (the entrypoint takes files in name order) on a fresh hubbub-db.
-- Regenerate the sha256 and bytes values if a fixture file changes, or Hubbub reports stale ones.
-- c88921ba-... is left out: README's Hubbub section imports fixtures/upload/file.csv for it.
INSERT INTO public.file (dataset_id, datastore, path, status, sha256, last_modified_time, last_validated_time, bytes, format) VALUES
    ('2d023ce9-6dbe-4b4f-a0cd-34768e1455ae', 'eidchub', 'Woodlands_Site_Summary.csv', 'VALID', 'b1071ca5ef2c67b5e6ea2498a81de37dcc9531fd89a00e4419c31195ef362199', '2025-12-01 00:00:00', '2025-12-01 00:00:00', 249, 'csv'),
    ('2d023ce9-6dbe-4b4f-a0cd-34768e1455ae', 'eidchub', 'Woodlands_Survey_Data_1971_2001.csv', 'VALID', 'd26ee02fbc079e9a404c6be594af4394b23a710987a39b0ddf72ff6ca2ce3e63', '2025-12-01 00:00:00', '2025-12-01 00:00:00', 12234, 'csv'),
    ('2d023ce9-6dbe-4b4f-a0cd-34768e1455ae', 'supporting-documents', 'Woodland_Survey_2003_Handbook.pdf', 'VALID', '734cc833240961dde4f04cc4cc9cfff601473b236b5640ef8370c50b0846bddc', '2025-12-01 00:00:00', '2025-12-01 00:00:00', 463100, 'pdf'),
    ('2d023ce9-6dbe-4b4f-a0cd-34768e1455ae', 'supporting-documents', 'Woodlands_Data_Dictionary.rtf', 'VALID', '0c3a1701c9060b499a000a47c4995ebdcbc80612d1f97a57823d57183387be5a', '2025-12-01 00:00:00', '2025-12-01 00:00:00', 1012, 'rtf'),
    ('993c5778-e139-4171-a57f-7a0f396be4b8', 'eidchub', 'data/bees_data.csv', 'VALID', '9541d6636ac582f86447c5f613e345b1945041ff219c9e4b25a7682a53d72364', '2025-12-01 00:00:00', '2025-12-01 00:00:00', 881, 'csv'),
    ('993c5778-e139-4171-a57f-7a0f396be4b8', 'supporting-documents', 'supporting-documents/sample_doc.docx', 'VALID', 'aa12ded730e5b7e507edd63eb7353079deeb9da814b8bbb0b28e01bb87d38463', '2025-12-01 00:00:00', '2025-12-01 00:00:00', 15158, 'docx');
