package br.edu.unicatolica.agendasolidaria.web.dto;

import br.edu.unicatolica.agendasolidaria.application.profissional.DadosProfissional;
import br.edu.unicatolica.agendasolidaria.domain.model.Disponibilidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;
import br.edu.unicatolica.agendasolidaria.domain.model.Profissional;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class ProfissionalDtos {

    private ProfissionalDtos() {
    }

    public record DisponibilidadeDto(
            @Schema(example = "MONDAY") @NotNull DayOfWeek diaSemana,
            @Schema(type = "string", example = "08:00") @NotNull LocalTime horaInicio,
            @Schema(type = "string", example = "12:00") @NotNull LocalTime horaFim) {

        public Disponibilidade paraDominio() {
            return new Disponibilidade(diaSemana, horaInicio, horaFim);
        }

        public static DisponibilidadeDto de(Disponibilidade d) {
            return new DisponibilidadeDto(d.diaSemana(), d.horaInicio(), d.horaFim());
        }
    }

    public record ProfissionalRequest(
            @Schema(example = "Ana Beatriz Lima") @NotBlank @Size(max = 120) String nome,
            @Schema(example = "PSICOLOGIA") @NotNull Especialidade especialidade,
            @Schema(example = "CRP 11/12345") @Size(max = 30) String registroProfissional,
            @Schema(example = "50") @NotNull @Min(10) @Max(180) Integer duracaoAtendimentoMinutos,
            @Valid List<DisponibilidadeDto> disponibilidades) {

        public DadosProfissional paraComando() {
            return new DadosProfissional(nome, especialidade, registroProfissional, duracaoAtendimentoMinutos,
                    paraDominio(disponibilidades));
        }
    }

    public record ProfissionalResponse(Long id, String nome, Especialidade especialidade,
                                       String especialidadeDescricao, String registroProfissional,
                                       int duracaoAtendimentoMinutos, List<DisponibilidadeDto> disponibilidades,
                                       boolean ativo) {

        public static ProfissionalResponse de(Profissional p) {
            return new ProfissionalResponse(p.getId(), p.getNome(), p.getEspecialidade(),
                    p.getEspecialidade().getDescricao(), p.getRegistroProfissional(),
                    p.getDuracaoAtendimentoMinutos(),
                    p.getDisponibilidades().stream().map(DisponibilidadeDto::de).toList(), p.isAtivo());
        }
    }

    public record HorariosLivresResponse(Long profissionalId, LocalDate data, int duracaoAtendimentoMinutos,
                                         @Schema(type = "array", example = "[\"08:00\", \"08:50\"]")
                                         List<LocalTime> horarios) {

        public static HorariosLivresResponse de(Profissional p, LocalDate data, List<LocalDateTime> horarios) {
            return new HorariosLivresResponse(p.getId(), data, p.getDuracaoAtendimentoMinutos(),
                    horarios.stream().map(LocalDateTime::toLocalTime).toList());
        }
    }

    public static List<Disponibilidade> paraDominio(List<DisponibilidadeDto> dtos) {
        return dtos == null ? List.of() : dtos.stream().map(DisponibilidadeDto::paraDominio).toList();
    }
}
