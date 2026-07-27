import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Agencia, TipoOperacaoNumerario } from '../../../../models/api.models';
import { AgenciasService } from '../../../../services/agencias.service';
import { AlertComponent } from '../../../shared/alert/alert.component';
import { BreadcrumbItem,PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';
import { CurrencyInputDirective } from '../../../../directives/currency-input.directive';

@Component({
  selector:'app-nova-solicitacao',
  standalone:true,
  imports:[FormsModule,AlertComponent,PageHeaderComponent,ConfirmationDialogComponent,CurrencyInputDirective],
  templateUrl: './nova-solicitacao.component.html'
})
export class NovaSolicitacaoComponent implements OnInit {
  @ViewChild('confirmacao') confirmacao!:ConfirmationDialogComponent;
  readonly breadcrumbs:BreadcrumbItem[]=[
    {label:'COIN Home',link:'/menu'},{label:'Tesouraria',link:'/tesouraria'},
    {label:'Solicitações',link:'/solicitacoes'},{label:'Nova solicitação'}
  ];
  salvando=false;erro='';agencias:Agencia[]=[];
  nova={tipoOperacao:'SUPRIMENTO' as TipoOperacaoNumerario,agenciaId:'' as number|'',
    valor:'' as number|'',motivo:'',dataDesejada:''};
  constructor(private http:HttpClient,private router:Router,private agenciasService:AgenciasService){}
  ngOnInit(){
    this.agenciasService.listar('','','CODIGO','ASC',0,100).subscribe({
      next:r=>this.agencias=r.itens.filter(a=>a.ativo!==false),
      error:()=>this.erro='Não foi possível carregar as agências.'
    });
  }
  salvar(){
    if(!this.nova.agenciaId||!this.nova.valor||!this.nova.motivo.trim()||!this.nova.dataDesejada){
      this.erro='Preencha todos os campos da solicitação.';return;
    }
    this.confirmacao.open();
  }
  confirmarCadastro(){
    this.salvando=true;this.erro='';
    this.http.post('/api/v1/solicitacoes-numerario',{
      ...this.nova,agenciaId:Number(this.nova.agenciaId),valor:Number(this.nova.valor)
    }).subscribe({
      next:()=>this.router.navigate(['/solicitacoes/consultar'],{state:{mensagem:'Solicitação criada com sucesso.'}}),
      error:(e:HttpErrorResponse)=>{this.salvando=false;this.erro=e.error?.msgError||e.error?.message||'Não foi possível criar a solicitação.';}
    });
  }
  voltar(){this.router.navigate(['/solicitacoes']);}
}
