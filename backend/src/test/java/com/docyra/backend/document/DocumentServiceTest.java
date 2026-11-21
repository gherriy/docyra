package com.docyra.backend.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class DocumentServiceTest {

    private final DocumentRepository repository = mock(DocumentRepository.class);
    private final ImageStorageService storage = mock(ImageStorageService.class);
    private final DocumentService service = new DocumentService(repository, storage);

    @Test
    void storesJpgImage() throws Exception {
        var file = new MockMultipartFile("file", "courrier.jpg", "image/jpeg", new byte[] {1, 2, 3});
        when(storage.store(any(), eq("jpg"))).thenReturn("abc.jpg");
        when(repository.save(any(Document.class))).thenAnswer(call -> call.getArgument(0));

        Document document = service.upload(file);

        assertThat(document.getStoredFilename()).isEqualTo("abc.jpg");
        assertThat(document.getOriginalFilename()).isEqualTo("courrier.jpg");
    }

    @Test
    void rejectsNonImageFile() {
        var file = new MockMultipartFile("file", "notes.txt", "text/plain", new byte[] {1});

        assertThatThrownBy(() -> service.upload(file))
                .isInstanceOf(InvalidImageException.class);
        verifyNoInteractions(storage, repository);
    }
}