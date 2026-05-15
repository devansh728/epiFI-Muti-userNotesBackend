-- Phase 3: Performance Indexes
-- B-tree index on notes.owner_id for fast filtering by user
CREATE INDEX idx_notes_owner_id ON notes (owner_id);

-- Composite index for paginated listing by owner with created_at ordering
CREATE INDEX idx_notes_owner_created ON notes (owner_id, created_at DESC);

-- Partial index on non-deleted notes to keep working set small
CREATE INDEX idx_notes_not_deleted ON notes (owner_id) WHERE deleted_at IS NULL;

-- B-tree index on note_shares.shared_with_user_id for finding all notes shared with a user
CREATE INDEX idx_note_shares_shared_with_user_id ON note_shares (shared_with_user_id);

-- B-tree index on note_versions.note_id for retrieving version history
CREATE INDEX idx_note_versions_note_id ON note_versions (note_id);

-- Indexes on refresh_tokens for lookups and reuse detection
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_not_revoked ON refresh_tokens (user_id) WHERE revoked_at IS NULL;

-- Index on users email (already has UNIQUE constraint, but explicit for clarity)
-- Comment: UNIQUE constraint on email field automatically creates an index
