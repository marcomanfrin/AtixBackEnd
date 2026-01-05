-- Migration script to make Work fields optional
-- Execute this script on your PostgreSQL database

-- Remove NOT NULL constraints from Work table columns
ALTER TABLE works ALTER COLUMN nas_sub_directory DROP NOT NULL;
ALTER TABLE works ALTER COLUMN electrical_schema_progression DROP NOT NULL;
ALTER TABLE works ALTER COLUMN programming_progression DROP NOT NULL;
ALTER TABLE works ALTER COLUMN completed DROP NOT NULL;
ALTER TABLE works ALTER COLUMN invoiced DROP NOT NULL;
ALTER TABLE works ALTER COLUMN created_at DROP NOT NULL;
ALTER TABLE works ALTER COLUMN atix_client_id DROP NOT NULL;
ALTER TABLE works ALTER COLUMN expected_office_hours DROP NOT NULL;
ALTER TABLE works ALTER COLUMN expected_plant_hours DROP NOT NULL;

-- Verification query - check if constraints were removed
SELECT column_name, is_nullable, data_type
FROM information_schema.columns
WHERE table_name = 'works'
  AND column_name IN (
    'nas_sub_directory',
    'electrical_schema_progression',
    'programming_progression',
    'completed',
    'invoiced',
    'created_at',
    'atix_client_id',
    'expected_office_hours',
    'expected_plant_hours'
  )
ORDER BY column_name;
