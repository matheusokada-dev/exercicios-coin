import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AlertComponent } from '../../../shared/alert/alert.component';
import { BreadcrumbItem, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { Agencia } from '../../../../models/api.models';
import { AgenciasService } from '../../../../services/agencias.service';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';
import { CurrencyInputDirective } from '../../../../directives/currency-input.directive';

@Component({
  selector:'app-nova-movimentacao',standalone:true,
  imports:[FormsModule,AlertComponent,PageHeaderComponent,ConfirmationDialogComponent,CurrencyInputDirective],
  templateUrl: './nova-movimentacao.component.html'
})
export class NovaMovimentacaoComponent implements OnInit {
  @ViewChild('confirmacao') confirmacao!:ConfirmationDialogComponent;
  readonly breadcrumbs:BreadcrumbItem[]=[
    {label:'COIN Home',link:'/menu'},{label:'Tesouraria',link:'/tesouraria'},
    {label:'Movimentações',link:'/movimentacoes'},{label:'Registrar'}];
  salvando=false;erro='';agencias:Agencia[]=[];
  nova={agenciaId:'' as number|'',tipo:'SAQUE',entradaAjuste:null as boolean|null,
    valor:'' as number|'',descricao:''};
  constructor(private http:HttpClient,private router:Router,private agenciasService:AgenciasService){}
  ngOnInit(){this.agenciasService.listar('','','CODIGO','ASC',0,100).subscribe({
    next:r=>this.agencias=r.itens.filter(a=>a.ativo!==false),
    error:()=>this.erro='Não foi possível carregar as agências.'
  });}
  salvar(){
    if(!this.nova.agenciaId||!this.nova.valor||!this.nova.descricao.trim()
      ||(this.nova.tipo==='AJUSTE'&&this.nova.entradaAjuste===null)){
      this.erro='Preencha todos os campos da movimentação.';return;
    }
    this.confirmacao.open();
  }
  confirmarRegistro(){
    this.salvando=true;this.erro='';
    this.http.post('/api/v1/movimentacoes',{...this.nova,agenciaId:Number(this.nova.agenciaId),
      valor:Number(this.nova.valor),entradaAjuste:this.nova.tipo==='AJUSTE'?this.nova.entradaAjuste:null,
      idempotencyKey:crypto.randomUUID()}).subscribe({
      next:()=>this.router.navigate(['/movimentacoes/consultar'],{state:{mensagem:'Movimentação registrada com sucesso.'}}),
      error:(e:HttpErrorResponse)=>{this.salvando=false;this.erro=e.error?.msgError||e.error?.message||'Não foi possível registrar a movimentação.';}
    });
  }
  voltar(){this.router.navigate(['/movimentacoes']);}
}
