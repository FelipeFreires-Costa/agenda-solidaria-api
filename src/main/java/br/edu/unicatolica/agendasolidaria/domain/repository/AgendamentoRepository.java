package br.edu.unicatolica.agendasolidaria.domain.repository;

import br.edu.unicatolica.agendasolidaria.domain.model.Agendamento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository {

    Agendamento salvar(Agendamento agendamento);

    Optional<Agendamento> buscarPorId(Long id);

    List<Agendamento> listarPorProfissionalEntre(Long profissionalId, LocalDateTime inicio, LocalDateTime fim);

    List<Agendamento> listarPorBeneficiarioEntre(Long beneficiarioId, LocalDateTime inicio, LocalDateTime fim);

    List<Agendamento> listarFuturosPorBeneficiario(Long beneficiarioId, LocalDateTime aPartirDe);

    List<Agendamento> filtrar(FiltroAgendamento filtro);
}
