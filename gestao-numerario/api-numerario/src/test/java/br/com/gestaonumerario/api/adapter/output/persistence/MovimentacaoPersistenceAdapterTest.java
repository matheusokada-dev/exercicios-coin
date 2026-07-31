package br.com.gestaonumerario.api.adapter.output.persistence;

import br.com.gestaonumerario.api.adapter.output.mapper.MovimentacaoPersistenceMapper;
import br.com.gestaonumerario.api.adapter.output.repository.AgenciaJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.MovimentacaoJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.SolicitacaoAbastecimentoJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.UsuarioJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.MovimentacaoEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.SolicitacaoAbastecimentoEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.UsuarioEntity;
import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.Agencia;
import br.com.gestaonumerario.api.core.domain.model.FiltroMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.Movimentacao;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoAbastecimento;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MovimentacaoPersistenceAdapterTest {

    private MovimentacaoJpaRepository repository;
    private AgenciaJpaRepository agenciaRepository;
    private SolicitacaoAbastecimentoJpaRepository solicitacaoRepository;
    private UsuarioJpaRepository usuarioRepository;
    private MovimentacaoPersistenceMapper mapper;
    private MovimentacaoPersistenceAdapter adapter;

    @BeforeEach
    void configurar() {
        repository = mock(MovimentacaoJpaRepository.class);
        agenciaRepository = mock(AgenciaJpaRepository.class);
        solicitacaoRepository = mock(SolicitacaoAbastecimentoJpaRepository.class);
        usuarioRepository = mock(UsuarioJpaRepository.class);
        mapper = mock(MovimentacaoPersistenceMapper.class);
        adapter = new MovimentacaoPersistenceAdapter(
                repository,
                agenciaRepository,
                solicitacaoRepository,
                usuarioRepository,
                mapper
        );
    }

    @Test
    void consultaIdempotenciaEPaginaDeMovimentacoes() {
        when(repository.existsByIdempotencyKey("mov-1")).thenReturn(true);
        when(
                repository.buscar(
                        eq(7L),
                        eq(TipoMovimentacao.DEPOSITO),
                        any(),
                        any(),
                        any(Pageable.class)
                )
        ).thenReturn(new PageImpl<>(List.of()));
        var filtro = new FiltroMovimentacao(
                7L,
                TipoMovimentacao.DEPOSITO,
                LocalDate.of(
                        2026,
                        7,
                        1
                ),
                LocalDate.of(
                        2026,
                        7,
                        31
                ),
                0,
                20
        );

        assertThat(adapter.existePorIdempotencyKey(null)).isFalse();
        assertThat(adapter.existePorIdempotencyKey("mov-1")).isTrue();
        assertThat(
                adapter.buscar(filtro)
                        .itens()
        ).isEmpty();
        verify(repository).existsByIdempotencyKey("mov-1");
    }

    @Test
    void resumeEntradasESaidasDoDia() {
        when(
                repository.somarValorPorAgenciaEDirecaoNoPeriodo(
                        eq(7L),
                        eq(true),
                        any(),
                        any()
                )
        ).thenReturn(new BigDecimal("150.00"));
        when(
                repository.somarValorPorAgenciaEDirecaoNoPeriodo(
                        eq(7L),
                        eq(false),
                        any(),
                        any()
                )
        ).thenReturn(new BigDecimal("40.00"));

        var resumo = adapter.resumirDiaPorAgencia(
                7L,
                LocalDate.of(
                        2026,
                        7,
                        28
                )
        );

        assertThat(resumo.valorEntradas()).isEqualByComparingTo("150.00");
        assertThat(resumo.valorSaidas()).isEqualByComparingTo("40.00");
    }

    @Test
    void salvaMovimentacaoComReferenciasPersistentes() {
        var movimentacao = mock(Movimentacao.class);
        var agencia = mock(Agencia.class);
        var solicitacao = mock(SolicitacaoAbastecimento.class);
        var usuario = mock(Usuario.class);
        var agenciaEntity = mock(AgenciaEntity.class);
        var solicitacaoEntity = mock(SolicitacaoAbastecimentoEntity.class);
        var usuarioEntity = mock(UsuarioEntity.class);
        var entity = mock(MovimentacaoEntity.class);
        var persistida = mock(Movimentacao.class);

        when(movimentacao.getAgencia()).thenReturn(agencia);
        when(agencia.getId()).thenReturn(7L);
        when(movimentacao.getSolicitacao()).thenReturn(solicitacao);
        when(solicitacao.getId()).thenReturn(9L);
        when(movimentacao.getUsuario()).thenReturn(usuario);
        when(usuario.getId()).thenReturn(3L);
        when(agenciaRepository.getReferenceById(7L)).thenReturn(agenciaEntity);
        when(solicitacaoRepository.getReferenceById(9L)).thenReturn(solicitacaoEntity);
        when(usuarioRepository.getReferenceById(3L)).thenReturn(usuarioEntity);
        when(
                mapper.toEntity(
                        movimentacao,
                        agenciaEntity,
                        solicitacaoEntity,
                        usuarioEntity
                )
        ).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(persistida);

        assertThat(adapter.salvar(movimentacao)).isSameAs(persistida);
    }

    @Test
    void salvaMovimentacaoSemSolicitacaoAssociada() {
        var movimentacao = mock(Movimentacao.class);
        var agencia = mock(Agencia.class);
        var usuario = mock(Usuario.class);
        var agenciaEntity = mock(AgenciaEntity.class);
        var usuarioEntity = mock(UsuarioEntity.class);
        var entity = mock(MovimentacaoEntity.class);

        when(movimentacao.getAgencia()).thenReturn(agencia);
        when(agencia.getId()).thenReturn(7L);
        when(movimentacao.getUsuario()).thenReturn(usuario);
        when(usuario.getId()).thenReturn(3L);
        when(agenciaRepository.getReferenceById(7L)).thenReturn(agenciaEntity);
        when(usuarioRepository.getReferenceById(3L)).thenReturn(usuarioEntity);
        when(
                mapper.toEntity(
                        movimentacao,
                        agenciaEntity,
                        null,
                        usuarioEntity
                )
        ).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        adapter.salvar(movimentacao);

        verify(mapper).toDomain(entity);
    }
}
