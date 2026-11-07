package br.edu.unicatolica.agendasolidaria.application.relatorio;

import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;
import br.edu.unicatolica.agendasolidaria.domain.model.StatusAgendamento;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Indicadores para a associação prestar contas do impacto social (quantas pessoas foram atendidas,
 * em quais áreas e qual a taxa de comparecimento).
 */
public record ResumoAtendimentos(LocalDate inicio,
                                 LocalDate fim,
                                 long totalAgendamentos,
                                 long beneficiariosDistintosAtendidos,
                                 Map<StatusAgendamento, Long> porStatus,
                                 List<PorEspecialidade> porEspecialidade,
                                 double taxaComparecimentoGeral) {

    public record PorEspecialidade(Especialidade especialidade,
                                   long agendamentos,
                                   long realizados,
                                   long faltas,
                                   double taxaComparecimento) {
    }
}
