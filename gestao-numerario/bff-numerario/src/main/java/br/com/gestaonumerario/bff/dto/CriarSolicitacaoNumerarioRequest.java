package br.com.gestaonumerario.bff.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record CriarSolicitacaoNumerarioRequest(
 @NotBlank String tipoOperacao,@NotNull @Positive Long agenciaId,
 @NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal valor,@NotBlank @Size(max=500) String motivo,
 @NotNull @FutureOrPresent LocalDate dataDesejada) {}
