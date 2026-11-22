export interface Patient {
  id: number;
  lastName: string;
  firstName: string;
}

export interface ImportResult {
  imported: number;
  skipped: number;
}