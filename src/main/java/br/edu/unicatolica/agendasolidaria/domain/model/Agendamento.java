package br.edu.unicatolica.agendasolidaria.domain.model;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Raiz de agregado que representa um atendimento marcado entre um beneficiário e um profissional.
 * Referencia os outros agregados apenas por identificador (DDD).
 */
public class Agendamento {

    private Long id;
    private final Long beneficiarioId;
    private final Long profissionalId;
    private final LocalDateTime inicio;
    private final LocalDateTime fim;
    private StatusAgendamento status;
    private final String observacao;
    private String motivoCancelamento;
    private final LocalDateTime criadoEm;

    private Agendamento(Long id, Long beneficiarioId, Long profissionalId, LocalDateTime inicio, LocalDateTime fim,
                        StatusAgendamento status, String observacao, String motivoCancelamento,
                        LocalDateTime criadoEm) {
        this.id = id;
        this.beneficiarioId = Objects.requireNonNull(beneficiarioId, "Beneficiário é obrigatório");
        this.profissionalId = Objects.requireNonNull(profissionalId, "Profissional é obrigatório");
        this.inicio = Objects.requireNonNull(inicio, "Data/hora de início é obrigatória");
        this.fim = Objects.requireNonNull(fim, "Data/hora de fim é obrigatória");
        if (!inicio.isBefore(fim)) {
            throw new RegraNegocioException("Início do agendamento deve ser anterior ao fim");
        }
        this.status = Objects.requireNonNull(status);
        this.observacao = observacao;
        this.motivoCancelamento = motivoCancelamento;
        this.criadoEm = criadoEm;
    }

    /**
     * Creator (GRASP) + Factory Method: o agendamento é criado a partir do profissional,
     * que detém a informação da duração do atendimento.
     */
    public static Agendamento novo(Beneficiario beneficiario, Profissional profissional, LocalDateTime inicio,
                                   String observacao, LocalDateTime agora) {
        LocalDateTime fim = inicio.plusMinutes(profissional.getDuracaoAtendimentoMinutos());
        return new Agendamento(null, beneficiario.getId(), profissional.getId(), inicio, fim,
                StatusAgendamento.AGENDADO, observacao, null, agora);
    }

    public static Agendamento reconstituir(Long id, Long beneficiarioId, Long profissionalId, LocalDateTime inicio,
                                           LocalDateTime fim, StatusAgendamento status, String observacao,
                                           String motivoCancelamento, LocalDateTime criadoEm) {
        return new Agendamento(id, beneficiarioId, profissionalId, inicio, fim, status, observacao,
                motivoCancelamento, criadoEm);
    }

    public boolean conflitaCom(LocalDateTime outroInicio, LocalDateTime outroFim) {
        return status.ocupaHorario() && inicio.isBefore(outroFim) && outroInicio.isBefore(fim);
    }

    public void confirmar() {
        status = status.confirmar();
    }

    public void cancelar(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("Informe o motivo do cancelamento");
        }
        status = status.cancelar();
        motivoCancelamento = motivo.trim();
    }

    public void registrarComparecimento(LocalDateTime agora) {
        exigirQueJaComecou(agora);
        status = status.registrarComparecimento();
    }

    public void registrarFalta(LocalDateTime agora) {
        exigirQueJaComecou(agora);
        status = status.registrarFalta();
    }

    private void exigirQueJaComecou(LocalDateTime agora) {
        if (agora.isBefore(inicio)) {
            throw new RegraNegocioException("Só é possível registrar comparecimento/falta após o horário de início");
        }
    }

    public void definirId(Long id) {
        this.id = id;
    }

    public Long getId() { return id; }
    public Long getBeneficiarioId() { return beneficiarioId; }
    public Long getProfissionalId() { return profissionalId; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFim() { return fim; }
    public StatusAgendamento getStatus() { return status; }
    public String getObservacao() { return observacao; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
