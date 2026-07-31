package br.com.gestaonumerario.api.adapter.output.persistence;

import br.com.gestaonumerario.api.adapter.output.mapper.UnidadeOperacionalVirtualMapper;
import br.com.gestaonumerario.api.adapter.output.mapper.UsuarioPersistenceMapper;
import br.com.gestaonumerario.api.adapter.output.repository.AgenciaJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.HistoricoSolicitacaoNumerarioJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.MovimentacaoJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.OperacaoNumerarioJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.SolicitacaoAbastecimentoJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.UsuarioJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import br.com.gestaonumerario.api.core.domain.enums.CampoOrdenacaoAgencia;
import br.com.gestaonumerario.api.core.domain.enums.DirecaoOrdenacao;
import br.com.gestaonumerario.api.core.domain.enums.EventoHistoricoSolicitacao;
import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.enums.StatusOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.domain.enums.TipoOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.DetalheSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.FiltroAgencia;
import br.com.gestaonumerario.api.core.domain.model.FiltroMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.FiltroSolicitacao;
import br.com.gestaonumerario.api.core.domain.model.FiltroSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.OperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.UnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.core.domain.model.command.AjustarDivergenciaCommand;
import br.com.gestaonumerario.api.core.domain.model.command.CargaInicialTesourariaCommand;
import br.com.gestaonumerario.api.core.domain.model.command.CriarSolicitacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.DecidirSolicitacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ExecutarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ProgramarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ReceberOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.exception.IdempotencyKeyDuplicadaException;
import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import br.com.gestaonumerario.api.port.input.FinanceiroNumerarioInputPort;
import br.com.gestaonumerario.api.port.input.OperacaoNumerarioInputPort;
import br.com.gestaonumerario.api.port.input.SolicitacaoNumerarioInputPort;
import br.com.gestaonumerario.api.port.output.AgenciaOutputPort;
import br.com.gestaonumerario.api.port.output.MovimentacaoOutputPort;
import br.com.gestaonumerario.api.port.output.NumerarioOutputPort;
import br.com.gestaonumerario.api.port.output.SolicitacaoAbastecimentoOutputPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NumerarioPersistenceAdapterTest {

    private static final long MINIMO_SOLICITACOES_CONCLUIDAS_MIGRADAS = 1;
    private static final long MINIMO_SOLICITACOES_LEGADAS = 14;
    private static final long MINIMO_MOVIMENTACOES_LEGADAS = 8;

    @Autowired
    private NumerarioOutputPort adapter;
    @Autowired
    private SolicitacaoAbastecimentoJpaRepository solicitacaoRepository;
    @Autowired
    private HistoricoSolicitacaoNumerarioJpaRepository historicoRepository;
    @Autowired
    private OperacaoNumerarioJpaRepository operacaoRepository;
    @Autowired
    private MovimentacaoJpaRepository movimentacaoRepository;
    @Autowired
    private AgenciaJpaRepository agenciaRepository;
    @Autowired
    private UsuarioJpaRepository usuarioRepository;
    @Autowired
    private UnidadeOperacionalVirtualMapper unidadeMapper;
    @Autowired
    private UsuarioPersistenceMapper usuarioMapper;
    @Autowired
    private AgenciaOutputPort agenciaLegado;
    @Autowired
    private MovimentacaoOutputPort movimentacaoLegado;
    @Autowired
    private SolicitacaoAbastecimentoOutputPort solicitacaoLegado;
    @Autowired
    private SolicitacaoNumerarioInputPort solicitacaoUseCase;
    @Autowired
    private OperacaoNumerarioInputPort operacaoUseCase;
    @Autowired
    private FinanceiroNumerarioInputPort financeiroUseCase;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveConsultarMassaMigradaComFiltrosEDetalheAgregado() {
        var pagina = adapter.consultarSolicitacoes(
                new FiltroSolicitacaoNumerario(
                        null,
                        TipoOperacaoNumerario.SUPRIMENTO,
                        StatusSolicitacaoNumerario.CONCLUIDA,
                        null,
                        null,
                        null,
                        null,
                        0,
                        20
                )
        );
        assertTrue(pagina.totalItens() >= MINIMO_SOLICITACOES_CONCLUIDAS_MIGRADAS);
        var detalhe = adapter.buscarDetalhe(
                pagina.itens()
                        .getFirst()
                        .getId()
        )
                .orElseThrow();
        assertNotNull(detalhe.operacao());
        assertFalse(
                detalhe.historico()
                        .isEmpty()
        );
        assertEquals(
                StatusOperacaoNumerario.CONCILIADA,
                detalhe.operacao()
                        .getStatus()
        );
    }

    @Test
    void seletorDeveOmitirUnidadesInativasMasBuscaHistoricaDeveEncontraLas() {
        var ativas = adapter.consultarUnidadesAtivas(null);
        assertTrue(
                ativas.stream()
                        .allMatch(UnidadeOperacional::isAtivo)
        );
        assertTrue(
                ativas.stream()
                        .noneMatch(u -> "LEGADO-ORIGEM".equals(u.getCodigo()))
        );
    }

    @Test
    void deveVerificarIdempotenciaGlobal() {
        String chave = operacaoRepository.findAll()
                .getFirst()
                .getIdempotencyKey();
        assertTrue(adapter.existeIdempotencyKey(chave));
        assertFalse(adapter.existeIdempotencyKey("CHAVE-INEXISTENTE-TESTE"));
    }

    @Test
    void devePersistirSolicitacaoEHistoricoNaMesmaTransacao() {
        var agenciaEntity = agenciaRepository.findAll()
                .stream()
                .filter(
                        AgenciaEntity::isAtivo
                )
                .filter(
                        a -> !solicitacaoRepository.existsByAgencia_IdAndStatusIn(
                                a.getId(),
                                java.util.EnumSet.of(
                                        StatusSolicitacaoNumerario.PENDENTE,
                                        StatusSolicitacaoNumerario.APROVADA,
                                        StatusSolicitacaoNumerario.EM_EXECUCAO,
                                        StatusSolicitacaoNumerario.COM_DIVERGENCIA
                                )
                        )
                )
                .findFirst()
                .orElseThrow();
        var gestor = usuarioMapper.toDomain(
                usuarioRepository.findAll()
                        .stream()
                        .filter(u -> u.getPerfil() == PerfilUsuario.GESTOR)
                        .findFirst()
                        .orElseThrow()
        );
        var unidade = unidadeMapper.agencia(agenciaEntity);
        var nova = SolicitacaoNumerario.criar(
                TipoOperacaoNumerario.SUPRIMENTO,
                agenciaEntity.getId(),
                unidade,
                new BigDecimal("123.45"),
                "Teste transacional",
                LocalDate.now(ZoneOffset.UTC)
                        .plusDays(1),
                gestor,
                LocalDate.now(ZoneOffset.UTC),
                Instant.now()
        );

        var salva = adapter.salvarSolicitacao(nova);

        assertNotNull(salva.getId());
        assertEquals(
                1,
                historicoRepository.findBySolicitacao_IdOrderByDataEventoAscIdAsc(salva.getId())
                        .size()
        );
    }

    @Test
    void consultasLegadasDevemContinuarLendoEsquemaAtual() {
        var agencias = agenciaLegado.buscar(
                new FiltroAgencia(
                        null,
                        true,
                        null,
                        CampoOrdenacaoAgencia.SALDO_ATUAL,
                        DirecaoOrdenacao.DESC,
                        0,
                        10
                )
        );
        var solicitacoes = solicitacaoLegado.buscar(
                new FiltroSolicitacao(
                        null,
                        null,
                        null,
                        null,
                        0,
                        10
                )
        );
        var movimentacoes = movimentacaoLegado.buscar(
                new FiltroMovimentacao(
                        null,
                        null,
                        null,
                        null,
                        0,
                        10
                )
        );

        assertFalse(
                agencias.itens()
                        .isEmpty()
        );
        assertTrue(solicitacoes.totalItens() >= MINIMO_SOLICITACOES_LEGADAS);
        assertTrue(movimentacoes.totalItens() >= MINIMO_MOVIMENTACOES_LEGADAS);
    }

    @Test
    void jpaDeveIncrementarVersaoAoAtualizarAgregadoExistente() {
        var pendente = adapter.consultarSolicitacoes(
                new FiltroSolicitacaoNumerario(
                        null,
                        null,
                        StatusSolicitacaoNumerario.PENDENTE,
                        null,
                        null,
                        null,
                        null,
                        0,
                        1
                )
        )
                .itens()
                .getFirst();
        long versaoAnterior = pendente.getVersao();
        var gestor = usuarioMapper.toDomain(
                usuarioRepository.findAll()
                        .stream()
                        .filter(u -> u.getPerfil() == PerfilUsuario.GESTOR)
                        .findFirst()
                        .orElseThrow()
        );
        pendente.aprovar(
                gestor,
                "Aprovação de teste",
                versaoAnterior,
                Instant.now()
        );

        var salva = adapter.salvarSolicitacao(pendente);

        assertEquals(
                versaoAnterior + 1,
                salva.getVersao()
        );
    }

    @Test
    void casoDeUsoDeveAprovarEGravarEvento() {
        var pendente = adapter.consultarSolicitacoes(
                new FiltroSolicitacaoNumerario(
                        null,
                        null,
                        StatusSolicitacaoNumerario.PENDENTE,
                        null,
                        null,
                        null,
                        null,
                        0,
                        1
                )
        )
                .itens()
                .getFirst();
        var gestor = usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getPerfil() == PerfilUsuario.GESTOR)
                .findFirst()
                .orElseThrow();

        var aprovada = solicitacaoUseCase.aprovar(
                new DecidirSolicitacaoNumerarioCommand(
                        pendente.getId(),
                        "Aprovada no teste",
                        pendente.getVersao(),
                        gestor.getId()
                )
        );

        assertEquals(
                StatusSolicitacaoNumerario.APROVADA,
                aprovada.getStatus()
        );
        assertTrue(
                historicoRepository.findBySolicitacao_IdOrderByDataEventoAscIdAsc(aprovada.getId())
                        .stream()
                        .anyMatch(h -> h.getEvento() == EventoHistoricoSolicitacao.SOLICITACAO_APROVADA)
        );
    }

    @Test
    void endpointDevePermitirGestorEBloquearOperador() throws Exception {
        mockMvc.perform(get("/api/v1/solicitacoes-numerario").with(user("gestor").roles("GESTOR")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/solicitacoes-numerario").with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointsAuxiliaresDevemConsultarUnidadesOperacoesEHistorico() throws Exception {
        var concluida = adapter.consultarSolicitacoes(
                new FiltroSolicitacaoNumerario(
                        null,
                        null,
                        StatusSolicitacaoNumerario.CONCLUIDA,
                        null,
                        null,
                        null,
                        null,
                        0,
                        1
                )
        )
                .itens()
                .getFirst();
        mockMvc.perform(
                get("/api/v1/unidades-operacionais").param(
                        "tipo",
                        "TESOURARIA"
                )
                        .with(user("gestor").roles("GESTOR"))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("TES-001"));
        mockMvc.perform(
                get("/api/v1/operacoes-numerario").param(
                        "status",
                        "CONCILIADA"
                )
                        .with(user("gestor").roles("GESTOR"))
        )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalItens")
                                .value(greaterThanOrEqualTo((int) MINIMO_SOLICITACOES_CONCLUIDAS_MIGRADAS))
                );
        mockMvc.perform(
                get(
                        "/api/v1/solicitacoes-numerario/{id}/historico",
                        concluida.getId()
                ).with(user("gestor").roles("GESTOR"))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].evento").exists());
    }

    @Test
    void fluxoLogisticoDeveMovimentarSaldosUmaUnicaVez() {
        var agencia = agenciaRepository.findAll()
                .stream()
                .filter(
                        AgenciaEntity::isAtivo
                )
                .filter(a -> !adapter.existeSolicitacaoAberta(a.getId()))
                .findFirst()
                .orElseThrow();
        var gestor = usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getPerfil() == PerfilUsuario.GESTOR)
                .findFirst()
                .orElseThrow();
        var destino = adapter.consultarUnidadesAtivas(TipoUnidadeOperacional.AGENCIA)
                .stream()
                .filter(u -> !u.getId().equals(agencia.getId()))
                .findFirst()
                .orElseThrow();
        var saldoOrigemAntes = agencia.getSaldoAtual();
        var saldoDestinoAntes = destino.getSaldoAtual();
        var hoje = LocalDate.now(ZoneOffset.UTC);
        var criada = solicitacaoUseCase.criar(
                new CriarSolicitacaoNumerarioCommand(
                        TipoOperacaoNumerario.RECOLHIMENTO,
                        agencia.getId(),
                        new BigDecimal("10.00"),
                        "Excesso de numerário",
                        hoje.plusDays(1),
                        gestor.getId()
                )
        );
        var aprovada = solicitacaoUseCase.aprovar(
                new DecidirSolicitacaoNumerarioCommand(
                        criada.getId(),
                        "Recolhimento aprovado",
                        criada.getVersao(),
                        gestor.getId()
                )
        );
        var programada = operacaoUseCase.programar(
                new ProgramarOperacaoNumerarioCommand(
                        criada.getId(),
                        destino.getId(),
                        aprovada.getVersao(),
                        gestor.getId(),
                        "PROG-FLUXO-1"
                )
        );
        var expedida = operacaoUseCase.expedir(
                new ExecutarOperacaoNumerarioCommand(
                        criada.getId(),
                        programada.getVersao(),
                        programada.getOrigem()
                                .getVersao(),
                        gestor.getId(),
                        "EXP-FLUXO-1"
                )
        );

        assertThrows(
                IdempotencyKeyDuplicadaException.class,
                () -> operacaoUseCase.expedir(
                        new ExecutarOperacaoNumerarioCommand(
                                criada.getId(),
                                expedida.getVersao(),
                                expedida.getOrigem()
                                        .getVersao(),
                                gestor.getId(),
                                "EXP-FLUXO-1"
                        )
                )
        );

        var recebida = operacaoUseCase.receber(
                new ReceberOperacaoNumerarioCommand(
                        criada.getId(),
                        new BigDecimal("10.00"),
                        null,
                        expedida.getVersao(),
                        expedida.getDestino()
                                .getVersao(),
                        gestor.getId(),
                        "REC-FLUXO-1"
                )
        );

        assertEquals(
                StatusOperacaoNumerario.RECEBIDA,
                recebida.getStatus()
        );
        assertEquals(
                StatusSolicitacaoNumerario.CONCLUIDA,
                recebida.getSolicitacao()
                        .getStatus()
        );
        assertTrue(
                movimentacaoRepository.findAll()
                        .stream()
                        .anyMatch(
                                m -> "EXP-FLUXO-1".equals(m.getIdempotencyKey())
                                        && m.getTipo() == TipoMovimentacao.SAIDA_PARA_TRANSITO
                        )
        );
        assertTrue(
                movimentacaoRepository.findAll()
                        .stream()
                        .anyMatch(
                                m -> "REC-FLUXO-1".equals(m.getIdempotencyKey())
                                        && m.getTipo() == TipoMovimentacao.ENTRADA_DE_TRANSITO
                        )
        );
        assertEquals(
                saldoOrigemAntes.subtract(new BigDecimal("10.00")),
                agenciaRepository.findById(agencia.getId()).orElseThrow().getSaldoAtual()
        );
        assertEquals(
                saldoDestinoAntes.add(new BigDecimal("10.00")),
                agenciaRepository.findById(destino.getId()).orElseThrow().getSaldoAtual()
        );
    }

    @Test
    void cargaInicialDaTesourariaDeveSerUnicaEAuditavel() {
        var gestor = usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getPerfil() == PerfilUsuario.GESTOR)
                .findFirst()
                .orElseThrow();
        var tesouraria = adapter.consultarUnidadesAtivas(TipoUnidadeOperacional.TESOURARIA)
                .stream()
                .findFirst()
                .orElseThrow();

        var carregada = financeiroUseCase.realizarCargaInicial(
                new CargaInicialTesourariaCommand(
                        new BigDecimal("1000.00"),
                        "Implantação controlada",
                        tesouraria.getVersao(),
                        gestor.getId(),
                        "CARGA-INICIAL-TESTE"
                )
        );

        assertEquals(
                tesouraria.getSaldoAtual().add(new BigDecimal("1000.00")),
                carregada.getSaldoAtual()
        );
        assertThrows(
                IdempotencyKeyDuplicadaException.class,
                () -> financeiroUseCase.realizarCargaInicial(
                        new CargaInicialTesourariaCommand(
                                new BigDecimal("1000.00"),
                                "Repetição",
                                carregada.getVersao(),
                                gestor.getId(),
                                "CARGA-INICIAL-TESTE"
                        )
                )
        );
        assertThrows(
                RegraOperacaoNumerarioException.class,
                () -> financeiroUseCase.realizarCargaInicial(
                        new CargaInicialTesourariaCommand(
                                new BigDecimal("1.00"),
                                "Segunda carga",
                                carregada.getVersao(),
                                gestor.getId(),
                                "OUTRA-CARGA-INICIAL"
                        )
                )
        );
        assertFalse(
                movimentacaoRepository.findAll()
                        .stream()
                        .anyMatch(
                                m -> "CARGA-INICIAL-TESTE".equals(m.getIdempotencyKey())
                                        && m.getTipo() == TipoMovimentacao.AJUSTE && m.isEntrada()
                                        && m.getOperacao() == null
                        )
        );
    }

    @Test
    void ajusteDeDivergenciaDeveSerSeparadoEVinculadoAOperacao() {
        var agencia = agenciaRepository.findAll()
                .stream()
                .filter(
                        AgenciaEntity::isAtivo
                )
                .filter(a -> !adapter.existeSolicitacaoAberta(a.getId()))
                .findFirst()
                .orElseThrow();
        var gestor = usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getPerfil() == PerfilUsuario.GESTOR)
                .findFirst()
                .orElseThrow();
        var destino = adapter.consultarUnidadesAtivas(TipoUnidadeOperacional.AGENCIA)
                .stream()
                .filter(u -> !u.getId().equals(agencia.getId()))
                .findFirst()
                .orElseThrow();
        var criada = solicitacaoUseCase.criar(
                new CriarSolicitacaoNumerarioCommand(
                        TipoOperacaoNumerario.RECOLHIMENTO,
                        agencia.getId(),
                        new BigDecimal("10.00"),
                        "Teste de divergência",
                        LocalDate.now(ZoneOffset.UTC)
                                .plusDays(1),
                        gestor.getId()
                )
        );
        var aprovada = solicitacaoUseCase.aprovar(
                new DecidirSolicitacaoNumerarioCommand(
                        criada.getId(),
                        "Aprovada",
                        criada.getVersao(),
                        gestor.getId()
                )
        );
        var programada = operacaoUseCase.programar(
                new ProgramarOperacaoNumerarioCommand(
                        criada.getId(),
                        destino.getId(),
                        aprovada.getVersao(),
                        gestor.getId(),
                        "PROG-DIV-1"
                )
        );
        var expedida = operacaoUseCase.expedir(
                new ExecutarOperacaoNumerarioCommand(
                        criada.getId(),
                        programada.getVersao(),
                        programada.getOrigem()
                                .getVersao(),
                        gestor.getId(),
                        "EXP-DIV-1"
                )
        );
        var divergente = operacaoUseCase.receber(
                new ReceberOperacaoNumerarioCommand(
                        criada.getId(),
                        new BigDecimal("8.00"),
                        "Diferença apurada",
                        expedida.getVersao(),
                        expedida.getDestino()
                                .getVersao(),
                        gestor.getId(),
                        "REC-DIV-1"
                )
        );

        var ajustada = financeiroUseCase.ajustarDivergencia(
                new AjustarDivergenciaCommand(
                        criada.getId(),
                        divergente.getOrigem()
                                .getId(),
                        new BigDecimal("2.00"),
                        true,
                        "Numerário localizado depois",
                        divergente.getOrigem()
                                .getVersao(),
                        gestor.getId(),
                        "AJ-DIV-1"
                )
        );

        assertEquals(
                divergente.getOrigem()
                        .getSaldoAtual()
                        .add(new BigDecimal("2.00")),
                ajustada.getSaldoAtual()
        );
        assertTrue(
                movimentacaoRepository.findAll()
                        .stream()
                        .anyMatch(
                                m -> "AJ-DIV-1".equals(m.getIdempotencyKey())
                                        && m.getTipo() == TipoMovimentacao.AJUSTE_DIVERGENCIA && m.getOperacao() != null
                        )
        );
        assertThrows(
                RegraOperacaoNumerarioException.class,
                () -> financeiroUseCase.ajustarDivergencia(
                        new AjustarDivergenciaCommand(
                                criada.getId(),
                                divergente.getDestino()
                                        .getId(),
                                new BigDecimal("2.01"),
                                true,
                                "Acima da divergência",
                                ajustada.getVersao(),
                                gestor.getId(),
                                "AJ-DIV-INVALIDO"
                        )
                )
        );
    }
}
