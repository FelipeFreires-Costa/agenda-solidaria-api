package br.edu.unicatolica.agendasolidaria.domain.model;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;

/**
 * Value Object (DDD) que garante que só existam CPFs válidos no domínio.
 */
public record Cpf(String valor) {

    public Cpf {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("CPF é obrigatório");
        }
        valor = valor.replaceAll("\\D", "");
        if (!digitosVerificadoresValidos(valor)) {
            throw new RegraNegocioException("CPF inválido");
        }
    }

    public String formatado() {
        return valor.substring(0, 3) + "." + valor.substring(3, 6) + "."
                + valor.substring(6, 9) + "-" + valor.substring(9);
    }

    private static boolean digitosVerificadoresValidos(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        for (int posicao = 9; posicao < 11; posicao++) {
            int soma = 0;
            for (int i = 0; i < posicao; i++) {
                soma += (cpf.charAt(i) - '0') * (posicao + 1 - i);
            }
            int digito = (soma * 10) % 11;
            if (digito == 10) {
                digito = 0;
            }
            if (digito != cpf.charAt(posicao) - '0') {
                return false;
            }
        }
        return true;
    }
}
