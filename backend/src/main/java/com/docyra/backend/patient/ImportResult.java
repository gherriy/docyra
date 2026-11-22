package com.docyra.backend.patient;

import java.util.List;

public record ImportResult(int imported, int skipped, List<Integer> invalidLines)  {
    
}
