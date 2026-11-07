package br.edu.unicatolica.agendasolidaria.application.agendamento.regra;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;

public class RegraParticipantesAtivos implements RegraAgendamento {

    @Override
    public void validar(ContextoAgendamento contexto) {
        if (!contexto.beneficiario().isAtivo()) {
            throw new RegraNegocioException("Beneficiário está inativo");
        }
        if (!contexto.profissional().isAtivo()) {
            throw new RegraNegocioException("Profissional está inativo");
        }
    }
}
