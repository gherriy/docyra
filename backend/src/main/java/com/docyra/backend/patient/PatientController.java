package com.docyra.backend.patient;

import java.io.IOException;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientImportService importService;

    public PatientController(PatientImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/import")
    public ImportResult importPatients(@RequestParam("file") MultipartFile file) throws IOException {
        return importService.importCsv(file.getInputStream());
    }
}