package br.com.gestaonumerario.api.adapter.output.repository.entity;

import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacao;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoOperacaoNumerario;
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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "solicitacao_numerario")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SolicitacaoAbastecimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "agencia_id",
            nullable = false
    )
    private AgenciaEntity agencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origem_agencia_id")
    private AgenciaEntity origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_agencia_id")
    private AgenciaEntity destino;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo_operacao",
            nullable = false,
            length = 20
    )
    private TipoOperacaoNumerario tipoOperacao;

    @Column(
            name = "valor_solicitado",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal valor;

    @Column(
            nullable = false,
            length = 500
    )
    private String motivo;

    @Column(
            name = "data_desejada",
            nullable = false
    )
    private LocalDate dataDesejada;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private StatusSolicitacaoNumerario status;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "solicitante_id",
            nullable = false
    )
    private UsuarioEntity solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decisor_id")
    private UsuarioEntity decisor;

    @Column(
            name = "justificativa_decisao",
            length = 500
    )
    private String justificativaDecisao;

    @Column(
            name = "data_criacao",
            nullable = false,
            updatable = false
    )
    private Instant dataCriacao;

    @Column(name = "data_decisao")
    private Instant dataDecisao;

    @Column(name = "data_conclusao")
    private Instant dataAtendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelado_por_id")
    private UsuarioEntity canceladoPor;

    @Column(
            name = "justificativa_cancelamento",
            length = 500
    )
    private String justificativaCancelamento;

    @Column(name = "data_cancelamento")
    private Instant dataCancelamento;

    @Version
    @Column(nullable = false)
    private long versao;

    public SolicitacaoAbastecimentoEntity(
            Long id,
            AgenciaEntity agencia,
            BigDecimal valor,
            String motivo,
            LocalDate dataDesejada,
            StatusSolicitacao status,
            UsuarioEntity solicitante,
            UsuarioEntity decisor,
            String justificativaDecisao,
            String justificativaEspecial,
            Instant dataCriacao,
            Instant dataDecisao,
            Instant dataAtendimento,
            long versao) {
        this.id = id;
        this.agencia = agencia;
        this.tipoOperacao = TipoOperacaoNumerario.SUPRIMENTO;
        this.valor = valor;
        this.motivo = motivo;
        this.dataDesejada = dataDesejada;
        this.status = switch (status) {
            case PENDENTE -> StatusSolicitacaoNumerario.PENDENTE;
            case APROVADA -> StatusSolicitacaoNumerario.APROVADA;
            case REJEITADA -> StatusSolicitacaoNumerario.REJEITADA;
            case ATENDIDA -> StatusSolicitacaoNumerario.CONCLUIDA;
        };
        this.solicitante = solicitante;
        this.decisor = decisor;
        this.justificativaDecisao = justificativaDecisao;
        this.dataCriacao = dataCriacao;
        this.dataDecisao = dataDecisao;
        this.dataAtendimento = dataAtendimento;
        this.versao = versao;
    }

    public StatusSolicitacao getStatusLegado() {
        return switch (status) {
            case PENDENTE -> StatusSolicitacao.PENDENTE;
            case REJEITADA -> StatusSolicitacao.REJEITADA;
            case CONCLUIDA -> StatusSolicitacao.ATENDIDA;
            default -> StatusSolicitacao.APROVADA;
        };
    }

    public String getJustificativaEspecial() {
        return null;
    }

    public SolicitacaoAbastecimentoEntity(
            Long id,
            TipoOperacaoNumerario tipoOperacao,
            AgenciaEntity agencia,
            AgenciaEntity origem,
            AgenciaEntity destino,
            BigDecimal valor,
            String motivo,
            LocalDate dataDesejada,
            StatusSolicitacaoNumerario status,
            UsuarioEntity solicitante,
            UsuarioEntity decisor,
            String justificativaDecisao,
            Instant dataCriacao,
            Instant dataDecisao,
            Instant dataConclusao,
            UsuarioEntity canceladoPor,
            String justificativaCancelamento,
            Instant dataCancelamento,
            long versao) {
        this.id = id;
        this.tipoOperacao = tipoOperacao;
        this.agencia = agencia;
        this.origem = origem;
        this.destino = destino;
        this.valor = valor;
        this.motivo = motivo;
        this.dataDesejada = dataDesejada;
        this.status = status;
        this.solicitante = solicitante;
        this.decisor = decisor;
        this.justificativaDecisao = justificativaDecisao;
        this.dataCriacao = dataCriacao;
        this.dataDecisao = dataDecisao;
        this.dataAtendimento = dataConclusao;
        this.canceladoPor = canceladoPor;
        this.justificativaCancelamento = justificativaCancelamento;
        this.dataCancelamento = dataCancelamento;
        this.versao = versao;
    }
}
