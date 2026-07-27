package br.com.gestaonumerario.bff.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record AjustarDivergenciaRequest(
 @NotNull @Positive Long unidadeId,@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal valor,
 boolean entrada,@NotBlank @Size(max=500) String justificativa,
 @NotNull @PositiveOrZero Integer versaoUnidade) {}
