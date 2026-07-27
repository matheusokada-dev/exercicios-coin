package br.com.gestaonumerario.api.adapter.output.mapper;

import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.UnidadeOperacionalEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.UsuarioEntity;
import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceMappersTest {

    @Test
    void  converteAgenciaUsuarioEUnidadeNosDoisSentidos() {
        Instant agora = Instant.parse("2026-07-25T12:00:00Z");
        var agenciaMapper = new AgenciaPersistenceMapper();
        var usuarioMapper = new UsuarioPersistenceMapper();
        var unidadeMapper = new UnidadeOperacionalPersistenceMapper();
        var agenciaEntity = new AgenciaEntity(1L, "001", "Centro", "São Paulo",
                BigDecimal.ZERO, BigDecimal.TEN, true, 2);
        var usuarioEntity = new UsuarioEntity(7L, "Gestor", "gestor", "hash",
                PerfilUsuario.GESTOR, true, agora, 1, agora.plusSeconds(10));
        var unidadeEntity = new UnidadeOperacionalEntity(3L, TipoUnidadeOperacional.TESOURARIA,
                "TES", "Tesouraria", true, BigDecimal.TEN, true, 4, agora, agora);

        var agencia = agenciaMapper.toDomain(agenciaEntity);
        var usuario = usuarioMapper.toDomain(usuarioEntity);
        var unidade = unidadeMapper.toDomain(unidadeEntity);

        PersistenceMappersTest(agenciaMapper.toEntity(agencia).PersistenceMappersTest()).PersistenceMappersTest("001");
        PersistenceMappersTest(usuarioMapper.toEntity(usuario).PersistenceMappersTest()).PersistenceMappersTest("gestor");
        PersistenceMappersTest(unidadeMapper.toEntity(unidade).PersistenceMappersTest()).PersistenceMappersTest("TES");


    @Test
    void preservaNulosEmTodosOsMapeadoresDePersistencia() {
        var agencia = new AgenciaPersistenceMapper();
        var usuario = new UsuarioPersistenceMapper();
        var unidade = new UnidadeOperacionalPersistenceMapper();
        var solicitacaoLegada = new SolicitacaoAbastecimentoPersistenceMapper(agencia, usuario);
        var solicitacao = new SolicitacaoNumerarioPersistenceMapper(unidade, usuario);
        var movimentacao = new MovimentacaoPersistenceMapper(agencia, solicitacaoLegada, usuario);
        var operacao = new OperacaoNumerarioPersistenceMapper(solicitacao, unidade, usuario);

        assertThat(agencia.toDomain(null)).isNull();
        assertThat(agencia.toEntity(null)).isNull();
        assertThat(usuario.toDomain(null)).isNull();
        assertThat(usuario.toEntity(null)).isNull();
        assertThat(unidade.toDomain(null)).isNull();
        assertThat(unidade.toEntity(null)).isNull();
        assertThat(solicitacaoLegada.toDomain(null)).isNull();
        assertThat(solicitacaoLegada.toEntity(null, null, null, null)).isNull();
        assertThat(solicitacao.toDomain(null)).isNull();
        assertThat(movimentacao.toDomain(null)).isNull();
        assertThat(movimentacao.toEntity(null, null, null, null)).isNull();
        assertThat(operacao.toDomain(null)).isNull();
    }
}
