import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ChevronRight, ClipboardList, FilePlus2, LucideAngularModule } from 'lucide-angular';
import { PageBackComponent } from '../../../shared/page-back/page-back.component';

@Component({
  selector: 'app-solicitacoes-menu',
  standalone: true,
  imports: [RouterLink,LucideAngularModule,PageBackComponent],
  templateUrl: './solicitacoes-menu.component.html',
  styleUrl: '../../menu/menu.component.css'
})
export class SolicitacoesMenuComponent {
  readonly ClipboardList=ClipboardList;
  readonly FilePlus2=FilePlus2;
  readonly ChevronRight=ChevronRight;
}
