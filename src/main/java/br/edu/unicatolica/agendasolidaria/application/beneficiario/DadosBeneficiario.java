package br.edu.unicatolica.agendasolidaria.application.beneficiario;

import java.time.LocalDate;

public record DadosBeneficiario(String nome, String cpf, LocalDate dataNascimento, String telefone, String email) {
}
