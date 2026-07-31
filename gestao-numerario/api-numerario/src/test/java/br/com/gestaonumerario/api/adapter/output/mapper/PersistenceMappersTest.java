package br.com.gestaonumerario.api.adapter.output.mapper;

import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.UsuarioEntity;
import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceMappersTest {

    @Test
    void converteAgenciaEUsuarioNosDoisSentidos() {
        Instant agora = Instant.parse("2026-07-25T12:00:00Z");
        var agenciaMapper = new AgenciaPersistenceMapper();
        var usuarioMapper = new UsuarioPersistenceMapper();
        var agenciaEntity = new AgenciaEntity(
                1L,
                "001",
                "Centro",
                "Sao Paulo",
                BigDecimal.ZERO,
                BigDecimal.TEN,
                true,
                2
        );
        var usuarioEntity = new UsuarioEntity(
                7L,
                "Gestor",
                "gestor",
                "hash",
                PerfilUsuario.GESTOR,
                true,
                agora,
                1,
                agora.plusSeconds(10)
        );

        assertThat(agenciaMapper.toEntity(agenciaMapper.toDomain(agenciaEntity)).getCodigo())
                .isEqualTo("001");
        assertThat(usuarioMapper.toEntity(usuarioMapper.toDomain(usuarioEntity)).getLogin())
                .isEqualTo("gestor");
    }

    @Test
    void preservaNulosNosMapeadoresDePersistencia() {
        var agencia = new AgenciaPersistenceMapper();
        var usuario = new UsuarioPersistenceMapper();
        var solicitacaoLegada = new SolicitacaoAbastecimentoPersistenceMapper(
                agencia,
                usuario
        );
        var solicitacao = new SolicitacaoNumerarioPersistenceMapper(
                new UnidadeOperacionalVirtualMapper(),
                usuario
        );
        var movimentacao = new MovimentacaoPersistenceMapper(
                agencia,
                solicitacaoLegada,
                usuario
        );
        var operacao = new OperacaoNumerarioPersistenceMapper(
                solicitacao,
                new UnidadeOperacionalVirtualMapper(),
                usuario
        );

        assertThat(agencia.toDomain(null)).isNull();
        assertThat(agencia.toEntity(null)).isNull();
        assertThat(usuario.toDomain(null)).isNull();
        assertThat(usuario.toEntity(null)).isNull();
        assertThat(solicitacaoLegada.toDomain(null)).isNull();
        assertThat(solicitacaoLegada.toEntity(null, null, null, null)).isNull();
        assertThat(solicitacao.toDomain(null)).isNull();
        assertThat(movimentacao.toDomain(null)).isNull();
        assertThat(movimentacao.toEntity(null, null, null, null)).isNull();
        assertThat(operacao.toDomain(null)).isNull();
    }
}
