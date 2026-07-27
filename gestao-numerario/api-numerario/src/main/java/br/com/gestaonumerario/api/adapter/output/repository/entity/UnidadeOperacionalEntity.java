package br.com.gestaonumerario.api.adapter.output.repository.entity;

import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Entity
@Table(name = "unidade_operacional")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UnidadeOperacionalEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private TipoUnidadeOperacional tipo;
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(name = "controla_saldo", nullable = false)
    private boolean controlaSaldo;
    @Column(name = "saldo_atual", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoAtual;
    @Column(nullable = false)
    private boolean ativo;
    @Version @Column(nullable = false)
    private long versao;
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;
    @OneToOne(mappedBy = "unidadeOperacional", fetch = FetchType.LAZY)
    private AgenciaEntity agencia;

    public UnidadeOperacionalEntity(Long id, TipoUnidadeOperacional tipo, String codigo,
            String nome, boolean controlaSaldo, BigDecimal saldoAtual, boolean ativo,
            long versao, Instant criadoEm, Instant atualizadoEm) {
        this.id = id; this.tipo = tipo; this.codigo = codigo; this.nome = nome;
        this.controlaSaldo = controlaSaldo; this.saldoAtual = saldoAtual; this.ativo = ativo;
        this.versao = versao; this.criadoEm = criadoEm; this.atualizadoEm = atualizadoEm;
    }
}
