package br.edu.unicatolica.agendasolidaria.infrastructure.config;

import br.edu.unicatolica.agendasolidaria.application.agendamento.AgendamentoService;
import br.edu.unicatolica.agendasolidaria.application.agendamento.PublicadorEventosAgendamento;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraAgendamento;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraAntecedenciaMinima;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraDentroDaDisponibilidade;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraLimiteAgendamentosAtivos;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraParticipantesAtivos;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraSemConflitoBeneficiario;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraSemConflitoProfissional;
import br.edu.unicatolica.agendasolidaria.application.beneficiario.BeneficiarioService;
import br.edu.unicatolica.agendasolidaria.application.profissional.ProfissionalService;
import br.edu.unicatolica.agendasolidaria.application.relatorio.RelatorioService;
import br.edu.unicatolica.agendasolidaria.domain.event.AgendamentoObserver;
import br.edu.unicatolica.agendasolidaria.domain.repository.AgendamentoRepository;
import br.edu.unicatolica.agendasolidaria.domain.repository.BeneficiarioRepository;
import br.edu.unicatolica.agendasolidaria.domain.repository.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

/**
 * Composition Root: único ponto em que as camadas internas (application/domain), que não conhecem
 * o Spring, são montadas com suas dependências concretas.
 */
@Configuration
public class CasosDeUsoConfig {

    @Bean
    public Clock clock(@Value("${agenda.fuso-horario:America/Fortaleza}") String fusoHorario) {
        return Clock.system(ZoneId.of(fusoHorario));
    }

    @Bean
    public BeneficiarioService beneficiarioService(BeneficiarioRepository repository) {
        return new BeneficiarioService(repository);
    }

    @Bean
    public ProfissionalService profissionalService(ProfissionalRepository profissionalRepository,
                                                   AgendamentoRepository agendamentoRepository,
                                                   Clock clock) {
        return new ProfissionalService(profissionalRepository, agendamentoRepository, clock);
    }

    @Bean
    public List<RegraAgendamento> regrasAgendamento(
            AgendamentoRepository agendamentoRepository,
            @Value("${agenda.regras.antecedencia-minima-horas:2}") int antecedenciaMinimaHoras,
            @Value("${agenda.regras.limite-agendamentos-ativos:3}") int limiteAgendamentosAtivos) {
        return List.of(
                new RegraParticipantesAtivos(),
                new RegraAntecedenciaMinima(antecedenciaMinimaHoras),
                new RegraDentroDaDisponibilidade(),
                new RegraSemConflitoProfissional(agendamentoRepository),
                new RegraSemConflitoBeneficiario(agendamentoRepository),
                new RegraLimiteAgendamentosAtivos(agendamentoRepository, limiteAgendamentosAtivos));
    }

    @Bean
    public PublicadorEventosAgendamento publicadorEventosAgendamento(List<AgendamentoObserver> observadores) {
        return new PublicadorEventosAgendamento(observadores);
    }

    @Bean
    public AgendamentoService agendamentoService(AgendamentoRepository agendamentoRepository,
                                                 BeneficiarioRepository beneficiarioRepository,
                                                 ProfissionalRepository profissionalRepository,
                                                 List<RegraAgendamento> regrasAgendamento,
                                                 PublicadorEventosAgendamento publicador,
                                                 Clock clock) {
        return new AgendamentoService(agendamentoRepository, beneficiarioRepository, profissionalRepository,
                regrasAgendamento, publicador, clock);
    }

    @Bean
    public RelatorioService relatorioService(AgendamentoService agendamentoService) {
        return new RelatorioService(agendamentoService);
    }
}
