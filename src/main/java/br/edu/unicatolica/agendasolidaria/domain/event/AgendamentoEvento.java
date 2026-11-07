package br.edu.unicatolica.agendasolidaria.domain.event;

import br.edu.unicatolica.agendasolidaria.domain.model.Agendamento;
import br.edu.unicatolica.agendasolidaria.domain.model.Beneficiario;
import br.edu.unicatolica.agendasolidaria.domain.model.Profissional;

import java.time.LocalDateTime;

/**
 * Evento de domínio disparado a cada mudança relevante em um agendamento.
 */
public record AgendamentoEvento(TipoEventoAgendamento tipo,
                                Agendamento agendamento,
                                Beneficiario beneficiario,
                                Profissional profissional,
                                LocalDateTime ocorridoEm) {
}
