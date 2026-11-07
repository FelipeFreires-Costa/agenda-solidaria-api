package br.edu.unicatolica.agendasolidaria.application.agendamento;

import br.edu.unicatolica.agendasolidaria.domain.event.AgendamentoEvento;
import br.edu.unicatolica.agendasolidaria.domain.event.AgendamentoObserver;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Subject do padrão Observer. A falha de um observador (ex.: serviço de SMS fora do ar)
 * não pode desfazer o agendamento, por isso cada notificação é isolada.
 */
public class PublicadorEventosAgendamento {

    private static final Logger LOG = Logger.getLogger(PublicadorEventosAgendamento.class.getName());

    private final List<AgendamentoObserver> observadores;

    public PublicadorEventosAgendamento(List<AgendamentoObserver> observadores) {
        this.observadores = List.copyOf(observadores);
    }

    public void publicar(AgendamentoEvento evento) {
        for (AgendamentoObserver observador : observadores) {
            try {
                observador.aoOcorrer(evento);
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, "Falha no observador " + observador.getClass().getSimpleName(), e);
            }
        }
    }
}
