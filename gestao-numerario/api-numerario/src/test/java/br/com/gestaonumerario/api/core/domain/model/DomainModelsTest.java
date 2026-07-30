package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.SaldoInsuficienteException;
import br.com.gestaonumerario.api.core.exception.ValorDeveSerPositivoException;
import br.com.gestaonumerario.api.core.exception.ValorMonetarioObrigatorioException;
import br.com.gestaonumerario.api.core.exception.ValorNaoPodeSerNegativoException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainModelsTest {

    private static final Instant AGORA = Instant.parse("2026-07-25T12:00:00Z");

    @Test
    void agenciaNormalizaDadosCalculaLimiteEMovimentaSaldo() {
        var agencia = new Agencia(
                1L,
                " 001 ",
                " Centro ",
                " São Paulo ",
                new BigDecimal("40"),
                new BigDecimal("100"),
                true,
                2
        );

        assertThat(agencia.getCodigo()).isEqualTo("001");
        assertThat(agencia.estaAbaixoDoLimite()).isTrue();
        assertThat(agencia.calcularSugestaoAbastecimento()).isEqualByComparingTo("60.00");

        agencia.abastecer(new BigDecimal("70"));
        assertThat(agencia.calcularSugestaoAbastecimento()).isZero();
        agencia.retirar(new BigDecimal("10"));
        agencia.atualizarDados(
                " Nova ",
                " Campinas ",
                new BigDecimal("80")
        );

        assertThat(agencia.getSaldoAtual()).isEqualByComparingTo("100.00");
        assertThat(agencia.getNome()).isEqualTo("Nova");
        assertThat(agencia.getCidade()).isEqualTo("Campinas");
        assertThat(agencia.getLimiteMinimo()).isEqualByComparingTo("80.00");
        assertThat(agencia.getId()).isEqualTo(1L);
        assertThat(agencia.getVersao()).isEqualTo(2);
        agencia.desativar();
        assertThat(agencia.isAtivo()).isFalse();
        agencia.ativar();
        assertThat(agencia.isAtivo()).isTrue();
    }

    @Test
    void agenciaRejeitaTextoAusenteESaldoInsuficiente() {
        assertThatThrownBy(
                () -> new Agencia(
                        1L,
                        " ",
                        "Nome",
                        "Cidade",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        true,
                        0
                )
        ).isInstanceOf(CampoObrigatorioException.class);
        var agencia = agencia();
        assertThatThrownBy(() -> agencia.retirar(new BigDecimal("101"))).isInstanceOf(SaldoInsuficienteException.class);
    }

    @Test
    void usuarioControlaDadosAtivacaoEFalhasDeLogin() {
        var usuario = new Usuario(
                1L,
                " Gestor ",
                " gestor ",
                " hash ",
                PerfilUsuario.GESTOR,
                true,
                AGORA,
                -2,
                null
        );

        assertThat(usuario.getNome()).isEqualTo("Gestor");
        assertThat(usuario.getLogin()).isEqualTo("gestor");
        assertThat(usuario.getTentativasLoginFalhas()).isZero();
        assertThat(usuario.estaBloqueado(AGORA)).isFalse();
        usuario.registrarFalhaLogin(
                AGORA,
                2,
                Duration.ofMinutes(10)
        );
        assertThat(usuario.possuiFalhasLogin()).isTrue();
        assertThat(usuario.tentativasLoginRestantes(2)).isEqualTo(1);
        usuario.registrarFalhaLogin(
                AGORA,
                2,
                Duration.ofMinutes(10)
        );
        assertThat(usuario.estaBloqueado(AGORA.plusSeconds(1))).isTrue();
        usuario.registrarFalhaLogin(
                AGORA.plus(Duration.ofMinutes(11)),
                2,
                Duration.ofMinutes(10)
        );
        assertThat(usuario.getTentativasLoginFalhas()).isEqualTo(1);

        usuario.atualizarNome(" Novo ");
        usuario.alterarSenhaHash(" novo-hash ");
        usuario.alterarPerfil(PerfilUsuario.OPERADOR);
        usuario.desativar();
        assertThat(usuario.getNome()).isEqualTo("Novo");
        assertThat(usuario.getSenhaHash()).isEqualTo("novo-hash");
        assertThat(usuario.getPerfil()).isEqualTo(PerfilUsuario.OPERADOR);
        assertThat(usuario.isAtivo()).isFalse();
        usuario.ativar();
        usuario.limparTentativasLogin();
        assertThat(usuario.isAtivo()).isTrue();
        assertThat(usuario.possuiFalhasLogin()).isFalse();
        assertThat(usuario.getId()).isEqualTo(1L);
        assertThat(usuario.getCriadoEm()).isEqualTo(AGORA);
        assertThat(usuario.getBloqueadoAte()).isNull();
    }

    @Test
    void usuarioRejeitaCamposObrigatorios() {
        assertThatThrownBy(
                () -> usuario(
                        null,
                        PerfilUsuario.GESTOR
                )
        ).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(
                () -> usuario(
                        "hash",
                        null
                )
        ).isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void movimentacaoCalculaEntradaSaidaNormalizaEReconstitui() {
        var agencia = agencia();
        var usuario = usuario(
                "hash",
                PerfilUsuario.GESTOR
        );

        var entrada = Movimentacao.criar(
                agencia,
                null,
                TipoMovimentacao.DEPOSITO,
                new BigDecimal("10"),
                " Depósito ",
                AGORA,
                usuario,
                " chave "
        );
        assertThat(entrada.isEntrada()).isTrue();
        assertThat(entrada.getSaldoAnterior()).isEqualByComparingTo("100.00");
        assertThat(entrada.getSaldoPosterior()).isEqualByComparingTo("110.00");
        assertThat(entrada.getDescricao()).isEqualTo("Depósito");
        assertThat(entrada.getIdempotencyKey()).isEqualTo("chave");

        var saida = Movimentacao.criar(
                agencia,
                null,
                TipoMovimentacao.SAQUE,
                false,
                new BigDecimal("20"),
                " ",
                AGORA,
                usuario,
                null
        );
        assertThat(saida.getSaldoPosterior()).isEqualByComparingTo("80.00");
        assertThat(saida.getDescricao()).isNull();
        assertThat(saida.getAgencia()).isSameAs(agencia);
        assertThat(saida.getTipo()).isEqualTo(TipoMovimentacao.SAQUE);
        assertThat(saida.getValor()).isEqualByComparingTo("20.00");
        assertThat(saida.getDataMovimento()).isEqualTo(AGORA);
        assertThat(saida.getUsuario()).isSameAs(usuario);
        assertThat(saida.getSolicitacao()).isNull();

        var persistida = Movimentacao.reconstituir(
                9L,
                agencia,
                null,
                TipoMovimentacao.DEPOSITO,
                true,
                BigDecimal.ONE,
                BigDecimal.TEN,
                new BigDecimal("11"),
                null,
                AGORA,
                usuario,
                null
        );
        assertThat(persistida.getId()).isEqualTo(9L);
    }

    @Test
    void movimentacaoRejeitaDirecaoAusenteSaldoInsuficienteEObrigatorios() {
        assertThatThrownBy(
                () -> Movimentacao.criar(
                        agencia(),
                        null,
                        TipoMovimentacao.AJUSTE,
                        BigDecimal.ONE,
                        null,
                        AGORA,
                        usuario(
                                "hash",
                                PerfilUsuario.GESTOR
                        ),
                        null
                )
        ).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(
                () -> Movimentacao.criar(
                        agencia(),
                        null,
                        TipoMovimentacao.SAQUE,
                        false,
                        new BigDecimal("101"),
                        null,
                        AGORA,
                        usuario(
                                "hash",
                                PerfilUsuario.GESTOR
                        ),
                        null
                )
        ).isInstanceOf(SaldoInsuficienteException.class);
        assertThatThrownBy(
                () -> Movimentacao.reconstituir(
                        1L,
                        null,
                        null,
                        TipoMovimentacao.DEPOSITO,
                        true,
                        BigDecimal.ONE,
                        BigDecimal.ZERO,
                        BigDecimal.ONE,
                        null,
                        AGORA,
                        usuario(
                                "hash",
                                PerfilUsuario.GESTOR
                        ),
                        null
                )
        ).isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void valorMonetarioRejeitaNuloZeroComoPositivoENegativo() {
        assertThatThrownBy(() -> ValorMonetario.normalizar(null))
                .isInstanceOf(ValorMonetarioObrigatorioException.class);
        assertThatThrownBy(() -> ValorMonetario.exigirPositivo(BigDecimal.ZERO))
                .isInstanceOf(ValorDeveSerPositivoException.class);
        assertThatThrownBy(() -> ValorMonetario.exigirNaoNegativo(new BigDecimal("-0.01")))
                .isInstanceOf(ValorNaoPodeSerNegativoException.class);
    }

    private Agencia agencia() {
        return new Agencia(
                1L,
                "001",
                "Centro",
                "São Paulo",
                new BigDecimal("100"),
                new BigDecimal("50"),
                true,
                0
        );
    }

    private Usuario usuario(String hash, PerfilUsuario perfil) {
        return new Usuario(
                1L,
                "Gestor",
                "gestor",
                hash,
                perfil,
                true,
                AGORA,
                0,
                null
        );
    }
}
