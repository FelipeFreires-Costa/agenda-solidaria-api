package br.edu.unicatolica.agendasolidaria.application.agendamento.regra;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;

public class RegraAntecedenciaMinima implements RegraAgendamento {

    private final int antecedenciaMinimaHoras;

    public RegraAntecedenciaMinima(int antecedenciaMinimaHoras) {
        this.antecedenciaMinimaHoras = antecedenciaMinimaHoras;
    }

    @Override
    public void validar(ContextoAgendamento contexto) {
        if (contexto.inicio().isBefore(contexto.agora().plusHours(antecedenciaMinimaHoras))) {
            throw new RegraNegocioException("O agendamento deve ser feito com pelo menos "
                    + antecedenciaMinimaHoras + " hora(s) de antecedência");
        }
    }
}
