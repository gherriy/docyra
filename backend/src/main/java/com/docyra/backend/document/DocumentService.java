package com.docyra.backend.document;

import java.io.IOException;
import java.util.Map;
import java.util.List;


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
}