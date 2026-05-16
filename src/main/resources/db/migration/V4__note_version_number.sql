ALTER TABLE note_versions
ADD COLUMN version_number INT;

UPDATE note_versions nv
SET version_number = sub.rn
FROM (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY note_id
               ORDER BY edited_at ASC
           ) AS rn
    FROM note_versions
) sub
WHERE nv.id = sub.id;

ALTER TABLE note_versions
ALTER COLUMN version_number SET NOT NULL;