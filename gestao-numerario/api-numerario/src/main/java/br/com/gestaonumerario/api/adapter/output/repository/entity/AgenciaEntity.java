package br.com.gestaonumerario.api.adapter.output.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "agencia")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true,
            length = 10
    )
    private String codigo;

    @Column(
            nullable = false,
            length = 120
    )
    private String nome;

    @Column(
            nullable = false,
            length = 100
    )
    private String cidade;

    @Column(
            name = "saldo_atual",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal saldoAtual;

    @Column(
            name = "limite_minimo",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal limiteMinimo;

    @Column(nullable = false)
    private boolean ativo;

    @Version
    @Column(nullable = false)
    private long versao;

    public AgenciaEntity(
            Long id,
            String codigo,
            String nome,
            String cidade,
            BigDecimal saldoAtual,
            BigDecimal limiteMinimo,
            boolean ativo,
            long versao) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.cidade = cidade;
        this.saldoAtual = saldoAtual;
        this.limiteMinimo = limiteMinimo;
        this.ativo = ativo;
        this.versao = versao;
    }

    public AgenciaEntity(
            String codigo,
            String nome,
            String cidade,
            BigDecimal saldoAtual,
            BigDecimal limiteMinimo) {
        this.codigo = codigo;
        this.nome = nome;
        this.cidade = cidade;
        this.saldoAtual = saldoAtual;
        this.limiteMinimo = limiteMinimo;
        this.ativo = true;
    }

    public void atualizarDados(
            String nome,
            String cidade,
            BigDecimal saldoAtual,
            BigDecimal limiteMinimo,
            boolean ativo) {
        this.nome = nome;
        this.cidade = cidade;
        this.saldoAtual = saldoAtual;
        this.limiteMinimo = limiteMinimo;
        this.ativo = ativo;
    }

    public void atualizarSaldo(BigDecimal saldoAtual) {
        this.saldoAtual = saldoAtual;
    }
}
