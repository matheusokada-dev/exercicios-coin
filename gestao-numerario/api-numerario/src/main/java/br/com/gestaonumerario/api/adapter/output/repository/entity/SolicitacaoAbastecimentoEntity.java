package br.com.gestaonumerario.api.adapter.output.repository.entity;

import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacao;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "solicitacao_abastecimento")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SolicitacaoAbastecimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agencia_id", nullable = false)
    private AgenciaEntity agencia;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(name = "data_desejada", nullable = false)
    private LocalDate dataDesejada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacao status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private UsuarioEntity solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decisor_id")
    private UsuarioEntity decisor;

    @Column(name = "justificativa_decisao", length = 500)
    private String justificativaDecisao;

    @Column(name = "justificativa_especial", length = 500)
    private String justificativaEspecial;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private Instant dataCriacao;

    @Column(name = "data_decisao")
    private Instant dataDecisao;

    @Column(name = "data_atendimento")
    private Instant dataAtendimento;

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
            long versao
    ) {
        this.id = id;
        this.agencia = agencia;
        this.valor = valor;
        this.motivo = motivo;
        this.dataDesejada = dataDesejada;
        this.status = status;
        this.solicitante = solicitante;
        this.decisor = decisor;
        this.justificativaDecisao = justificativaDecisao;
        this.justificativaEspecial = justificativaEspecial;
        this.dataCriacao = dataCriacao;
        this.dataDecisao = dataDecisao;
        this.dataAtendimento = dataAtendimento;
        this.versao = versao;
    }
}
