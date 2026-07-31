package br.com.gestaonumerario.api.core.usecase.agencia;

import br.com.gestaonumerario.api.core.domain.enums.CampoOrdenacaoAgencia;
import br.com.gestaonumerario.api.core.domain.enums.DirecaoOrdenacao;
import br.com.gestaonumerario.api.core.domain.model.Agencia;
import br.com.gestaonumerario.api.core.domain.model.DetalheAgencia;
import br.com.gestaonumerario.api.core.domain.model.FiltroAgencia;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.ResumoMovimentacaoDiaria;
import br.com.gestaonumerario.api.core.domain.model.ValorMonetario;
import br.com.gestaonumerario.api.core.domain.model.command.AtualizarAgenciaCommand;
import br.com.gestaonumerario.api.core.domain.model.command.CriarAgenciaCommand;
import br.com.gestaonumerario.api.core.exception.AgenciaNaoEncontradaException;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.CodigoAgenciaDuplicadoException;
import br.com.gestaonumerario.api.port.input.AgenciaInputPort;
import br.com.gestaonumerario.api.port.output.AgenciaOutputPort;
import br.com.gestaonumerario.api.port.output.MovimentacaoOutputPort;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import br.com.gestaonumerario.api.port.output.SolicitacaoAbastecimentoOutputPort;
import br.com.gestaonumerario.api.port.output.TransacaoOutputPort;

public class AgenciaUseCase implements AgenciaInputPort {

    private final AgenciaOutputPort agenciaPort;
    private final SolicitacaoAbastecimentoOutputPort solicitacaoPort;
    private final MovimentacaoOutputPort movimentacaoPort;
    private final RelogioOutputPort relogioPort;
    private final TransacaoOutputPort transacaoPort;

    public AgenciaUseCase(
            AgenciaOutputPort agenciaPort,
            SolicitacaoAbastecimentoOutputPort solicitacaoPort,
            MovimentacaoOutputPort movimentacaoPort,
            RelogioOutputPort relogioPort,
            TransacaoOutputPort transacaoPort) {
        this.agenciaPort = agenciaPort;
        this.solicitacaoPort = solicitacaoPort;
        this.movimentacaoPort = movimentacaoPort;
        this.relogioPort = relogioPort;
        this.transacaoPort = transacaoPort;
    }

    @Override
    public Agencia criar(CriarAgenciaCommand command) {
        if (command == null) {
            throw new CampoObrigatorioException();
        }

        return transacaoPort.executar(() -> {
            String codigo = textoObrigatorio(command.codigo());

            if (agenciaPort.existePorCodigo(codigo)) {
                throw new CodigoAgenciaDuplicadoException();
            }

            return agenciaPort.salvar(
                    new Agencia(
                            null,
                            codigo,
                            command.nome(),
                            command.cidade(),
                            command.saldoAtual(),
                            command.limiteMinimo(),
                            true,
                            0
                    )
            );
        });
    }

    @Override
    public Agencia atualizar(AtualizarAgenciaCommand command) {
        if (command == null || command.agenciaId() == null) {
            throw new CampoObrigatorioException();
        }

        return transacaoPort.executar(() -> {
            Agencia agencia = buscarAgencia(command.agenciaId());
            agencia.atualizarDados(
                    command.nome(),
                    command.cidade(),
                    command.limiteMinimo()
            );
            return agenciaPort.salvar(agencia);
        });
    }

    @Override
    public Agencia buscarPorId(Long id) {
        if (id == null) {
            throw new CampoObrigatorioException();
        }
        return buscarAgencia(id);
    }

    @Override
    public Pagina<Agencia> listar(FiltroAgencia filtro) {
        if (filtro == null || filtro.pagina() < 0 || filtro.tamanho() < 1) {
            throw new CampoObrigatorioException();
        }

        FiltroAgencia filtroNormalizado = new FiltroAgencia(
                textoOpcional(filtro.busca()),
                filtro.ativo(),
                filtro.alerta(),
                filtro.ordenarPor() == null ? CampoOrdenacaoAgencia.CODIGO : filtro.ordenarPor(),
                filtro.direcao() == null ? DirecaoOrdenacao.ASC : filtro.direcao(),
                filtro.pagina(),
                filtro.tamanho()
        );
        return agenciaPort.buscar(filtroNormalizado);
    }

    @Override
    public DetalheAgencia detalhar(Long agenciaId) {
        Agencia agencia = buscarPorId(agenciaId);
        var hoje = relogioPort.hoje();
        ResumoMovimentacaoDiaria resumo = movimentacaoPort.resumirDiaPorAgencia(
                agenciaId,
                hoje
        );
        var solicitacaoAprovada = solicitacaoPort.buscarAprovadaPorAgenciaId(agenciaId);
        var valorAprovado = solicitacaoAprovada.map(solicitacao -> solicitacao.getValor())
                .orElse(ValorMonetario.zero());

        return new DetalheAgencia(
                agencia,
                hoje,
                resumo.valorEntradas(),
                resumo.valorSaidas(),
                valorAprovado,
                ValorMonetario.normalizar(
                        agencia.getSaldoAtual()
                                .add(valorAprovado)
                )
        );
    }

    @Override
    public void desativar(Long agenciaId) {
        if (agenciaId == null) {
            throw new CampoObrigatorioException();
        }

        transacaoPort.executar(() -> {
            Agencia agencia = buscarAgencia(agenciaId);
            agencia.desativar();
            agenciaPort.salvar(agencia);
            return null;
        });
    }

    private Agencia buscarAgencia(Long id) {
        return agenciaPort.buscarPorId(id)
                .orElseThrow(AgenciaNaoEncontradaException::new);
    }

    private static String textoObrigatorio(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException();
        }
        return valor.trim();
    }

    private static String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
