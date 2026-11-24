package com.docyra.backend.document;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByStatusInOrderByCreatedAtAsc(Collection<DocumentStatus> statuses);

    @Override
    @EntityGraph(attributePaths = {"pages", "patient"})
    Optional<Document> findById(Long id);
}
