package br.edu.unicatolica.agendasolidaria.application.agendamento.regra;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;

public class RegraDentroDaDisponibilidade implements RegraAgendamento {

    @Override
    public void validar(ContextoAgendamento contexto) {
        if (!contexto.profissional().atendeNoIntervalo(contexto.inicio(), contexto.fim())) {
            throw new RegraNegocioException("O profissional não atende neste dia/horário");
        }
    }
}
