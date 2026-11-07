package br.edu.unicatolica.agendasolidaria.domain.repository;

import br.edu.unicatolica.agendasolidaria.domain.model.StatusAgendamento;

import java.time.LocalDateTime;

/**
 * Critérios opcionais de busca de agendamentos (campos nulos são ignorados).
 */
public record FiltroAgendamento(Long profissionalId,
                                Long beneficiarioId,
                                StatusAgendamento status,
                                LocalDateTime inicioApartirDe,
                                LocalDateTime inicioAntesDe) {

    public static FiltroAgendamento periodo(LocalDateTime de, LocalDateTime ate) {
        return new FiltroAgendamento(null, null, null, de, ate);
    }
}
