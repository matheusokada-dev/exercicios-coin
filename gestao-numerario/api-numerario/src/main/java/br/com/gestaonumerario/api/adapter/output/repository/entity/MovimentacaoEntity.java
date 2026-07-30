package br.com.gestaonumerario.api.adapter.output.repository.entity;

import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
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
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Getter
@Entity
@Immutable
@Table(name = "movimentacao")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovimentacaoEntity {

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
    @JoinColumn(name = "solicitacao_id")
    private SolicitacaoAbastecimentoEntity solicitacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operacao_id")
    private OperacaoNumerarioEntity operacao;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 25
    )
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private boolean entrada;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal valor;

    @Column(
            name = "saldo_anterior",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal saldoAnterior;

    @Column(
            name = "saldo_posterior",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal saldoPosterior;

    @Column(length = 500)
    private String descricao;

    @Column(
            name = "data_movimento",
            nullable = false,
            updatable = false
    )
    private Instant dataMovimento;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "usuario_id",
            nullable = false
    )
    private UsuarioEntity usuario;

    @Column(
            name = "idempotency_key",
            unique = true,
            length = 80
    )
    private String idempotencyKey;

    public MovimentacaoEntity(
            Long id,
            AgenciaEntity agencia,
            SolicitacaoAbastecimentoEntity solicitacao,
            TipoMovimentacao tipo,
            boolean entrada,
            BigDecimal valor,
            BigDecimal saldoAnterior,
            BigDecimal saldoPosterior,
            String descricao,
            Instant dataMovimento,
            UsuarioEntity usuario,
            String idempotencyKey) {
        this.id = id;
        this.agencia = agencia;
        this.solicitacao = solicitacao;
        this.tipo = tipo;
        this.entrada = entrada;
        this.valor = valor;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = saldoPosterior;
        this.descricao = descricao;
        this.dataMovimento = dataMovimento;
        this.usuario = usuario;
        this.idempotencyKey = idempotencyKey;
    }

    public MovimentacaoEntity(
            AgenciaEntity agencia,
            SolicitacaoAbastecimentoEntity solicitacao,
            OperacaoNumerarioEntity operacao,
            TipoMovimentacao tipo,
            boolean entrada,
            BigDecimal valor,
            BigDecimal saldoAnterior,
            BigDecimal saldoPosterior,
            String descricao,
            Instant dataMovimento,
            UsuarioEntity usuario,
            String idempotencyKey) {
        this.agencia = agencia;
        this.solicitacao = solicitacao;
        this.operacao = operacao;
        this.tipo = tipo;
        this.entrada = entrada;
        this.valor = valor;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = saldoPosterior;
        this.descricao = descricao;
        this.dataMovimento = dataMovimento;
        this.usuario = usuario;
        this.idempotencyKey = idempotencyKey;
    }
}
