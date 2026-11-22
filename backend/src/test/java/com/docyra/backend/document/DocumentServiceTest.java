package com.docyra.backend.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

import com.docyra.backend.ocr.OcrService;
import com.docyra.backend.patient.Patient;
import com.docyra.backend.patient.PatientMatcher;
import com.docyra.backend.patient.PatientRepository;
import com.docyra.backend.pdf.PdfService;
import com.docyra.backend.scan.PageDetector;

class DocumentServiceTest {

    private final DocumentRepository repository = mock(DocumentRepository.class);
    private final ImageStorageService storage = mock(ImageStorageService.class);
    private final OcrService ocr = mock(OcrService.class);
    private final PatientRepository patients = mock(PatientRepository.class);
    private final PdfService pdfService = mock(PdfService.class);
    private final PageDetector pageDetector = mock(PageDetector.class);
    private final DocumentService service = new DocumentService(repository, storage, ocr,patients, new PatientMatcher(), pdfService, pageDetector);

    @Test
    void storesJpgImage() throws Exception {
        var file = new MockMultipartFile("file", "courrier.jpg", "image/jpeg", new byte[] { 1, 2, 3 });
        when(storage.store(any(), eq("jpg"))).thenReturn("abc.jpg");
        when(repository.save(any(Document.class))).thenAnswer(call -> call.getArgument(0));
        when(ocr.extractText(any())).thenReturn("Madame DUPONT Marie");
        Document document = service.upload(file).document();

        assertThat(document.getStoredFilename()).isEqualTo("abc.jpg");
        assertThat(document.getOriginalFilename()).isEqualTo("courrier.jpg");
        assertThat(document.getExtractedText()).isEqualTo("Madame DUPONT Marie");
    }

    @Test
    void rejectsNonImageFile() {
        var file = new MockMultipartFile("file", "notes.txt", "text/plain", new byte[] { 1 });

        assertThatThrownBy(() -> service.upload(file))
                .isInstanceOf(InvalidImageException.class);
        verifyNoInteractions(storage, repository);
    }

        @Test
    void assignsPatientWhenOnlyOneIsFound() throws Exception {
        var file = new MockMultipartFile("file", "courrier.jpg", "image/jpeg", new byte[] {1});
        Patient dupont = new Patient("DUPONT", "Marie");
        when(storage.store(any(), eq("jpg"))).thenReturn("abc.jpg");
        when(ocr.extractText(any())).thenReturn("Compte rendu de Mme Marie DUPONT");
        when(patients.findAll()).thenReturn(List.of(dupont, new Patient("MARTIN", "Jean")));
        when(repository.save(any(Document.class))).thenAnswer(call -> call.getArgument(0));

        Document document = service.upload(file).document();

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.MATCHED);
        assertThat(document.getPatient()).isEqualTo(dupont);
    }
}