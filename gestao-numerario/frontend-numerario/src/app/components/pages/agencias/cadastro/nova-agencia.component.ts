import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AlertComponent } from '../../../shared/alert/alert.component';
import { BreadcrumbItem, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';
import { CurrencyInputDirective } from '../../../../directives/currency-input.directive';

@Component({
  selector: 'app-nova-agencia', standalone: true,
  imports: [FormsModule, AlertComponent, PageHeaderComponent, ConfirmationDialogComponent, CurrencyInputDirective],
  templateUrl: './nova-agencia.component.html'
})
export class NovaAgenciaComponent {
  @ViewChild('confirmacao') confirmacao!:ConfirmationDialogComponent;
  readonly breadcrumbs: BreadcrumbItem[] = [
    {label:'COIN Home',link:'/menu'},{label:'Tesouraria',link:'/tesouraria'},
    {label:'Agências',link:'/agencias'},{label:'Nova agência'}];
  salvando=false; erro='';
  nova={codigo:'',nome:'',cidade:'',saldoAtual:'' as number|'',limiteMinimo:'' as number|''};
  constructor(private http:HttpClient,private router:Router){}
  salvar(){
    if(!this.nova.codigo.trim()||!this.nova.nome.trim()||!this.nova.cidade.trim()
      ||this.nova.saldoAtual===''||this.nova.limiteMinimo===''){
      this.erro='Preencha todos os campos da agência.'; return;
    }
    if(Number(this.nova.saldoAtual)<0||Number(this.nova.limiteMinimo)<0){
      this.erro='Saldo inicial e limite mínimo não podem ser negativos.'; return;
    }
    this.confirmacao.open();
  }
  confirmarCadastro(){
    this.salvando=true; this.erro='';
    this.http.post('/api/v1/agencias',{...this.nova,saldoAtual:Number(this.nova.saldoAtual),
      limiteMinimo:Number(this.nova.limiteMinimo)}).subscribe({
      next:()=>this.router.navigate(['/agencias/consultar'],{state:{mensagem:'Agência criada com sucesso.'}}),
      error:(e:HttpErrorResponse)=>{this.salvando=false;this.erro=e.error?.msgError||e.error?.message||'Não foi possível criar a agência.';}
    });
  }
  voltar(){this.router.navigate(['/agencias']);}
}
