package com.docyra.backend.patient;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientImportService importService;
    private final PatientRepository patientRepository;

    public PatientController(PatientImportService importService,  PatientRepository patientRepository) {
        this.importService = importService;
        this.patientRepository = patientRepository;

    }

    @PostMapping("/import")
    public ImportResult importPatients(@RequestParam("file") MultipartFile file) throws IOException {
        return importService.importCsv(file.getInputStream());
    }
        @GetMapping
    public List<PatientResponse> list() {
        return patientRepository.findAllByOrderByLastNameAscFirstNameAsc().stream()
                .map(PatientResponse::from)
                .toList();
    }
}