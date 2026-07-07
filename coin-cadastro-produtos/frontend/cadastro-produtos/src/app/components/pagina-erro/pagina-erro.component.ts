import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-pagina-erro',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './pagina-erro.component.html',
  styleUrl: './pagina-erro.component.css'
})
export class PaginaErroComponent implements OnInit {
  codigoErro = 'Erro 404';
  tituloErro = 'Página não encontrada';
  descricaoErro = 'A página que você tentou acessar não existe ou ainda não foi configurada.';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    const tipo = this.route.snapshot.queryParamMap.get('tipo');

    if (tipo === 'infra') {
      this.codigoErro = 'Erro de conexão';
      this.tituloErro = 'Não foi possível conectar';
      this.descricaoErro = 'Não conseguimos acessar os serviços necessários no momento. Tente novamente em instantes.';
    }
  }
}
