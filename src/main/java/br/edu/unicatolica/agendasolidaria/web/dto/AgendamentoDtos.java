package br.edu.unicatolica.agendasolidaria.web.dto;

import br.edu.unicatolica.agendasolidaria.application.agendamento.AgendamentoDetalhado;
import br.edu.unicatolica.agendasolidaria.application.agendamento.SolicitacaoAgendamento;
import br.edu.unicatolica.agendasolidaria.domain.model.Agendamento;
import br.edu.unicatolica.agendasolidaria.domain.model.Especialidade;
import br.edu.unicatolica.agendasolidaria.domain.model.StatusAgendamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class AgendamentoDtos {

    private AgendamentoDtos() {
    }

    public record AgendamentoRequest(
            @Schema(example = "1") @NotNull Long beneficiarioId,
            @Schema(example = "1") @NotNull Long profissionalId,
            @Schema(example = "2026-11-16T09:40:00") @NotNull LocalDateTime inicio,
            @Schema(example = "Primeira consulta") @Size(max = 500) String observacao) {

        public SolicitacaoAgendamento paraComando() {
            return new SolicitacaoAgendamento(beneficiarioId, profissionalId, inicio, observacao);
        }
    }

    public record CancelamentoRequest(
            @Schema(example = "Beneficiário informou que estará viajando") @NotBlank @Size(max = 500) String motivo) {
    }

    public record ResumoBeneficiario(Long id, String nome, String telefone) {
    }

    public record ResumoProfissional(Long id, String nome, Especialidade especialidade) {
    }

    public record AgendamentoResponse(Long id, StatusAgendamento status, LocalDateTime inicio, LocalDateTime fim,
                                      ResumoBeneficiario beneficiario, ResumoProfissional profissional,
                                      String observacao, String motivoCancelamento, LocalDateTime criadoEm) {

        public static AgendamentoResponse de(AgendamentoDetalhado d) {
            Agendamento a = d.agendamento();
            return new AgendamentoResponse(a.getId(), a.getStatus(), a.getInicio(), a.getFim(),
                    new ResumoBeneficiario(d.beneficiario().getId(), d.beneficiario().getNome(),
                            d.beneficiario().getTelefone()),
                    new ResumoProfissional(d.profissional().getId(), d.profissional().getNome(),
                            d.profissional().getEspecialidade()),
                    a.getObservacao(), a.getMotivoCancelamento(), a.getCriadoEm());
        }
    }
}
