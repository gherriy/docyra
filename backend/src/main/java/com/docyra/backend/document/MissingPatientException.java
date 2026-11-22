package com.docyra.backend.document;

public class MissingPatientException extends RuntimeException {

    public MissingPatientException() {
        super("Aucun patient n'est associé à ce document");
    }
    
}
