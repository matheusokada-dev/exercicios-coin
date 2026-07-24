import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  ArrowLeftRight,
  BookOpen,
  Building2,
  ChevronRight,
  ClipboardList,
  LayoutDashboard,
  LucideAngularModule
} from 'lucide-angular';
import { PageBackComponent } from '../shared/page-back/page-back.component';

@Component({
  selector: 'app-tesouraria-menu',
  standalone: true,
  imports: [RouterLink, LucideAngularModule, PageBackComponent],
  templateUrl: './tesouraria-menu.component.html',
  styleUrl: '../menu/menu.component.css'
})
export class TesourariaMenuComponent {
  readonly ArrowLeftRight = ArrowLeftRight;
  readonly BookOpen = BookOpen;
  readonly Building2 = Building2;
  readonly ChevronRight = ChevronRight;
  readonly ClipboardList = ClipboardList;
  readonly LayoutDashboard = LayoutDashboard;
}
