import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  ChevronRight,
  Landmark,
  Settings2,
  LucideAngularModule
} from 'lucide-angular';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [RouterLink, LucideAngularModule],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent {
  readonly Landmark = Landmark;
  readonly Settings2 = Settings2;
  readonly ChevronRight = ChevronRight;
}
