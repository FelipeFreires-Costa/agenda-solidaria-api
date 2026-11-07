package br.edu.unicatolica.agendasolidaria.application.agendamento.regra;

import br.edu.unicatolica.agendasolidaria.domain.model.Beneficiario;
import br.edu.unicatolica.agendasolidaria.domain.model.Profissional;

import java.time.LocalDateTime;

/**
 * Dados necessários para validar uma solicitação de agendamento.
 */
public record ContextoAgendamento(Beneficiario beneficiario,
                                  Profissional profissional,
                                  LocalDateTime inicio,
                                  LocalDateTime fim,
                                  LocalDateTime agora) {
}
