package br.com.coin.cadastroprodutos.specifications;


import br.com.coin.cadastroprodutos.dtos.FiltroProdutoDTO;
import br.com.coin.cadastroprodutos.entities.Produto;
import org.springframework.data.jpa.domain.Specification;

public class ProdutoSpecification {

    private ProdutoSpecification() {
    }

    public static Specification<Produto> comFiltros(FiltroProdutoDTO filtro) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (filtro.busca() != null && !filtro.busca().isBlank()) {
                String termo = "%" + filtro.busca().toLowerCase() + "%";

                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("nome")),
                                termo
                        )
                );
            }

            if (filtro.status() != null && filtro.status().equalsIgnoreCase("ATIVOS")) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.isTrue(root.get("ativo"))
                );
            }

            if (filtro.status() != null && filtro.status().equalsIgnoreCase("INATIVOS")) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.isFalse(root.get("ativo"))
                );
            }

            if (filtro.precoMinimo() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("preco"),
                                filtro.precoMinimo()
                        )
                );
            }

            if (filtro.precoMaximo() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("preco"),
                                filtro.precoMaximo()
                        )
                );
            }

            return predicates;
        };
    }
}