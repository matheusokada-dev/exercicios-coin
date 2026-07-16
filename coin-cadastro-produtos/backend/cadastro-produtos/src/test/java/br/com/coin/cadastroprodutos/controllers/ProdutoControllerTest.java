package br.com.coin.cadastroprodutos.controllers;

import br.com.coin.cadastroprodutos.dtos.ProdutoDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoDtoResponse;
import br.com.coin.cadastroprodutos.dtos.ProdutoFiltroDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDtoRequest;
import br.com.coin.cadastroprodutos.services.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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
@DisplayName("Testes para ProdutoController")
class ProdutoControllerTest {

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private ProdutoController produtoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(produtoController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve criar produto com status Created")
    void testCriarProduto_Success() throws Exception {
        ProdutoDtoRequest request = new ProdutoDtoRequest("Arroz", new BigDecimal("10.00"));
        ProdutoDtoResponse response = new ProdutoDtoResponse(1L, "Arroz", new BigDecimal("10.00"), true);

        when(produtoService.criar(any(ProdutoDtoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Arroz"))
                .andExpect(jsonPath("$.preco").value(10.00))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(produtoService).criar(any(ProdutoDtoRequest.class));
    }

    @Test
    @DisplayName("Deve listar produtos com status OK")
    void testListarProdutos_Success() throws Exception {
        ProdutoDtoResponse produto = new ProdutoDtoResponse(1L, "Arroz", new BigDecimal("10.00"), true);
        Page<ProdutoDtoResponse> produtos = new PageImpl<>(
                List.of(produto),
                PageRequest.of(0, 5, Sort.by("nome").ascending()),
                1
        );

        when(produtoService.listar(any(ProdutoFiltroDtoRequest.class), any())).thenReturn(produtos);

        mockMvc.perform(get("/produtos")
                        .param("busca", "Arroz")
                        .param("status", "todos")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "nome,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Arroz"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));

        verify(produtoService).listar(any(ProdutoFiltroDtoRequest.class), any());
    }

    @Test
    @DisplayName("Deve buscar produto por ID com status OK")
    void testBuscarProdutoPorId_Success() throws Exception {
        ProdutoDtoResponse response = new ProdutoDtoResponse(1L, "Arroz", new BigDecimal("10.00"), true);

        when(produtoService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/produtos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Arroz"))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(produtoService).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve atualizar produto com status OK")
    void testAtualizarProduto_Success() throws Exception {
        ProdutoUpdateDtoRequest request = new ProdutoUpdateDtoRequest("Arroz Integral", new BigDecimal("12.50"), true);
        ProdutoDtoResponse response = new ProdutoDtoResponse(1L, "Arroz Integral", new BigDecimal("12.50"), true);

        when(produtoService.atualizar(any(Long.class), any(ProdutoUpdateDtoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/produtos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Arroz Integral"))
                .andExpect(jsonPath("$.preco").value(12.50))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(produtoService).atualizar(any(Long.class), any(ProdutoUpdateDtoRequest.class));
    }

    @Test
    @DisplayName("Deve desativar produto com status No Content")
    void testDesativarProduto_Success() throws Exception {
        mockMvc.perform(delete("/produtos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(produtoService).desativar(1L);
    }

    @Test
    @DisplayName("Deve retornar Bad Request quando JSON estiver malformado")
    void testCriarProduto_MalformedRequestBody() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }
}
