package br.edu.unicatolica.agendasolidaria.application;

import br.edu.unicatolica.agendasolidaria.application.agendamento.AgendamentoDetalhado;
import br.edu.unicatolica.agendasolidaria.application.agendamento.AgendamentoService;
import br.edu.unicatolica.agendasolidaria.application.agendamento.PublicadorEventosAgendamento;
import br.edu.unicatolica.agendasolidaria.application.agendamento.SolicitacaoAgendamento;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraAntecedenciaMinima;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraDentroDaDisponibilidade;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraLimiteAgendamentosAtivos;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraParticipantesAtivos;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraSemConflitoBeneficiario;
import br.edu.unicatolica.agendasolidaria.application.agendamento.regra.RegraSemConflitoProfissional;
import br.edu.unicatolica.agendasolidaria.application.profissional.ProfissionalService;
import br.edu.unicatolica.agendasolidaria.application.relatorio.RelatorioService;
import br.edu.unicatolica.agendasolidaria.application.relatorio.ResumoAtendimentos;
import br.edu.unicatolica.agendasolidaria.domain.event.AgendamentoEvento;
import br.edu.unicatolica.agendasolidaria.domain.event.AgendamentoObserver;
import br.edu.unicatolica.agendasolidaria.domain.event.TipoEventoAgendamento;
import br.edu.unicatolica.agendasolidaria.domain.exception.ConflitoException;
import br.edu.unicatolica.agendasolidaria.domain.exception.RecursoNaoEncontradoException;
import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;
import br.edu.unicatolica.agendasolidaria.domain.model.Beneficiario;
import br.edu.unicatolica.agendasolidaria.domain.model.Cpf;
import br.edu.unicatolica.agendasolidaria.domain.model.Disponibilidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Profissional;
import br.edu.unicatolica.agendasolidaria.domain.model.StatusAgendamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgendamentoServiceTest {

    /** Segunda-feira, 09/11/2026, 07:00. */
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 9, 7, 0);
    private static final LocalDate PROXIMA_SEGUNDA = LocalDate.of(2026, 11, 16);

    private final RepositoriosEmMemoria.Agendamentos agendamentos = new RepositoriosEmMemoria.Agendamentos();
    private final RepositoriosEmMemoria.Beneficiarios beneficiarios = new RepositoriosEmMemoria.Beneficiarios();
    private final RepositoriosEmMemoria.Profissionais profissionais = new RepositoriosEmMemoria.Profissionais();
    private final List<AgendamentoEvento> eventos = new ArrayList<>();
    private RepositoriosEmMemoria.RelogioAjustavel relogio;
    private AgendamentoService service;

    private Beneficiario maria;
    private Beneficiario jose;
    private Profissional psicologa;
    private Profissional nutricionista;

    @BeforeEach
    void setUp() {
        relogio = new RepositoriosEmMemoria.RelogioAjustavel(AGORA, ZoneId.of("America/Fortaleza"));
        AgendamentoObserver capturador = eventos::add;
        AgendamentoObserver observadorComDefeito = e -> {
            throw new IllegalStateException("provedor de SMS indisponível");
        };
        service = new AgendamentoService(agendamentos, beneficiarios, profissionais, List.of(
                new RegraParticipantesAtivos(),
                new RegraAntecedenciaMinima(2),
                new RegraDentroDaDisponibilidade(),
                new RegraSemConflitoProfissional(agendamentos),
                new RegraSemConflitoBeneficiario(agendamentos),
                new RegraLimiteAgendamentosAtivos(agendamentos, 3)),
                new PublicadorEventosAgendamento(List.of(observadorComDefeito, capturador)), relogio);

        maria = beneficiarios.salvar(Beneficiario.novo("Maria das Graças", new Cpf("61053178328"),
                LocalDate.of(1968, 3, 14), "(88) 99901-1001", null));
        jose = beneficiarios.salvar(Beneficiario.novo("José Raimundo", new Cpf("92918073202"),
                LocalDate.of(1955, 9, 2), "(88) 99902-2002", null));
        psicologa = profissionais.salvar(Profissional.novo("Ana", Especialidade.PSICOLOGIA, "CRP", 50,
                List.of(janela(DayOfWeek.MONDAY, 8, 12))));
        nutricionista = profissionais.salvar(Profissional.novo("Carlos", Especialidade.NUTRICAO, "CRN", 30,
                List.of(janela(DayOfWeek.MONDAY, 8, 12), janela(DayOfWeek.TUESDAY, 8, 12))));
    }

    @Test
    void agendaAtendimentoENotificaObservadoresMesmoComUmDelesFalhando() {
        AgendamentoDetalhado resultado = agendar(maria, psicologa, PROXIMA_SEGUNDA.atTime(8, 0));

        assertThat(resultado.agendamento().getId()).isNotNull();
        assertThat(resultado.agendamento().getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(resultado.agendamento().getFim()).isEqualTo(PROXIMA_SEGUNDA.atTime(8, 50));
        assertThat(eventos).singleElement()
                .satisfies(e -> assertThat(e.tipo()).isEqualTo(TipoEventoAgendamento.CRIADO));
    }

    @Test
    void rejeitaHorarioForaDaDisponibilidade() {
        assertThatThrownBy(() -> agendar(maria, psicologa, PROXIMA_SEGUNDA.plusDays(1).atTime(8, 0)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não atende");
        assertThatThrownBy(() -> agendar(maria, psicologa, PROXIMA_SEGUNDA.atTime(11, 30)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void rejeitaConflitoNaAgendaDoProfissional() {
        agendar(maria, psicologa, PROXIMA_SEGUNDA.atTime(8, 0));

        assertThatThrownBy(() -> agendar(jose, psicologa, PROXIMA_SEGUNDA.atTime(8, 30)))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("profissional");
    }

    @Test
    void rejeitaBeneficiarioEmDoisAtendimentosNoMesmoHorario() {
        agendar(maria, psicologa, PROXIMA_SEGUNDA.atTime(8, 0));

        assertThatThrownBy(() -> agendar(maria, nutricionista, PROXIMA_SEGUNDA.atTime(8, 30)))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("beneficiário");
    }

    @Test
    void exigeAntecedenciaMinima() {
        assertThatThrownBy(() -> agendar(maria, psicologa, AGORA.plusMinutes(60)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("antecedência");
        assertThat(agendar(maria, psicologa, AGORA.plusHours(2)).agendamento().getId()).isNotNull();
    }

    @Test
    void limitaQuantidadeDeAgendamentosFuturosPorBeneficiario() {
        agendar(maria, psicologa, PROXIMA_SEGUNDA.atTime(8, 0));
        agendar(maria, psicologa, PROXIMA_SEGUNDA.plusWeeks(1).atTime(8, 0));
        agendar(maria, nutricionista, PROXIMA_SEGUNDA.plusDays(1).atTime(8, 0));

        assertThatThrownBy(() -> agendar(maria, nutricionista, PROXIMA_SEGUNDA.plusDays(8).atTime(8, 0)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("limite");
    }

    @Test
    void cancelamentoLiberaOHorarioParaOutraPessoa() {
        Long id = agendar(maria, psicologa, PROXIMA_SEGUNDA.atTime(8, 0)).agendamento().getId();

        service.cancelar(id, "Consulta remarcada");

        assertThat(agendar(jose, psicologa, PROXIMA_SEGUNDA.atTime(8, 0)).agendamento().getStatus())
                .isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(eventos).extracting(AgendamentoEvento::tipo).containsExactly(
                TipoEventoAgendamento.CRIADO, TipoEventoAgendamento.CANCELADO, TipoEventoAgendamento.CRIADO);
    }

    @Test
    void naoAgendaParaBeneficiarioInativo() {
        maria.inativar();
        assertThatThrownBy(() -> agendar(maria, psicologa, PROXIMA_SEGUNDA.atTime(8, 0)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    void informaQuandoBeneficiarioNaoExiste() {
        assertThatThrownBy(() -> service.agendar(new SolicitacaoAgendamento(99L, psicologa.getId(),
                PROXIMA_SEGUNDA.atTime(8, 0), null)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void cicloDeVidaCompletoComPassagemDoTempoEGeracaoDeRelatorio() {
        Long realizado = agendar(maria, psicologa, AGORA.plusHours(3)).agendamento().getId();
        Long falta = agendar(jose, nutricionista, AGORA.plusHours(3)).agendamento().getId();
        service.confirmar(realizado);
        service.confirmar(falta);

        assertThatThrownBy(() -> service.registrarComparecimento(realizado))
                .isInstanceOf(RegraNegocioException.class);

        relogio.avancar(Duration.ofHours(4));
        service.registrarComparecimento(realizado);
        service.registrarFalta(falta);

        ResumoAtendimentos resumo = new RelatorioService(service).resumir(AGORA.toLocalDate(), AGORA.toLocalDate());
        assertThat(resumo.totalAgendamentos()).isEqualTo(2);
        assertThat(resumo.beneficiariosDistintosAtendidos()).isEqualTo(1);
        assertThat(resumo.porStatus()).containsEntry(StatusAgendamento.REALIZADO, 1L)
                .containsEntry(StatusAgendamento.FALTOU, 1L);
        assertThat(resumo.taxaComparecimentoGeral()).isEqualTo(50.0);
        assertThat(resumo.porEspecialidade()).hasSize(2);
    }

    @Test
    void horariosLivresDescontamOcupadosEPassados() {
        ProfissionalService profissionalService = new ProfissionalService(profissionais, agendamentos, relogio);
        agendar(maria, psicologa, PROXIMA_SEGUNDA.atTime(8, 50));

        assertThat(profissionalService.horariosLivres(psicologa.getId(), PROXIMA_SEGUNDA)).containsExactly(
                PROXIMA_SEGUNDA.atTime(8, 0), PROXIMA_SEGUNDA.atTime(9, 40), PROXIMA_SEGUNDA.atTime(10, 30));

        relogio.avancar(Duration.ofHours(2));
        assertThat(profissionalService.horariosLivres(psicologa.getId(), AGORA.toLocalDate()))
                .containsExactly(AGORA.toLocalDate().atTime(9, 40), AGORA.toLocalDate().atTime(10, 30));
    }

    private AgendamentoDetalhado agendar(Beneficiario b, Profissional p, LocalDateTime inicio) {
        return service.agendar(new SolicitacaoAgendamento(b.getId(), p.getId(), inicio, null));
    }

    private static Disponibilidade janela(DayOfWeek dia, int de, int ate) {
        return new Disponibilidade(dia, LocalTime.of(de, 0), LocalTime.of(ate, 0));
    }
}
