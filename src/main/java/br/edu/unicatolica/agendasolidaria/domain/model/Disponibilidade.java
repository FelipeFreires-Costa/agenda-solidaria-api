package br.edu.unicatolica.agendasolidaria.domain.model;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Value Object: janela semanal em que o profissional voluntário atende (ex.: terça, 08:00–12:00).
 */
public record Disponibilidade(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim) {

    public Disponibilidade {
        if (diaSemana == null || horaInicio == null || horaFim == null) {
            throw new RegraNegocioException("Dia da semana, hora de início e hora de fim são obrigatórios");
        }
        if (!horaInicio.isBefore(horaFim)) {
            throw new RegraNegocioException("Hora de início deve ser anterior à hora de fim");
        }
    }

    /**
     * Information Expert (GRASP): a própria disponibilidade sabe dizer se um intervalo cabe nela.
     */
    public boolean abrange(LocalDateTime inicio, LocalDateTime fim) {
        return inicio.getDayOfWeek() == diaSemana
                && inicio.toLocalDate().equals(fim.toLocalDate())
                && !inicio.toLocalTime().isBefore(horaInicio)
                && !fim.toLocalTime().isAfter(horaFim);
    }

    public boolean sobrepoe(Disponibilidade outra) {
        return diaSemana == outra.diaSemana
                && horaInicio.isBefore(outra.horaFim)
                && outra.horaInicio.isBefore(horaFim);
    }
}
