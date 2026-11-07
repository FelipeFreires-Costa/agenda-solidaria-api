package br.edu.unicatolica.agendasolidaria.application.agendamento.regra;

import br.edu.unicatolica.agendasolidaria.domain.exception.ConflitoException;
import br.edu.unicatolica.agendasolidaria.domain.repository.AgendamentoRepository;

public class RegraSemConflitoProfissional implements RegraAgendamento {

    private final AgendamentoRepository repository;

    public RegraSemConflitoProfissional(AgendamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validar(ContextoAgendamento contexto) {
        boolean ocupado = repository
                .listarPorProfissionalEntre(contexto.profissional().getId(),
                        contexto.inicio().toLocalDate().atStartOfDay(),
                        contexto.inicio().toLocalDate().plusDays(1).atStartOfDay())
                .stream()
                .anyMatch(a -> a.conflitaCom(contexto.inicio(), contexto.fim()));
        if (ocupado) {
            throw new ConflitoException("Horário já ocupado na agenda do profissional");
        }
    }
}
