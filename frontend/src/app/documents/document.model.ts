import { Patient } from '../patients/patient.model';

export type DocumentStatus = 'PROCESSING' | 'MATCHED' | 'TO_REVIEW' | 'VALIDATED' | 'DOWNLOADED';

export interface DocumentSummary {
  id: number;
  status: DocumentStatus;
  patient: Patient | null;
  fileName: string | null;
  createdAt: string;
}