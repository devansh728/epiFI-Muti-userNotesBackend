-- Phase 2: Full-Text Search Configuration
-- Add tsvector column to notes for full-text search
ALTER TABLE notes
ADD COLUMN search_vector TSVECTOR
GENERATED ALWAYS AS (
  setweight(to_tsvector('english', COALESCE(title, '')), 'A') ||
  setweight(to_tsvector('english', COALESCE(content, '')), 'B')
) STORED;

-- Create GIN index on search_vector for fast full-text search
CREATE INDEX idx_notes_search_vector ON notes USING GIN (search_vector);

-- Add comment explaining the search vector
COMMENT ON COLUMN notes.search_vector IS 'Generated full-text search vector. Title (weight A) has higher relevance than content (weight B)';
