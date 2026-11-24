package com.docyra.backend.document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.StringJoiner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.List;
import java.util.Collection;
import java.util.EnumSet;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.docyra.backend.error.NotFoundException;
import com.docyra.backend.ocr.OcrService;
import com.docyra.backend.patient.Patient;
import com.docyra.backend.patient.PatientMatcher;
import com.docyra.backend.patient.PatientRepository;
import com.docyra.backend.pdf.PdfService;
import com.docyra.backend.scan.PageDetector;

import jakarta.transaction.Transactional;

@Service
public class DocumentService {

    private static final int MAX_PAGES = 20;

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png");

    private final DocumentRepository documentRepository;
    private final ImageStorageService storageService;
    private final OcrService ocrService;
    private final PatientRepository patientRepository;
    private final PatientMatcher patientMatcher;
    private final PdfService pdfService;
    private final PageDetector pageDetector;

    public DocumentService(DocumentRepository documentRepository, ImageStorageService storageService,
            OcrService ocrService, PatientRepository patientRepository,
                           PatientMatcher patientMatcher, PdfService pdfService, PageDetector pageDetector) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.ocrService = ocrService;
        this.patientRepository = patientRepository;
        this.patientMatcher = patientMatcher;
        this.pdfService = pdfService;
        this.pageDetector = pageDetector;
    }

    public UploadResult upload(List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new InvalidImageException("Ajoutez au moins une page");
        }
        if (files.size() > MAX_PAGES) {
            throw new InvalidImageException("Un courrier ne peut pas dépasser " + MAX_PAGES + " pages");
        }
        for (MultipartFile file : files) {
            if (!ALLOWED_TYPES.containsKey(file.getContentType())) {
                throw new InvalidImageException("Seules les images JPG et PNG sont acceptées");
            }
        }

        Document document = new Document(files.get(0).getOriginalFilename());
        StringJoiner text = new StringJoiner("\n\n");
        for (MultipartFile file : files) {
            String storedFilename = storageService.store(file.getInputStream(), ALLOWED_TYPES.get(file.getContentType()));
            document.addPage(storedFilename, file.getContentType());
            text.add(ocrService.extractText(storageService.resolve(storedFilename)));
        }
        document.setExtractedText(text.toString());
        List<Patient> candidates = patientMatcher.findInText(patientRepository.findAll(), document.getExtractedText());
        if (candidates.size() == 1) {
            document.assignPatient(candidates.get(0));
        }
        return new UploadResult(documentRepository.save(document), candidates);
    }

    @Transactional 
    public Document assignPatient(Long documentId, Long patientId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document introuvable"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient introuvable"));
        document.assignPatient(patient);
        return documentRepository.save(document);
    }
        public DocumentPdf getPdf(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document introuvable"));
        if (document.getPatient() == null) {
            throw new MissingPatientException();
        }

        byte[] pdf = pdfService.imagesToPdf(straightenedPages(document));
        return new DocumentPdf(DocumentFileName.of(document.getPatient()), pdf);
    }
        public List<Document> list(Collection<DocumentStatus> statuses) {
        Collection<DocumentStatus> filter = (statuses == null || statuses.isEmpty())
                ? EnumSet.complementOf(EnumSet.of(DocumentStatus.DOWNLOADED))
                : statuses;
        return documentRepository.findByStatusInOrderByCreatedAtAsc(filter);
    }
        public UploadResult getDetail(Long documentId) {
        Document document = findDocument(documentId);
        List<Patient> candidates = patientMatcher.findInText(patientRepository.findAll(), document.getExtractedText());
        return new UploadResult(document, candidates);
    }

    public byte[] getPageImage(Long documentId, int pageNumber) {
        Document document = findDocument(documentId);
        return straighten(document.getPage(pageNumber));
    }

    private List<byte[]> straightenedPages(Document document) {
        return document.getPages().stream().map(this::straighten).toList();
    }

    private byte[] straighten(DocumentPage page) {
        return pageDetector.extractPage(storageService.resolve(page.getStoredFilename()));
    }

    private Document findDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document introuvable"));
    }
        @Transactional
    public Document validate(Long documentId) {
        Document document = findDocument(documentId);
        document.validate();
        return documentRepository.save(document);
    }

    @Transactional
    public byte[] buildValidatedZip() throws IOException {
        List<Document> documents = documentRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(DocumentStatus.VALIDATED));
        if (documents.isEmpty()) {
            throw new NotFoundException("Aucun courrier validé à télécharger");
        }

        UniqueFileNames names = new UniqueFileNames();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Document document : documents) {
                zip.putNextEntry(new ZipEntry(names.next(DocumentFileName.of(document.getPatient()))));
                zip.write(pdfService.imagesToPdf(straightenedPages(document)));
                zip.closeEntry();
                document.markDownloaded();
            }
        }
        documentRepository.saveAll(documents);
        return out.toByteArray();
    }
}