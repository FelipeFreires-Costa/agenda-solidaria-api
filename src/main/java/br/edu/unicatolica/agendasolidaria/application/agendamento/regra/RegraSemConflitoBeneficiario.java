package br.edu.unicatolica.agendasolidaria.application.agendamento.regra;

import br.edu.unicatolica.agendasolidaria.domain.exception.ConflitoException;
import br.edu.unicatolica.agendasolidaria.domain.repository.AgendamentoRepository;

public class RegraSemConflitoBeneficiario implements RegraAgendamento {

    private final AgendamentoRepository repository;

    public RegraSemConflitoBeneficiario(AgendamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validar(ContextoAgendamento contexto) {
        boolean ocupado = repository
                .listarPorBeneficiarioEntre(contexto.beneficiario().getId(),
                        contexto.inicio().toLocalDate().atStartOfDay(),
                        contexto.inicio().toLocalDate().plusDays(1).atStartOfDay())
                .stream()
                .anyMatch(a -> a.conflitaCom(contexto.inicio(), contexto.fim()));
        if (ocupado) {
            throw new ConflitoException("O beneficiário já possui outro atendimento neste horário");
        }
    }
}
