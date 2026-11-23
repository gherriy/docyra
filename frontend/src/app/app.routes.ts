import { Routes } from '@angular/router';
import { Shell } from './layout/shell/shell';
import { PatientImportPage } from './patients/patient-import-page/patient-import-page';
import { ValidationQueuePage } from './documents/validation-queue-page/validation-queue-page';
import { DocumentReviewPage } from './documents/document-review-page/document-review-page';


export const routes: Routes = [
    {
    path: '',
    component: Shell,
    children: [
        { path: '', redirectTo: 'patients', pathMatch: 'full' },
        { path: 'patients', component: PatientImportPage, title: 'Docyra - Patients' },
        { path: 'a-valider', component: ValidationQueuePage, title: 'Docyra - À valider' },
        { path: 'a-valider/:id', component: DocumentReviewPage, title: 'Docyra - Vérifier un courrier' },

    ],
  },
];
