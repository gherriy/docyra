package com.docyra.backend.document;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
        List<Document> findByStatusInOrderByCreatedAtAsc(Collection<DocumentStatus> statuses);

    
}
