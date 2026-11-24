import { Routes } from '@angular/router';
import { Shell } from './layout/shell/shell';
import { PatientImportPage } from './patients/patient-import-page/patient-import-page';
import { ValidationQueuePage } from './documents/validation-queue-page/validation-queue-page';
import { DocumentReviewPage } from './documents/document-review-page/document-review-page';
import { LoginPage } from './auth/login-page/login-page';
import { authGuard } from './auth/auth.guard';
import { PhonePage } from './phone/phone-page/phone-page';
import { ScanPage } from './scan/scan-page/scan-page';


export const routes: Routes = [
  { path: 'connexion', component: LoginPage, title: 'Docyra - Connexion' },
    { path: 'scan', component: ScanPage, title: 'Docyra - Scan' },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],

    children: [
      { path: '', redirectTo: 'patients', pathMatch: 'full' },
      { path: 'patients', component: PatientImportPage, title: 'Docyra - Patients' },
      { path: 'a-valider', component: ValidationQueuePage, title: 'Docyra - À valider' },
      { path: 'a-valider/:id', component: DocumentReviewPage, title: 'Docyra - Vérifier un courrier' },
      { path: 'telephone', component: PhonePage, title: 'Docyra - Téléphone' },

    ],
  },
];
