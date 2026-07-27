package br.com.gestaonumerario.api.adapter.output.repository.entity;

import br.com.gestaonumerario.api.core.domain.enums.StatusOperacaoNumerario;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Entity
@Table(name = "operacao_numerario")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperacaoNumerarioEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false) private SolicitacaoAbastecimentoEntity solicitacao;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origem_id", nullable = false) private UnidadeOperacionalEntity origem;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destino_id", nullable = false) private UnidadeOperacionalEntity destino;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 25) private StatusOperacaoNumerario status;
    @Column(name="valor_programado", nullable=false, precision=19, scale=2) private BigDecimal valorProgramado;
    @Column(name="valor_expedido", precision=19, scale=2) private BigDecimal valorExpedido;
    @Column(name="valor_recebido", precision=19, scale=2) private BigDecimal valorRecebido;
    @Column(name="valor_divergencia", precision=19, scale=2) private BigDecimal valorDivergencia;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="programado_por_id") private UsuarioEntity programadoPor;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="expedido_por_id") private UsuarioEntity expedidoPor;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="recebido_por_id") private UsuarioEntity recebidoPor;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="conciliado_por_id") private UsuarioEntity conciliadoPor;
    @Column(name="data_programacao", nullable=false) private Instant dataProgramacao;
    @Column(name="data_expedicao") private Instant dataExpedicao;
    @Column(name="data_recebimento") private Instant dataRecebimento;
    @Column(name="data_conciliacao") private Instant dataConciliacao;
    @Column(name="justificativa_divergencia", length=500) private String justificativaDivergencia;
    @Column(name="descricao_ocorrencia", length=500) private String descricaoOcorrencia;
    @Column(name="idempotency_key", nullable=false, unique=true, length=80) private String idempotencyKey;
    @Version @Column(nullable=false) private long versao;

    public OperacaoNumerarioEntity(Long id, SolicitacaoAbastecimentoEntity solicitacao,
            UnidadeOperacionalEntity origem, UnidadeOperacionalEntity destino,
            StatusOperacaoNumerario status, BigDecimal valorProgramado,
            BigDecimal valorExpedido, BigDecimal valorRecebido, BigDecimal valorDivergencia,
            UsuarioEntity programadoPor, UsuarioEntity expedidoPor, UsuarioEntity recebidoPor,
            UsuarioEntity conciliadoPor, Instant dataProgramacao, Instant dataExpedicao,
            Instant dataRecebimento, Instant dataConciliacao, String justificativaDivergencia,
            String descricaoOcorrencia, String idempotencyKey, long versao) {
        this.id=id; this.solicitacao=solicitacao; this.origem=origem; this.destino=destino;
        this.status=status; this.valorProgramado=valorProgramado; this.valorExpedido=valorExpedido;
        this.valorRecebido=valorRecebido; this.valorDivergencia=valorDivergencia;
        this.programadoPor=programadoPor; this.expedidoPor=expedidoPor; this.recebidoPor=recebidoPor;
        this.conciliadoPor=conciliadoPor; this.dataProgramacao=dataProgramacao;
        this.dataExpedicao=dataExpedicao; this.dataRecebimento=dataRecebimento;
        this.dataConciliacao=dataConciliacao; this.justificativaDivergencia=justificativaDivergencia;
        this.descricaoOcorrencia=descricaoOcorrencia; this.idempotencyKey=idempotencyKey; this.versao=versao;
    }
}
