package br.com.gestaonumerario.api.adapter.output.repository.entity;

import br.com.gestaonumerario.api.core.domain.enums.EventoHistoricoSolicitacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Getter
@Entity
@Immutable
@Table(name = "historico_solicitacao_numerario")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoricoSolicitacaoNumerarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(name = "solicitacao_id")
    private SolicitacaoAbastecimentoEntity solicitacao;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operacao_id")
    private OperacaoNumerarioEntity operacao;
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private EventoHistoricoSolicitacao evento;
    @Column(
            name = "status_anterior",
            length = 25
    )
    private String statusAnterior;
    @Column(
            name = "status_novo",
            nullable = false,
            length = 25
    )
    private String statusNovo;
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;
    @Column(
            name = "data_evento",
            nullable = false
    )
    private Instant dataEvento;
    @Column(length = 500)
    private String justificativa;
    @Column(
            name = "dados_complementares",
            columnDefinition = "json"
    )
    private String dadosComplementares;

    public HistoricoSolicitacaoNumerarioEntity(
            SolicitacaoAbastecimentoEntity solicitacao,
            OperacaoNumerarioEntity operacao,
            EventoHistoricoSolicitacao evento,
            String statusAnterior,
            String statusNovo,
            UsuarioEntity usuario,
            Instant dataEvento,
            String justificativa,
            String dadosComplementares) {
        this.solicitacao = solicitacao;
        this.operacao = operacao;
        this.evento = evento;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.usuario = usuario;
        this.dataEvento = dataEvento;
        this.justificativa = justificativa;
        this.dadosComplementares = dadosComplementares;
    }
}
