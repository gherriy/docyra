import { Routes } from '@angular/router';
import { Shell } from './layout/shell/shell';
import { PatientImportPage } from './patients/patient-import-page/patient-import-page';


export const routes: Routes = [
    {
    path: '',
    component: Shell,
    children: [
        { path: '', redirectTo: 'patients', pathMatch: 'full' },
      { path: 'patients', component: PatientImportPage, title: 'Docyra - Patients' },
    ],
  },
];
