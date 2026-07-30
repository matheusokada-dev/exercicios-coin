import { HttpErrorResponse } from '@angular/common/http';
import { Component, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AlertComponent } from '../../../shared/alert/alert.component';
import { BreadcrumbItem, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';
import { CurrencyInputDirective } from '../../../../directives/currency-input.directive';
import { AgenciasService } from '../../../../services/agencias.service';

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
  constructor(private agenciasService:AgenciasService,private router:Router){}
  salvar(){
    const codigo=this.nova.codigo.trim();
    const nome=this.nova.nome.trim();
    const cidade=this.nova.cidade.trim();
    if(!codigo){this.erro='Informe o código da agência.';return;}
    if(!/^\d+$/.test(codigo)){this.erro='O código da agência deve conter somente números.';return;}
    if(!nome){this.erro='Informe o nome da agência.';return;}
    if(!cidade){this.erro='Informe a cidade da agência.';return;}
    if(!/^[\p{L} ]+$/u.test(cidade)){
      this.erro='A cidade deve conter somente letras e espaços.';return;
    }
    if(this.nova.saldoAtual===''){this.erro='Informe o saldo inicial.';return;}
    if(this.nova.limiteMinimo===''){this.erro='Informe o limite mínimo.';return;}
    if(Number(this.nova.saldoAtual)<0||Number(this.nova.limiteMinimo)<0){
      this.erro='Saldo inicial e limite mínimo não podem ser negativos.'; return;
    }
    if(excedeLimiteMonetario(this.nova.saldoAtual)
      ||excedeLimiteMonetario(this.nova.limiteMinimo)){
      this.erro='Saldo inicial e limite mínimo devem ser de no máximo R$ 99.999.999.999.999.999,00.';return;
    }
    this.nova.codigo=codigo;this.nova.nome=nome;this.nova.cidade=cidade;
    this.erro='';
    this.confirmacao.open();
  }
  confirmarCadastro(){
    this.salvando=true; this.erro='';
    this.agenciasService.criar({
      ...this.nova,
      saldoAtual:this.nova.saldoAtual as number|string,
      limiteMinimo:this.nova.limiteMinimo as number|string
    }).subscribe({
      next:()=>this.router.navigate(['/agencias/consultar'],{state:{mensagem:'Agência criada com sucesso.'}}),
      error:(e:HttpErrorResponse)=>{
        this.salvando=false;
        this.erro=e.error?.fields?.[0]?.message||e.error?.msgError||e.error?.message
          ||'Não foi possível criar a agência.';
      }
    });
  }
  voltar(){this.router.navigate(['/agencias']);}
}

function excedeLimiteMonetario(valor:number|string):boolean{
  const texto=String(valor).replace(',','.').replace(/^0+/,'');
  const inteiro=(texto.split('.')[0]||'0').replace(/\D/g,'');
  const maximo='99999999999999999';
  return inteiro.length>maximo.length
    ||(inteiro.length===maximo.length&&inteiro>maximo);
}
