package br.com.gestaonumerario.api.adapter.output.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Getter @Entity
@Table(name="comando_idempotente")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
public class ComandoIdempotenteEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="idempotency_key",nullable=false,unique=true,length=80) private String idempotencyKey;
    @Column(name="tipo_comando",nullable=false,length=40) private String tipoComando;
    @Column(name="chave_execucao_unica",unique=true,length=40) private String chaveExecucaoUnica;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="operacao_id") private OperacaoNumerarioEntity operacao;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="usuario_id") private UsuarioEntity usuario;
    @Column(name="data_processamento",nullable=false) private Instant dataProcessamento;

    public ComandoIdempotenteEntity(String key,String tipo,OperacaoNumerarioEntity operacao,
            UsuarioEntity usuario,Instant data) {
        this.idempotencyKey=key;this.tipoComando=tipo;this.operacao=operacao;
        this.chaveExecucaoUnica="CARGA_INICIAL_TESOURARIA".equals(tipo)?tipo:null;
        this.usuario=usuario;this.dataProcessamento=data;
    }
}
