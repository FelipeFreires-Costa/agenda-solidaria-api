package br.edu.unicatolica.agendasolidaria.application.agendamento;

import java.time.LocalDateTime;

public record SolicitacaoAgendamento(Long beneficiarioId, Long profissionalId, LocalDateTime inicio, String observacao) {
}
