import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Building2, ChevronRight, PlusCircle, LucideAngularModule } from 'lucide-angular';
import { PageBackComponent } from '../../../shared/page-back/page-back.component';

@Component({
  selector: 'app-agencias-menu',
  standalone: true,
  imports: [RouterLink, LucideAngularModule, PageBackComponent],
  templateUrl: './agencias-menu.component.html',
  styleUrl: '../../menu/menu.component.css'
})
export class AgenciasMenuComponent {
  readonly Building2 = Building2;
  readonly PlusCircle = PlusCircle;
  readonly ChevronRight = ChevronRight;
}
