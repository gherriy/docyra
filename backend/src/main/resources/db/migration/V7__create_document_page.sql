CREATE TABLE document_page (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES document(id) ON DELETE CASCADE,
    page_number INT NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    CONSTRAINT uk_document_page UNIQUE (document_id, page_number)
);

-- Les courriers déjà envoyés deviennent des courriers d'une page
INSERT INTO document_page (document_id, page_number, stored_filename, content_type)
SELECT id, 1, stored_filename, content_type FROM document;

ALTER TABLE document DROP COLUMN stored_filename;
ALTER TABLE document DROP COLUMN content_type;