import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import {
  Agencia, DetalheSolicitacaoNumerario, PaginaResponse, SolicitacaoNumerario,
  StatusSolicitacaoNumerario, TipoOperacaoNumerario, UnidadeOperacional
} from '../../../../models/api.models';
import { AgenciasService } from '../../../../services/agencias.service';
import { AlertComponent } from '../../../shared/alert/alert.component';
import { BreadcrumbItem, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';

type Acao = 'aprovar'|'rejeitar'|'cancelar'|'programar'|'separar'|'expedir'|
  'ocorrencia'|'receber'|'conciliar'|'ajustar'|'';
type MetodoHttp = 'post' | 'put';
type CorpoComando = Record<string, unknown>;

@Component({
  selector: 'app-solicitacoes',
  imports: [AlertComponent,CurrencyPipe,DatePipe,FormsModule,PageHeaderComponent,PaginationComponent],
  templateUrl: './solicitacoes.component.html',
  styleUrl: './solicitacoes.component.css'
})
export class SolicitacoesComponent implements OnInit {
  @ViewChild('detalheDialog') detalheDialog!:ElementRef<HTMLDialogElement>;
  readonly breadcrumbs: BreadcrumbItem[]=[{label:'COIN Home',link:'/menu'},{label:'Tesouraria',link:'/tesouraria'},{label:'Solicitações'}];
  readonly statusDisponiveis: StatusSolicitacaoNumerario[]=['PENDENTE','APROVADA','EM_EXECUCAO','COM_DIVERGENCIA','CONCLUIDA','REJEITADA','CANCELADA'];
  agenciaId='';tipo='';status='';dataInicio='';dataFim='';pagina=0;tamanho=20;
  resultado?:PaginaResponse<SolicitacaoNumerario>;detalhe?:DetalheSolicitacaoNumerario;unidades:UnidadeOperacional[]=[];agencias:Agencia[]=[];
  carregando=false;salvando=false;erro='';sucesso='';acao:Acao='';
  nova={tipoOperacao:'SUPRIMENTO' as TipoOperacaoNumerario,agenciaId:'' as number|'',valor:'' as number|'',motivo:'',dataDesejada:''};
  form={texto:'',unidadeId:'' as number|'',valor:'' as number|'',entrada:true,idempotencyKey:''};
  constructor(private http:HttpClient,private route:ActivatedRoute,private agenciasService:AgenciasService){}
  backTo='/solicitacoes';
  get totalPaginas(){return Math.max(1,this.resultado?.totalPaginas??1);}
  ngOnInit(){this.status=this.route.snapshot.queryParamMap.get('status')||'';
    this.breadcrumbs[2].link='/solicitacoes';
    this.breadcrumbs.push({label:'Consultar'});
    this.sucesso=history.state?.mensagem||'';
    if(this.route.snapshot.queryParamMap.get('origem')==='dashboard'
        || history.state?.origem==='dashboard'){
      this.breadcrumbs.splice(2,0,{label:'Dashboard',link:'/dashboard'});
      this.backTo='/dashboard';
    }
    this.carregarAgencias();this.carregarUnidades();this.listar();}
  carregarAgencias(){this.agenciasService.listar('','','CODIGO','ASC',0,100).subscribe({
    next:r=>this.agencias=r.itens,
    error:e=>this.falha(e,'Não foi possível carregar as agências.')
  });}
  listar(reset=true){if(reset)this.pagina=0;this.carregando=true;let p=new HttpParams().set('pagina',this.pagina).set('tamanho',this.tamanho);
    for(const [k,v] of Object.entries({agenciaId:this.agenciaId,tipo:this.tipo,status:this.status,dataInicio:this.dataInicio,dataFim:this.dataFim}))if(v)p=p.set(k,v);
    this.http.get<PaginaResponse<SolicitacaoNumerario>>('/api/v1/solicitacoes-numerario',{params:p}).subscribe({next:r=>{this.resultado=r;this.carregando=false;},error:e=>this.falha(e,'Não foi possível carregar as solicitações.')});}
  limpar(){this.agenciaId=this.tipo=this.status=this.dataInicio=this.dataFim='';this.listar();}
  carregarUnidades(){this.http.get<UnidadeOperacional[]>('/api/v1/unidades-operacionais').subscribe({next:r=>this.unidades=r,error:e=>this.falha(e,'Não foi possível carregar as unidades.')});}
  criar(){const n=this.nova;if(!n.agenciaId||!n.valor||!n.motivo||!n.dataDesejada){this.erro='Preencha todos os campos da solicitação.';return;}
    this.enviar('post','/api/v1/solicitacoes-numerario',{...n,agenciaId:Number(n.agenciaId),valor:Number(n.valor)},false,'Solicitação criada com sucesso.',()=>{this.nova={tipoOperacao:'SUPRIMENTO',agenciaId:'',valor:'',motivo:'',dataDesejada:''};this.listar();});}
  detalhar(id:number){this.http.get<DetalheSolicitacaoNumerario>(`/api/v1/solicitacoes-numerario/${id}`).subscribe({next:r=>{this.detalhe=r;this.acao='';queueMicrotask(()=>{if(!this.detalheDialog.nativeElement.open)this.detalheDialog.nativeElement.showModal();});},error:e=>this.falha(e,'Não foi possível carregar o detalhe.')});}
  fecharDetalhe(){if(this.detalheDialog?.nativeElement.open)this.detalheDialog.nativeElement.close();this.detalhe=undefined;this.acao='';}
  abrirAcao(a:Acao){this.acao=a;this.form={texto:'',unidadeId:'',valor:'',entrada:true,idempotencyKey:crypto.randomUUID()};if(a==='receber'&&this.detalhe?.operacao)this.form.valor=this.detalhe.operacao.valorExpedido??'';if(a==='ajustar'&&this.detalhe?.operacao){this.form.valor=this.detalhe.operacao.valorDivergencia??'';this.form.unidadeId=this.detalhe.operacao.destinoId;}}
  executarSimples(a:'separar'|'expedir'){this.abrirAcao(a);this.confirmarAcao();}
  confirmarAcao(){if(!this.detalhe||!this.acao)return;const id=this.detalhe.solicitacao.id,o=this.detalhe.operacao,a=this.acao;let body:CorpoComando={},key=false;
    if(['aprovar','rejeitar','cancelar'].includes(a))body={justificativa:this.form.texto,versao:this.detalhe.solicitacao.versao};
    if(a==='programar'){body={unidadeFaltanteId:Number(this.form.unidadeId),versaoSolicitacao:this.detalhe.solicitacao.versao};key=true;}
    if(a==='separar')body={versaoOperacao:o?.versao};
    if(a==='expedir'){body={versaoOperacao:o?.versao,versaoUnidade:this.versaoUnidade(o?.origemId)};key=true;}
    if(a==='ocorrencia')body={descricao:this.form.texto,versaoOperacao:o?.versao};
    if(a==='receber'){body={valorRecebido:Number(this.form.valor),justificativaDivergencia:this.form.texto||null,versaoOperacao:o?.versao,versaoUnidade:this.versaoUnidade(o?.destinoId)};key=true;}
    if(a==='conciliar'){body={justificativa:this.form.texto,versaoOperacao:o?.versao};key=true;}
    if(a==='ajustar'){body={unidadeId:Number(this.form.unidadeId),valor:Number(this.form.valor),entrada:this.form.entrada,justificativa:this.form.texto,versaoUnidade:this.versaoUnidade(Number(this.form.unidadeId))};key=true;}
    const method:MetodoHttp=a==='ajustar'?'post':'put',path=a==='ajustar'?`ajustes-divergencia`:a==='ocorrencia'?'registrar-ocorrencia':a;
    this.enviar(method,`/api/v1/solicitacoes-numerario/${id}/${path}`,body,key,'Operação concluída com sucesso.',()=>{this.acao='';this.carregarUnidades();this.detalhar(id);this.listar(false);});}
  private enviar(method:MetodoHttp,url:string,body:CorpoComando,key:boolean,msg:string,done:()=>void){this.salvando=true;this.erro='';const headers=key?new HttpHeaders({'Idempotency-Key':this.form.idempotencyKey||crypto.randomUUID()}):undefined;
    const req=method==='post'?this.http.post(url,body,{headers}):this.http.put(url,body,{headers});req.subscribe({next:()=>{this.salvando=false;this.sucesso=msg;done();},error:e=>{this.salvando=false;this.falha(e,'Não foi possível concluir a operação.');}});}
  irParaPagina(p:number){this.pagina=p;this.listar(false);}
  versaoUnidade(id?:number){return this.unidades.find(u=>u.id===id)?.versao??0;}
  unidadesDaRota(){const o=this.detalhe?.operacao;return this.unidades.filter(u=>u.id===o?.origemId||u.id===o?.destinoId);}
  nomeUnidade(id?:number){if(!id)return 'A definir';const u=this.unidades.find(x=>x.id===id);return u?`${u.codigo} — ${u.nome}`:`Unidade ${id}`;}
  nomeAgencia(id:number){const a=this.agencias.find(x=>x.id===id);return a?`${a.codigo} — ${a.nome}`:`Agência ${id}`;}
  rotuloStatus(s:string){return ({PENDENTE:'Pendente',APROVADA:'Aprovada',REJEITADA:'Rejeitada',CANCELADA:'Cancelada',EM_EXECUCAO:'Em execução',COM_DIVERGENCIA:'Com divergência',CONCLUIDA:'Concluída'} as Record<string,string>)[s]??s;}
  rotuloOperacao(s:string){return ({PROGRAMADA:'Programada',EM_SEPARACAO:'Em separação',EM_TRANSITO:'Em trânsito',RECEBIDA:'Recebida',COM_DIVERGENCIA:'Com divergência',CONCILIADA:'Conciliada'} as Record<string,string>)[s]??s;}
  rotuloEvento(e:string){return e.toLowerCase().replaceAll('_',' ').replace(/^./,c=>c.toUpperCase());}
  tituloAcao(){return ({aprovar:'Aprovar solicitação',rejeitar:'Rejeitar solicitação',cancelar:'Cancelar solicitação',programar:'Programar operação',ocorrencia:'Registrar ocorrência',receber:'Confirmar recebimento',conciliar:'Conciliar divergência',ajustar:'Registrar ajuste financeiro'} as Partial<Record<Acao,string>>)[this.acao]??this.acao;}
  private falha(e:HttpErrorResponse,f:string){this.erro=e.error?.msgError||e.error?.message||f;this.carregando=false;}
}
