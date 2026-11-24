import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LucideInbox, LucideSmartphone, LucideUsers } from '@lucide/angular';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LucideInbox, LucideUsers,LucideSmartphone],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',

})
export class Shell {
    readonly auth = inject(AuthService);

}