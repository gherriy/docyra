package com.docyra.backend.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

@Service
public class PdfService {

    public byte[] imagesToPdf(List<byte[]> images) {
        try (PDDocument pdf = new PDDocument()) {
            PDRectangle a4 = PDRectangle.A4;
            for (byte[] image : images) {
                PDPage page = new PDPage(a4);
                pdf.addPage(page);

                PDImageXObject picture = PDImageXObject.createFromByteArray(pdf, image, "page");
                float scale = Math.min(a4.getWidth() / picture.getWidth(), a4.getHeight() / picture.getHeight());
                float width = picture.getWidth() * scale;
                float height = picture.getHeight() * scale;
                float x = (a4.getWidth() - width) / 2;
                float y = (a4.getHeight() - height) / 2;

                try (PDPageContentStream content = new PDPageContentStream(pdf, page)) {
                    content.drawImage(picture, x, y, width, height);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdf.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new PdfGenerationException("Impossible de créer le PDF", e);
        }
    }
}