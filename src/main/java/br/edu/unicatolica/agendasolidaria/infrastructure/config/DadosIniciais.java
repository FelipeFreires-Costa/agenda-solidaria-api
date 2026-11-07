package br.edu.unicatolica.agendasolidaria.infrastructure.config;

import br.edu.unicatolica.agendasolidaria.application.agendamento.AgendamentoService;
import br.edu.unicatolica.agendasolidaria.application.agendamento.SolicitacaoAgendamento;
import br.edu.unicatolica.agendasolidaria.application.beneficiario.BeneficiarioService;
import br.edu.unicatolica.agendasolidaria.application.beneficiario.DadosBeneficiario;
import br.edu.unicatolica.agendasolidaria.application.profissional.DadosProfissional;
import br.edu.unicatolica.agendasolidaria.application.profissional.ProfissionalService;
import br.edu.unicatolica.agendasolidaria.domain.model.Agendamento;
import br.edu.unicatolica.agendasolidaria.domain.model.Beneficiario;
import br.edu.unicatolica.agendasolidaria.domain.model.Disponibilidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Profissional;
import br.edu.unicatolica.agendasolidaria.domain.model.StatusAgendamento;
import br.edu.unicatolica.agendasolidaria.domain.repository.AgendamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Popula o banco H2 com dados fictícios para demonstração (pitch). Desligado nos testes.
 */
@Component
@ConditionalOnProperty(name = "agenda.seed.habilitado", havingValue = "true", matchIfMissing = true)
public class DadosIniciais implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DadosIniciais.class);

    private final BeneficiarioService beneficiarioService;
    private final ProfissionalService profissionalService;
    private final AgendamentoService agendamentoService;
    private final AgendamentoRepository agendamentoRepository;
    private final Clock clock;

    public DadosIniciais(BeneficiarioService beneficiarioService, ProfissionalService profissionalService,
                         AgendamentoService agendamentoService, AgendamentoRepository agendamentoRepository,
                         Clock clock) {
        this.beneficiarioService = beneficiarioService;
        this.profissionalService = profissionalService;
        this.agendamentoService = agendamentoService;
        this.agendamentoRepository = agendamentoRepository;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        Profissional psicologa = profissionalService.cadastrar(new DadosProfissional(
                "Ana Beatriz Lima", Especialidade.PSICOLOGIA, "CRP 11/12345", 50, List.of(
                janela(DayOfWeek.MONDAY, 8, 12), janela(DayOfWeek.WEDNESDAY, 14, 18))));
        Profissional nutricionista = profissionalService.cadastrar(new DadosProfissional(
                "Carlos Eduardo Nogueira", Especialidade.NUTRICAO, "CRN-11 4567", 30, List.of(
                janela(DayOfWeek.TUESDAY, 8, 11), janela(DayOfWeek.THURSDAY, 8, 11))));
        Profissional fisioterapeuta = profissionalService.cadastrar(new DadosProfissional(
                "Juliana Pereira Sousa", Especialidade.FISIOTERAPIA, "CREFITO-6 98765-F", 40, List.of(
                janela(DayOfWeek.FRIDAY, 13, 17))));
        profissionalService.cadastrar(new DadosProfissional(
                "Marcos Vinícius Alves", Especialidade.ASSISTENCIA_SOCIAL, "CRESS 3ª Região 7788", 30, List.of(
                janela(DayOfWeek.SATURDAY, 8, 12))));

        Beneficiario maria = beneficiarioService.cadastrar(new DadosBeneficiario(
                "Maria das Graças Oliveira", "356.230.123-53", LocalDate.of(1968, 3, 14), "(88) 99901-1001", null));
        Beneficiario jose = beneficiarioService.cadastrar(new DadosBeneficiario(
                "José Raimundo Silva", "836.077.767-50", LocalDate.of(1955, 9, 2), "(88) 99902-2002", null));
        Beneficiario francisca = beneficiarioService.cadastrar(new DadosBeneficiario(
                "Francisca Helena Costa", "936.173.048-77", LocalDate.of(1990, 12, 20), "(88) 99903-3003",
                "francisca.helena@email.com"));
        beneficiarioService.cadastrar(new DadosBeneficiario(
                "Antônio Carlos Bezerra", "676.141.169-68", LocalDate.of(2001, 6, 8), "(88) 99904-4004", null));

        LocalDate hoje = LocalDate.now(clock);
        historico(maria, psicologa, ultimo(hoje, DayOfWeek.MONDAY).atTime(8, 0), StatusAgendamento.REALIZADO);
        historico(jose, nutricionista, ultimo(hoje, DayOfWeek.TUESDAY).atTime(8, 0), StatusAgendamento.REALIZADO);
        historico(francisca, fisioterapeuta, ultimo(hoje, DayOfWeek.FRIDAY).atTime(13, 0), StatusAgendamento.FALTOU);
        historico(francisca, psicologa, ultimo(hoje, DayOfWeek.WEDNESDAY).atTime(14, 0), StatusAgendamento.REALIZADO);

        agendamentoService.agendar(new SolicitacaoAgendamento(maria.getId(), psicologa.getId(),
                proximo(hoje, DayOfWeek.MONDAY).atTime(8, 0), "Retorno - acompanhamento de ansiedade"));
        agendamentoService.agendar(new SolicitacaoAgendamento(jose.getId(), nutricionista.getId(),
                proximo(hoje, DayOfWeek.TUESDAY).atTime(8, 30), "Controle de diabetes"));

        LOG.info("Dados de demonstração carregados: 4 profissionais, 4 beneficiários, 6 agendamentos");
    }

    private void historico(Beneficiario b, Profissional p, LocalDateTime inicio, StatusAgendamento status) {
        agendamentoRepository.salvar(Agendamento.reconstituir(null, b.getId(), p.getId(), inicio,
                inicio.plusMinutes(p.getDuracaoAtendimentoMinutos()), status, null, null, inicio.minusDays(7)));
    }

    private static Disponibilidade janela(DayOfWeek dia, int horaInicio, int horaFim) {
        return new Disponibilidade(dia, LocalTime.of(horaInicio, 0), LocalTime.of(horaFim, 0));
    }

    private static LocalDate proximo(LocalDate hoje, DayOfWeek dia) {
        return hoje.plusDays(1).with(TemporalAdjusters.nextOrSame(dia));
    }

    private static LocalDate ultimo(LocalDate hoje, DayOfWeek dia) {
        return hoje.with(TemporalAdjusters.previous(dia));
    }
}
