package br.com.foursys.produtos.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Produto {

    private Long id;
    private String nome;
    private BigDecimal preco;
    private boolean ativo;
}