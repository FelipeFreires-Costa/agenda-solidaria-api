package br.edu.unicatolica.agendasolidaria.domain.event;

/**
 * Padrão GoF <b>Observer</b>: interessados em mudanças de agendamento (notificação por WhatsApp/SMS,
 * auditoria, estatísticas) implementam esta interface sem que o caso de uso precise conhecê-los.
 */
public interface AgendamentoObserver {

    void aoOcorrer(AgendamentoEvento evento);
}
