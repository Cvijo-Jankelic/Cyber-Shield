-- Migration for PR #1 (Phase 3): email-based registration and pcap persistence.
-- Apply with: psql -d cyber_db -f sql/2026-09-23_add_email_and_pcap_data.sql

-- users.email: read/written by UserRepo, uniqueness enforced by RegisterService.
-- Nullable so existing rows stay valid; the unique index still allows multiple NULLs.
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255);
CREATE UNIQUE INDEX IF NOT EXISTS users_email_unique ON users (email);

-- incidents.pcap_data: raw captured packets stored by IncidentRepo/PcapStorageService.
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS pcap_data BYTEA;
