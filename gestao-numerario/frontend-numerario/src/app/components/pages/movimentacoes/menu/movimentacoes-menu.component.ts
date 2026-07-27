import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ArrowLeftRight, ChevronRight, PlusCircle, LucideAngularModule } from 'lucide-angular';
import { PageBackComponent } from '../../../shared/page-back/page-back.component';

@Component({
  selector: 'app-movimentacoes-menu',
  standalone: true,
  imports: [RouterLink, LucideAngularModule, PageBackComponent],
  templateUrl: './movimentacoes-menu.component.html',
  styleUrl: '../../menu/menu.component.css'
})
export class MovimentacoesMenuComponent {
  readonly ArrowLeftRight = ArrowLeftRight;
  readonly PlusCircle = PlusCircle;
  readonly ChevronRight = ChevronRight;
}
