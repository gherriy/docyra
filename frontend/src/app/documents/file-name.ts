import { Patient } from '../patients/patient.model';

const clean = (value: string) =>
  value
    .normalize('NFD')
    .replace(/\p{M}/gu, '')
    .trim()
    .replace(/\s+/g, '-')
    .replace(/[^A-Za-z0-9-]/g, '');

export function fileNameFor(patient: Patient) {
  return `${clean(patient.lastName).toUpperCase()}_${clean(patient.firstName)}.pdf`;
}