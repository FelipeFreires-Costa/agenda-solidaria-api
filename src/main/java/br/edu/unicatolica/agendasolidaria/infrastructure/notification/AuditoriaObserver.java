package br.edu.unicatolica.agendasolidaria.infrastructure.notification;

import br.edu.unicatolica.agendasolidaria.domain.event.AgendamentoEvento;
import br.edu.unicatolica.agendasolidaria.domain.event.AgendamentoObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registra a trilha de auditoria das mudanças de status dos agendamentos.
 */
@Component
public class AuditoriaObserver implements AgendamentoObserver {

    private static final Logger LOG = LoggerFactory.getLogger("AUDITORIA");

    @Override
    public void aoOcorrer(AgendamentoEvento evento) {
        LOG.info("agendamento={} evento={} status={} beneficiario={} profissional={} em={}",
                evento.agendamento().getId(), evento.tipo(), evento.agendamento().getStatus(),
                evento.beneficiario().getId(), evento.profissional().getId(), evento.ocorridoEm());
    }
}
