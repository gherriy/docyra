import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LucideInbox, LucideUsers } from '@lucide/angular';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LucideInbox, LucideUsers],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',

})
export class Shell {}