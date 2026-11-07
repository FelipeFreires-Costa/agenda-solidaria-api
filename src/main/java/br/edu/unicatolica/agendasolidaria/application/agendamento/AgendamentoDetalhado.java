package br.edu.unicatolica.agendasolidaria.application.agendamento;

import br.edu.unicatolica.agendasolidaria.domain.model.Agendamento;
import br.edu.unicatolica.agendasolidaria.domain.model.Beneficiario;
import br.edu.unicatolica.agendasolidaria.domain.model.Profissional;

public record AgendamentoDetalhado(Agendamento agendamento, Beneficiario beneficiario, Profissional profissional) {
}
