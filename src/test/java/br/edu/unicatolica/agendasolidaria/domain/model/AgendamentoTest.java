package br.edu.unicatolica.agendasolidaria.domain.model;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgendamentoTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 16, 8, 0);
    private static final LocalDateTime ANTES = INICIO.minusDays(3);
    private static final LocalDateTime DEPOIS = INICIO.plusHours(1);

    private Agendamento agendamento;

    @BeforeEach
    void setUp() {
        Beneficiario beneficiario = Beneficiario.novo("Maria", new Cpf("52998224725"),
                LocalDate.of(1970, 1, 1), "(88) 99999-0000", null);
        beneficiario.definirId(1L);
        Profissional profissional = Profissional.novo("Ana", Especialidade.PSICOLOGIA, null, 50,
                List.of(new Disponibilidade(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
        profissional.definirId(2L);
        agendamento = Agendamento.novo(beneficiario, profissional, INICIO, "obs", ANTES);
    }

    @Test
    void novoAgendamentoUsaDuracaoDoProfissionalEComecaAgendado() {
        assertThat(agendamento.getFim()).isEqualTo(INICIO.plusMinutes(50));
        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(agendamento.getBeneficiarioId()).isEqualTo(1L);
        assertThat(agendamento.getProfissionalId()).isEqualTo(2L);
    }

    @Test
    void fluxoCompletoAteRealizado() {
        agendamento.confirmar();
        agendamento.registrarComparecimento(DEPOIS);
        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.REALIZADO);
    }

    @Test
    void registraFaltaAposConfirmacao() {
        agendamento.confirmar();
        agendamento.registrarFalta(DEPOIS);
        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.FALTOU);
    }

    @Test
    void naoRegistraComparecimentoSemConfirmar() {
        assertThatThrownBy(() -> agendamento.registrarComparecimento(DEPOIS))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("AGENDADO");
    }

    @Test
    void naoRegistraComparecimentoAntesDoHorario() {
        agendamento.confirmar();
        assertThatThrownBy(() -> agendamento.registrarComparecimento(INICIO.minusMinutes(1)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void cancelamentoExigeMotivoELiberaHorario() {
        assertThatThrownBy(() -> agendamento.cancelar(" ")).isInstanceOf(RegraNegocioException.class);

        assertThat(agendamento.conflitaCom(INICIO.plusMinutes(10), INICIO.plusMinutes(60))).isTrue();
        agendamento.cancelar("Viagem");
        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
        assertThat(agendamento.getMotivoCancelamento()).isEqualTo("Viagem");
        assertThat(agendamento.conflitaCom(INICIO.plusMinutes(10), INICIO.plusMinutes(60))).isFalse();
    }

    @Test
    void estadosFinaisNaoAceitamNovasTransicoes() {
        agendamento.cancelar("Desistência");
        assertThatThrownBy(agendamento::confirmar).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> agendamento.cancelar("de novo")).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void horariosEncostadosNaoConflitam() {
        assertThat(agendamento.conflitaCom(INICIO.plusMinutes(50), INICIO.plusMinutes(100))).isFalse();
        assertThat(agendamento.conflitaCom(INICIO.minusMinutes(50), INICIO)).isFalse();
    }
}
