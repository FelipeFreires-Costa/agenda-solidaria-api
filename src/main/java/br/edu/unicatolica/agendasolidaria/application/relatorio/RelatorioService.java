package br.edu.unicatolica.agendasolidaria.application.relatorio;

import br.edu.unicatolica.agendasolidaria.application.agendamento.AgendamentoDetalhado;
import br.edu.unicatolica.agendasolidaria.application.agendamento.AgendamentoService;
import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;
import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;
import br.edu.unicatolica.agendasolidaria.domain.model.StatusAgendamento;
import br.edu.unicatolica.agendasolidaria.domain.repository.FiltroAgendamento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RelatorioService {

    private final AgendamentoService agendamentoService;

    public RelatorioService(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    public ResumoAtendimentos resumir(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            throw new RegraNegocioException("Período inválido: a data final deve ser igual ou posterior à inicial");
        }
        List<AgendamentoDetalhado> agendamentos = agendamentoService.listar(
                FiltroAgendamento.periodo(inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay()));

        Map<StatusAgendamento, Long> porStatus = new EnumMap<>(StatusAgendamento.class);
        for (StatusAgendamento status : StatusAgendamento.values()) {
            porStatus.put(status, agendamentos.stream()
                    .filter(a -> a.agendamento().getStatus() == status).count());
        }

        long beneficiariosAtendidos = agendamentos.stream()
                .filter(a -> a.agendamento().getStatus() == StatusAgendamento.REALIZADO)
                .map(a -> a.beneficiario().getId())
                .distinct()
                .count();

        Map<Especialidade, List<AgendamentoDetalhado>> agrupados = agendamentos.stream()
                .collect(Collectors.groupingBy(a -> a.profissional().getEspecialidade(),
                        () -> new EnumMap<>(Especialidade.class), Collectors.toList()));

        List<ResumoAtendimentos.PorEspecialidade> porEspecialidade = new ArrayList<>();
        agrupados.forEach((especialidade, lista) -> {
            long realizados = contar(lista, StatusAgendamento.REALIZADO);
            long faltas = contar(lista, StatusAgendamento.FALTOU);
            porEspecialidade.add(new ResumoAtendimentos.PorEspecialidade(
                    especialidade, lista.size(), realizados, faltas, taxa(realizados, faltas)));
        });

        return new ResumoAtendimentos(inicio, fim, agendamentos.size(), beneficiariosAtendidos, porStatus,
                porEspecialidade,
                taxa(porStatus.get(StatusAgendamento.REALIZADO), porStatus.get(StatusAgendamento.FALTOU)));
    }

    private static long contar(List<AgendamentoDetalhado> lista, StatusAgendamento status) {
        return lista.stream().filter(a -> a.agendamento().getStatus() == status).count();
    }

    private static double taxa(long realizados, long faltas) {
        long base = realizados + faltas;
        return base == 0 ? 0.0 : Math.round(realizados * 1000.0 / base) / 10.0;
    }
}
