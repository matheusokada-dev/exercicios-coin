package br.com.coin.cadastroprodutos.mappers;

import br.com.coin.cadastroprodutos.dtos.ProdutoDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoDtoResponse;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDtoRequest;
import br.com.coin.cadastroprodutos.entities.Produto;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

@Component
public class ProdutoMapper {

    private static final Set<String> SIGLAS = Set.of(
            "HDMI", "USB", "LED", "LCD", "SSD", "HD", "CPU", "GPU", "RAM", "TV", "DVD", "CD", "VGA", "RGB"
    );

    public Produto toEntity(ProdutoDtoRequest dto) {
        Produto produto = new Produto();
        produto.setNome(padronizarNome(dto.nome()));
        produto.setPreco(dto.preco());
        produto.setAtivo(true);
        return produto;
    }

    public ProdutoDtoResponse toDtoResponse(Produto produto) {
        return new ProdutoDtoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getPreco(),
                produto.getAtivo()
        );
    }

    public void updateEntity(Produto produto, ProdutoUpdateDtoRequest dto) {
        produto.setNome(padronizarNome(dto.nome()));
        produto.setPreco(dto.preco());
        produto.setAtivo(dto.ativo());
    }

    private String padronizarNome(String nome) {
        return Arrays.stream(nome.trim().split("\\s+"))
                .map(this::padronizarPalavra)
                .reduce((atual, proxima) -> atual + " " + proxima)
                .orElse("");
    }

    private String padronizarPalavra(String palavra) {
        String palavraMaiuscula = palavra.toUpperCase(Locale.ROOT);

        if (SIGLAS.contains(palavraMaiuscula)) {
            return palavraMaiuscula;
        }

        if (palavra.length() > 1 && palavra.equals(palavra.toUpperCase(Locale.ROOT))) {
            return palavra;
        }

        String palavraMinuscula = palavra.toLowerCase(Locale.ROOT);
        return palavraMinuscula.substring(0, 1).toUpperCase(Locale.ROOT) + palavraMinuscula.substring(1);
    }
}
