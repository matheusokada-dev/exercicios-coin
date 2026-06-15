package br.com.foursys;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    static void main() {
        try (Scanner sc = new Scanner(System.in)) {
            int op;
            do {
                System.out.println("""
                        Digite qual função do sistema deseja usar:
                        1 - Verificar se é par ou ímpar.
                        2 - Calcular a média.
                        3 - Calculadora.
                        0 - Sair.
                        """);
                op = sc.nextInt();

                switch (op) {
                    case 1 -> verificarSeParOuImpar(sc);
                    case 2 -> calcularMedia(sc);
                    case 3 -> calculadora(sc);
                    case 0 -> System.out.println("Encerrando...");
                    default -> System.out.println("Opção inválida");
                }


            } while (op != 0);


        }
    }

    public static void calcularMedia(Scanner sc) {
        System.out.println("Digite a quantidades notas:");

        int quantidadeNotas = sc.nextInt();

        if (quantidadeNotas <= 0) {
            System.out.println("Número não pode ser menor ou igual a zero.");
            return;
        }

        BigDecimal soma = BigDecimal.ZERO;
        List<BigDecimal> notas = new ArrayList<>();
        for (int i = 0; i < quantidadeNotas; i++) {
            float nota;

            do {
                System.out.printf("Digite a %d nota: %n", i + 1);

                nota = sc.nextFloat();
                if (nota < 0 || nota > 10) {
                    System.out.println("Nota inválida. Precisa estar entre 0 e 10.");
                }

            } while (nota < 0 || nota > 10);

            BigDecimal notaConvertida = BigDecimal.valueOf(nota);
            notas.add(notaConvertida);

            soma = soma.add(notaConvertida);

        }
        BigDecimal media = soma.divide(BigDecimal.valueOf(quantidadeNotas), 2, RoundingMode.HALF_UP);

        System.out.println(media);

    }

    public static void verificarSeParOuImpar(Scanner sc) {


        System.out.println("Digite um número inteiro: ");
        int numero = sc.nextInt();

        String resultado = (numero % 2 == 0) ? "Número é par" : "Número ímpar";

        System.out.println(resultado);

    }

    public static void calculadora(Scanner sc) {

        System.out.println("Digite o primeiro número:");
        double numero1 = sc.nextDouble();

        System.out.println("Digite a operação (+, -, *, /):");
        String operacao = sc.next();

        System.out.println("Digite o segundo número:");
        double numero2 = sc.nextDouble();

        double resultado;

        switch (operacao) {
            case "+":
                resultado = numero1 + numero2;
                System.out.printf("Resultado: %.2f%n", resultado);
                break;

            case "-":
                resultado = numero1 - numero2;
                System.out.printf("Resultado: %.2f%n", resultado);
                break;

            case "*":
                resultado = numero1 * numero2;
                System.out.printf("Resultado: %.2f%n", resultado);
                break;

            case "/":
                if (numero2 == 0) {
                    System.out.println("Não é possível dividir por zero.");
                } else {
                    resultado = numero1 / numero2;
                    System.out.printf("Resultado: %.2f%n", resultado);
                }
                break;

            default:
                System.out.println("Operação inválida.");
                break;
        }
    }
}

