package br.com.coin.bffcadastroprodutos.mappers;

import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendPageDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffUpdateDtoRequest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdutoBffMapperTest {

    private final ProdutoBffMapper mapper = Mappers.getMapper(ProdutoBffMapper.class);

    @Test
    void deveConverterRequestDaBffParaBackend() {
        var request = new ProdutoBffDtoRequest("Cabo HDMI", new BigDecimal("89.90"));

        var resultado = mapper.toBackendRequest(request);

        assertEquals("Cabo HDMI", resultado.nome());
        assertEquals(new BigDecimal("89.90"), resultado.preco());
    }

    @Test
    void deveConverterUpdateDaBffParaBackend() {
        var update = new ProdutoBffUpdateDtoRequest("Cabo USB", new BigDecimal("39.90"), false);

        var resultado = mapper.toBackendUpdate(update);

        assertEquals("Cabo USB", resultado.nome());
        assertEquals(new BigDecimal("39.90"), resultado.preco());
        assertEquals(false, resultado.ativo());
    }

    @Test
    void deveConverterResponseDoBackendParaBff() {
        var backendResponse = new ProdutoBackendDtoResponse(1L, "Mouse", new BigDecimal("59.90"), true);

        var resultado = mapper.toBffResponse(backendResponse);

        assertEquals(1L, resultado.id());
        assertEquals("Mouse", resultado.nome());
        assertEquals(new BigDecimal("59.90"), resultado.preco());
        assertEquals(true, resultado.ativo());
    }

    @Test
    void deveConverterPaginaDoBackendParaBff() {
        var produto = new ProdutoBackendDtoResponse(1L, "Teclado", new BigDecimal("120.00"), true);
        var pagina = new ProdutoBackendPageDtoResponse<>(List.of(produto), 1L, 1, 5, 0);

        var resultado = mapper.toBffPage(pagina);

        assertEquals(1L, resultado.totalElements());
        assertEquals(1, resultado.totalPages());
        assertEquals(5, resultado.size());
        assertEquals(0, resultado.number());
        assertEquals("Teclado", resultado.content().getFirst().nome());
        assertTrue(resultado.content().getFirst().ativo());
    }
}
