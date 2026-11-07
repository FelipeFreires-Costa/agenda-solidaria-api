package br.edu.unicatolica.agendasolidaria.infrastructure.notification;

import br.edu.unicatolica.agendasolidaria.domain.event.AgendamentoEvento;
import br.edu.unicatolica.agendasolidaria.domain.event.AgendamentoObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Simula o envio de mensagem (WhatsApp/SMS) ao beneficiário. Em produção, bastaria criar outra
 * implementação de {@link AgendamentoObserver} integrada a um provedor real, sem alterar o caso de uso.
 */
@Component
public class NotificacaoMensagemObserver implements AgendamentoObserver {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacaoMensagemObserver.class);
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    @Override
    public void aoOcorrer(AgendamentoEvento evento) {
        String quando = evento.agendamento().getInicio().format(FORMATO);
        String com = evento.profissional().getNome() + " (" + evento.profissional().getEspecialidade().getDescricao() + ")";
        String texto = switch (evento.tipo()) {
            case CRIADO -> "Seu atendimento com " + com + " foi agendado para " + quando + ".";
            case CONFIRMADO -> "Seu atendimento de " + quando + " com " + com + " está confirmado.";
            case CANCELADO -> "Seu atendimento de " + quando + " foi cancelado. Motivo: "
                    + evento.agendamento().getMotivoCancelamento();
            case REALIZADO -> "Obrigado por comparecer ao atendimento de " + quando + "!";
            case FALTA_REGISTRADA -> "Sentimos sua falta no atendimento de " + quando
                    + ". Procure a associação para remarcar.";
        };
        LOG.info("[MENSAGEM para {} - {}] Olá, {}! {}", evento.beneficiario().getTelefone(), evento.tipo(),
                evento.beneficiario().getNome().split(" ")[0], texto);
    }
}
