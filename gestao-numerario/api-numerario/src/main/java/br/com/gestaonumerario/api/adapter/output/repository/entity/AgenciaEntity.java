package br.com.gestaonumerario.api.adapter.output.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "agencia")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 100)
    private String cidade;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_operacional_id", nullable = false)
    private UnidadeOperacionalEntity unidadeOperacional;

    @Column(name = "limite_minimo", nullable = false, precision = 19, scale = 2)
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
            long versao
    ) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.cidade = cidade;
        this.unidadeOperacional = null;
        this.limiteMinimo = limiteMinimo;
        this.ativo = ativo;
        this.versao = versao;
    }

    public AgenciaEntity(String codigo,String nome,String cidade,
            UnidadeOperacionalEntity unidadeOperacional,BigDecimal limiteMinimo) {
        this.codigo=codigo;this.nome=nome;this.cidade=cidade;
        this.unidadeOperacional=unidadeOperacional;this.limiteMinimo=limiteMinimo;
        this.ativo=true;
    }

    public BigDecimal getSaldoAtual() {
        return unidadeOperacional == null ? BigDecimal.ZERO : unidadeOperacional.getSaldoAtual();
    }
}
