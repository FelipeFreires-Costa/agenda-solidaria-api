package br.edu.unicatolica.agendasolidaria.application.agendamento.regra;

/**
 * Padrão GoF <b>Strategy</b>: cada regra de validação é uma estratégia intercambiável.
 * Novas regras são adicionadas criando uma nova classe, sem alterar o caso de uso (OCP).
 */
public interface RegraAgendamento {

    /**
     * @throws br.edu.unicatolica.agendasolidaria.domain.exception.DomainException se a regra for violada
     */
    void validar(ContextoAgendamento contexto);
}
