package com.docyra.backend.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.Test;
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
    private final PdfService pdf = mock(PdfService.class);
    private final PageDetector pageDetector = mock(PageDetector.class);
    private final DocumentService service = new DocumentService(repository, storage, ocr,patients, new PatientMatcher(), pdf, pageDetector);

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
        @Test
    void listsAllDocumentsExceptDownloadedByDefault() {
        service.list(null);

        verify(repository).findByStatusInOrderByCreatedAtAsc(
                EnumSet.of(DocumentStatus.PROCESSING, DocumentStatus.MATCHED,
                        DocumentStatus.TO_REVIEW, DocumentStatus.VALIDATED));
    }

    @Test
    void listsOnlyRequestedStatuses() {
        service.list(List.of(DocumentStatus.TO_REVIEW));

        verify(repository).findByStatusInOrderByCreatedAtAsc(List.of(DocumentStatus.TO_REVIEW));
    }
        @Test
    void detailRecomputesCandidatesFromText() {
        Document document = new Document("courrier.jpg", "abc.jpg", "image/jpeg");
        document.setExtractedText("Patient : Hugo LEFEBVRE, frère de Noé LEFEBVRE");
        Patient hugo = new Patient("LEFEBVRE", "Hugo");
        Patient noe = new Patient("LEFEBVRE", "Noé");
        when(repository.findById(1L)).thenReturn(Optional.of(document));
        when(patients.findAll()).thenReturn(List.of(hugo, noe, new Patient("MARTIN", "Jean")));

        UploadResult result = service.getDetail(1L);

        assertThat(result.candidates()).containsExactly(hugo, noe);
    }
        @Test
    void cannotValidateWithoutPatient() {
        Document document = new Document("courrier.jpg", "abc.jpg", "image/jpeg");
        when(repository.findById(1L)).thenReturn(Optional.of(document));

        assertThatThrownBy(() -> service.validate(1L)).isInstanceOf(MissingPatientException.class);
    }

    @Test
    void zipContainsNumberedPdfsAndMarksDocumentsDownloaded() throws Exception {
        Patient marie = new Patient("DUPONT", "Marie");
        Document first = new Document("a.jpg", "a.jpg", "image/jpeg");
        Document second = new Document("b.jpg", "b.jpg", "image/jpeg");
        first.assignPatient(marie);
        second.assignPatient(marie);
        first.validate();
        second.validate();
        when(repository.findByStatusInOrderByCreatedAtAsc(List.of(DocumentStatus.VALIDATED)))
                .thenReturn(List.of(first, second));
        when(pageDetector.extractPage(any())).thenReturn(new byte[] {1});
        when(pdf.imageToPdf(any())).thenReturn(new byte[] {2});

        byte[] zipBytes = service.buildValidatedZip();

        List<String> names = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                names.add(entry.getName());
            }
        }
        assertThat(names).containsExactly("DUPONT_Marie.pdf", "DUPONT_Marie_2.pdf");
        assertThat(first.getStatus()).isEqualTo(DocumentStatus.DOWNLOADED);
        assertThat(second.getStatus()).isEqualTo(DocumentStatus.DOWNLOADED);
    }
}