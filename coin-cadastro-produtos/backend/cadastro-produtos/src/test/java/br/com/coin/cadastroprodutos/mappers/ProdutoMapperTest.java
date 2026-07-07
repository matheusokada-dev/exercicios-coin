package br.com.coin.cadastroprodutos.mappers;

import br.com.coin.cadastroprodutos.dtos.ProdutoRequestDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDTO;
import br.com.coin.cadastroprodutos.entities.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdutoMapperTest {

    private final ProdutoMapper mapper = new ProdutoMapper();

    @Test
    void deveConverterRequestParaEntidadeComNomePadronizadoEAtivoTrue() {
        var request = new ProdutoRequestDTO("  cabo hdmi  ", new BigDecimal("89.90"));

        Produto resultado = mapper.toEntity(request);

        assertEquals("Cabo HDMI", resultado.getNome());
        assertEquals(new BigDecimal("89.90"), resultado.getPreco());
        assertTrue(resultado.getAtivo());
    }

    @Test
    void devePreservarSiglasConhecidasAoPadronizarNome() {
        var request = new ProdutoRequestDTO("adaptador usb led", new BigDecimal("25.00"));

        Produto resultado = mapper.toEntity(request);

        assertEquals("Adaptador USB LED", resultado.getNome());
    }

    @Test
    void deveAtualizarEntidadeComNomePrecoEStatus() {
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setNome("Mouse");
        produto.setPreco(new BigDecimal("50.00"));
        produto.setAtivo(true);

        var update = new ProdutoUpdateDTO("mouse gamer rgb", new BigDecimal("99.90"), false);

        mapper.updateEntity(produto, update);

        assertEquals("Mouse Gamer RGB", produto.getNome());
        assertEquals(new BigDecimal("99.90"), produto.getPreco());
        assertEquals(false, produto.getAtivo());
    }

    @Test
    void deveConverterEntidadeParaResponse() {
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setNome("Mouse");
        produto.setPreco(new BigDecimal("50.00"));
        produto.setAtivo(true);

        var response = mapper.toResponseDTO(produto);

        assertEquals(1L, response.id());
        assertEquals("Mouse", response.nome());
        assertEquals(new BigDecimal("50.00"), response.preco());
        assertEquals(true, response.ativo());
    }
}
