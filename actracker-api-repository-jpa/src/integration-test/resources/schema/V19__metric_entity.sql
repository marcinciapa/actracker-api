ALTER TABLE metric ADD COLUMN creator_id VARCHAR(36) NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000';
ALTER TABLE metric ALTER COLUMN creator_id DROP DEFAULT;
ALTER TABLE metric ADD COLUMN deleted BOOLEAN DEFAULT FALSE;