package br.edu.unicatolica.agendasolidaria.domain.model;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfissionalTest {

    private static final LocalDate SEGUNDA = LocalDate.of(2026, 11, 16);

    private static Disponibilidade janela(DayOfWeek dia, int de, int ate) {
        return new Disponibilidade(dia, LocalTime.of(de, 0), LocalTime.of(ate, 0));
    }

    @Test
    void geraHorariosPossiveisConformeDuracaoDoAtendimento() {
        Profissional psicologa = Profissional.novo("Ana", Especialidade.PSICOLOGIA, "CRP", 50,
                List.of(janela(DayOfWeek.MONDAY, 8, 12)));

        assertThat(psicologa.horariosPossiveis(SEGUNDA)).containsExactly(
                SEGUNDA.atTime(8, 0), SEGUNDA.atTime(8, 50), SEGUNDA.atTime(9, 40), SEGUNDA.atTime(10, 30));
        assertThat(psicologa.horariosPossiveis(SEGUNDA.plusDays(1))).isEmpty();
    }

    @Test
    void sabeSeAtendeEmUmIntervalo() {
        Profissional p = Profissional.novo("Ana", Especialidade.PSICOLOGIA, null, 50,
                List.of(janela(DayOfWeek.MONDAY, 8, 12)));

        assertThat(p.atendeNoIntervalo(SEGUNDA.atTime(11, 10), SEGUNDA.atTime(12, 0))).isTrue();
        assertThat(p.atendeNoIntervalo(SEGUNDA.atTime(11, 30), SEGUNDA.atTime(12, 20))).isFalse();
        assertThat(p.atendeNoIntervalo(SEGUNDA.atTime(7, 30), SEGUNDA.atTime(8, 20))).isFalse();
        assertThat(p.atendeNoIntervalo(SEGUNDA.plusDays(1).atTime(8, 0), SEGUNDA.plusDays(1).atTime(8, 50))).isFalse();
    }

    @Test
    void naoPermiteJanelasSobrepostasNoMesmoDia() {
        assertThatThrownBy(() -> Profissional.novo("Ana", Especialidade.PSICOLOGIA, null, 50,
                List.of(janela(DayOfWeek.MONDAY, 8, 12), janela(DayOfWeek.MONDAY, 11, 14))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("sobrepostas");
    }

    @Test
    void validaDuracaoDoAtendimento() {
        assertThatThrownBy(() -> Profissional.novo("Ana", Especialidade.PSICOLOGIA, null, 5, List.of()))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void disponibilidadeExigeInicioAntesDoFim() {
        assertThatThrownBy(() -> new Disponibilidade(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(8, 0)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void disponibilidadeNaoAbrangeIntervaloQueVaiParaOutroDia() {
        Disponibilidade d = new Disponibilidade(DayOfWeek.MONDAY, LocalTime.of(20, 0), LocalTime.of(23, 59));
        assertThat(d.abrange(LocalDateTime.of(2026, 11, 16, 23, 30), LocalDateTime.of(2026, 11, 17, 0, 20)))
                .isFalse();
    }
}
