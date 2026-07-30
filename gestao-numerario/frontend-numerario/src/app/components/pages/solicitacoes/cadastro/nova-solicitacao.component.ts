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
  readonly hoje=dataLocalIso(new Date());
  nova={tipoOperacao:'' as TipoOperacaoNumerario|'',agenciaId:'' as number|'',
    valor:'' as number|'',motivo:'',dataDesejada:''};
  constructor(private http:HttpClient,private router:Router,private agenciasService:AgenciasService){}
  ngOnInit(){
    this.agenciasService.listar('','','CODIGO','ASC',0,100).subscribe({
      next:r=>this.agencias=r.itens.filter(a=>a.ativo!==false),
      error:()=>this.erro='Não foi possível carregar as agências.'
    });
  }
  salvar(){
    const valor=Number(this.nova.valor);
    if(!this.nova.tipoOperacao){this.erro='Selecione a operação.';return;}
    if(!this.nova.agenciaId){this.erro='Selecione a agência.';return;}
    if(this.nova.valor===''||!Number.isFinite(valor)||valor<=0){
      this.erro='Informe um valor maior que zero.';return;
    }
    if(!this.nova.dataDesejada){this.erro='Informe a data desejada.';return;}
    if(!this.nova.motivo.trim()){this.erro='Informe o motivo da solicitação.';return;}
    if(this.nova.motivo.trim().length>500){this.erro='O motivo deve ter no máximo 500 caracteres.';return;}
    const hoje=new Date();hoje.setHours(0,0,0,0);
    if(new Date(`${this.nova.dataDesejada}T00:00:00`).getTime()<hoje.getTime()){
      this.erro='A data desejada não pode estar no passado.';return;
    }
    this.nova.motivo=this.nova.motivo.trim();
    this.erro='';
    this.confirmacao.open();
  }
  confirmarCadastro(){
    this.salvando=true;this.erro='';
    this.http.post('/api/v1/solicitacoes-numerario',{
      ...this.nova,agenciaId:Number(this.nova.agenciaId),valor:Number(this.nova.valor)
    }).subscribe({
      next:()=>this.router.navigate(['/solicitacoes/consultar'],{state:{mensagem:'Solicitação criada com sucesso.'}}),
      error:(e:HttpErrorResponse)=>{
        this.salvando=false;
        this.erro=e.error?.fields?.[0]?.message||e.error?.msgError||e.error?.message
          ||'Não foi possível criar a solicitação.';
      }
    });
  }
  voltar(){this.router.navigate(['/solicitacoes']);}
}

function dataLocalIso(data:Date):string{
  const ano=data.getFullYear();
  const mes=String(data.getMonth()+1).padStart(2,'0');
  const dia=String(data.getDate()).padStart(2,'0');
  return `${ano}-${mes}-${dia}`;
}
