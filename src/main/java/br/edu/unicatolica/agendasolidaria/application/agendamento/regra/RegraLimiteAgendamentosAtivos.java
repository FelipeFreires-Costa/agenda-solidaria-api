package br.edu.unicatolica.agendasolidaria.application.agendamento.regra;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;
import br.edu.unicatolica.agendasolidaria.domain.repository.AgendamentoRepository;

/**
 * Regra de equidade: limita quantos atendimentos futuros uma mesma pessoa pode reservar,
 * para que as vagas voluntárias (escassas) sejam distribuídas entre mais famílias.
 */
public class RegraLimiteAgendamentosAtivos implements RegraAgendamento {

    private final AgendamentoRepository repository;
    private final int limite;

    public RegraLimiteAgendamentosAtivos(AgendamentoRepository repository, int limite) {
        this.repository = repository;
        this.limite = limite;
    }

    @Override
    public void validar(ContextoAgendamento contexto) {
        long ativos = repository.listarFuturosPorBeneficiario(contexto.beneficiario().getId(), contexto.agora())
                .stream()
                .filter(a -> a.getStatus().ocupaHorario())
                .count();
        if (ativos >= limite) {
            throw new RegraNegocioException("O beneficiário já possui " + ativos
                    + " atendimento(s) futuro(s); o limite é " + limite);
        }
    }
}
