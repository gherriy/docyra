package com.docyra.backend.document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
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

    public UploadResult upload(MultipartFile file) throws IOException {
        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new InvalidImageException("Seules les images JPG et PNG sont acceptées");
        }

        String storedFilename = storageService.store(file.getInputStream(), extension);
        Document document = new Document(file.getOriginalFilename(), storedFilename, file.getContentType());
        String text = ocrService.extractText(storageService.resolve(storedFilename));

        document.setExtractedText(text);
        List<Patient> candidates = patientMatcher.findInText(patientRepository.findAll(), text);
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

        byte[] page = pageDetector.extractPage(storageService.resolve(document.getStoredFilename()));
        byte[] pdf = pdfService.imageToPdf(page);
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

    public byte[] getPageImage(Long documentId) {
        Document document = findDocument(documentId);
        return pageDetector.extractPage(storageService.resolve(document.getStoredFilename()));
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
                byte[] page = pageDetector.extractPage(storageService.resolve(document.getStoredFilename()));
                zip.putNextEntry(new ZipEntry(names.next(DocumentFileName.of(document.getPatient()))));
                zip.write(pdfService.imageToPdf(page));
                zip.closeEntry();
                document.markDownloaded();
            }
        }
        documentRepository.saveAll(documents);
        return out.toByteArray();
    }
}