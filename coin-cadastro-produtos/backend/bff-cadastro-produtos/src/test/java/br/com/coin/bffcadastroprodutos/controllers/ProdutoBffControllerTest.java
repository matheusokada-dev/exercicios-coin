package br.com.coin.bffcadastroprodutos.controllers;

import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffFiltroDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffUpdateDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.response.ProdutoBffDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.response.ProdutoBffPageDtoResponse;
import br.com.coin.bffcadastroprodutos.services.ProdutoBffService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ProdutoBffController")
class ProdutoBffControllerTest {

    @Mock
    private ProdutoBffService produtoBffService;

    @InjectMocks
    private ProdutoBffController produtoBffController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(produtoBffController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve criar produto com status Created")
    void testCriarProduto_Success() throws Exception {
        ProdutoBffDtoRequest request = new ProdutoBffDtoRequest("Mouse", new BigDecimal("59.90"));
        ProdutoBffDtoResponse response = new ProdutoBffDtoResponse(1L, "Mouse", new BigDecimal("59.90"), true);

        when(produtoBffService.criar(any(ProdutoBffDtoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/bff/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mouse"))
                .andExpect(jsonPath("$.preco").value(59.90))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(produtoBffService).criar(any(ProdutoBffDtoRequest.class));
    }

    @Test
    @DisplayName("Deve listar produtos com status OK")
    void testListarProdutos_Success() throws Exception {
        ProdutoBffDtoResponse produto = new ProdutoBffDtoResponse(1L, "Mouse", new BigDecimal("59.90"), true);
        ProdutoBffPageDtoResponse<ProdutoBffDtoResponse> page =
                new ProdutoBffPageDtoResponse<>(List.of(produto), 1L, 1, 5, 0);

        when(produtoBffService.listar(any(ProdutoBffFiltroDtoRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/bff/produtos")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,asc")
                        .param("status", "todos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Mouse"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));

        verify(produtoBffService).listar(any(ProdutoBffFiltroDtoRequest.class));
    }

    @Test
    @DisplayName("Deve buscar produto por ID com status OK")
    void testBuscarProdutoPorId_Success() throws Exception {
        ProdutoBffDtoResponse response = new ProdutoBffDtoResponse(1L, "Mouse", new BigDecimal("59.90"), true);

        when(produtoBffService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/bff/produtos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mouse"))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(produtoBffService).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve atualizar produto com status OK")
    void testAtualizarProduto_Success() throws Exception {
        ProdutoBffUpdateDtoRequest request = new ProdutoBffUpdateDtoRequest("Mouse Gamer", new BigDecimal("99.90"), true);
        ProdutoBffDtoResponse response = new ProdutoBffDtoResponse(1L, "Mouse Gamer", new BigDecimal("99.90"), true);

        when(produtoBffService.atualizar(any(Long.class), any(ProdutoBffUpdateDtoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/bff/produtos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mouse Gamer"))
                .andExpect(jsonPath("$.preco").value(99.90))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(produtoBffService).atualizar(any(Long.class), any(ProdutoBffUpdateDtoRequest.class));
    }

    @Test
    @DisplayName("Deve desativar produto com status No Content")
    void testDesativarProduto_Success() throws Exception {
        mockMvc.perform(delete("/api/bff/produtos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(produtoBffService).desativar(1L);
    }

    @Test
    @DisplayName("Deve retornar Bad Request quando JSON estiver malformado")
    void testCriarProduto_MalformedRequestBody() throws Exception {
        mockMvc.perform(post("/api/bff/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }
}
