package br.edu.unicatolica.agendasolidaria.domain.model;

import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;

/**
 * Padrão GoF <b>State</b>: cada status decide quais transições são permitidas,
 * eliminando cadeias de if/else espalhadas pelo código.
 *
 * <pre>
 * AGENDADO ──confirmar──▶ CONFIRMADO ──registrarComparecimento──▶ REALIZADO
 *    │                        │  └──────registrarFalta──────────▶ FALTOU
 *    └────────cancelar────────┴──────────────────────────────────▶ CANCELADO
 * </pre>
 */
public enum StatusAgendamento {

    AGENDADO {
        @Override
        public StatusAgendamento confirmar() { return CONFIRMADO; }

        @Override
        public StatusAgendamento cancelar() { return CANCELADO; }
    },

    CONFIRMADO {
        @Override
        public StatusAgendamento cancelar() { return CANCELADO; }

        @Override
        public StatusAgendamento registrarComparecimento() { return REALIZADO; }

        @Override
        public StatusAgendamento registrarFalta() { return FALTOU; }
    },

    REALIZADO,
    FALTOU,
    CANCELADO;

    public StatusAgendamento confirmar() {
        throw transicaoInvalida("confirmar");
    }

    public StatusAgendamento cancelar() {
        throw transicaoInvalida("cancelar");
    }

    public StatusAgendamento registrarComparecimento() {
        throw transicaoInvalida("registrar comparecimento de");
    }

    public StatusAgendamento registrarFalta() {
        throw transicaoInvalida("registrar falta de");
    }

    /**
     * Status que ainda ocupam o horário na agenda do profissional.
     */
    public boolean ocupaHorario() {
        return this == AGENDADO || this == CONFIRMADO;
    }

    private RegraNegocioException transicaoInvalida(String acao) {
        return new RegraNegocioException("Não é possível " + acao + " um agendamento com status " + name());
    }
}
