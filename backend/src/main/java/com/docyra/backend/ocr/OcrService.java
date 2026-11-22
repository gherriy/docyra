package com.docyra.backend.ocr;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class OcrService {

    private final String datapath;
    private final String language;

    public OcrService(@Value("${docyra.ocr.datapath}") String datapath,
                      @Value("${docyra.ocr.language:fra}") String language,
                      @Value("${docyra.ocr.lib-path:}") String libPath) {
        this.datapath = datapath;
        this.language = language;
        if (!libPath.isBlank()) {
            System.setProperty("jna.library.path", libPath);
        }
    }

    public String extractText(Path image) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(datapath);
        tesseract.setLanguage(language);
        try {
            return tesseract.doOCR(image.toFile());
        } catch (TesseractException e) {
            throw new OcrException("Impossible de lire le texte de l'image", e);
        }
    }
}