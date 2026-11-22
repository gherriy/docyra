package com.docyra.backend.patient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientImportService {

    private final PatientRepository patientRepository;

    public PatientImportService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public ImportResult importCsv(InputStream csv) {
        int imported = 0;
        int skipped = 0;
        List<Integer> invalidLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csv, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // ligne d'en-tête : nom;prenom
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(";");
if (columns.length < 2 || columns[0].isBlank() || columns[1].isBlank()) {                    
                    invalidLines.add(lineNumber);
                    continue;
                }

                String lastName = columns[0].trim().toUpperCase();
                String firstName = columns[1].trim();

                if (patientRepository.existsByLastNameAndFirstName(lastName, firstName)) {
                    skipped++;
                } else {
                    patientRepository.save(new Patient(lastName, firstName));
                    imported++;
                }
            }
        } catch (IOException e) {
            throw new InvalidCsvException("Impossible de lire le fichier");
        }

        return new ImportResult(imported, skipped, invalidLines);
    }
}