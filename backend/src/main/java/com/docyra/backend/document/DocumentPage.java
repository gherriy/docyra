package com.docyra.backend.document;

import jakarta.persistence.*;

@Entity
@Table(name = "document_page")
public class DocumentPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    protected DocumentPage() {
    }

    DocumentPage(Document document, int pageNumber, String storedFilename, String contentType) {
        this.document = document;
        this.pageNumber = pageNumber;
        this.storedFilename = storedFilename;
        this.contentType = contentType;
    }

    public Long getId() { return id; }
    public int getPageNumber() { return pageNumber; }
    public String getStoredFilename() { return storedFilename; }
    public String getContentType() { return contentType; }
}