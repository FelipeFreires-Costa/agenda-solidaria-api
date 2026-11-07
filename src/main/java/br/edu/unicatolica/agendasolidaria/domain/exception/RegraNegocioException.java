package br.edu.unicatolica.agendasolidaria.domain.exception;

/**
 * Violação de uma regra de negócio (ex.: conflito de horário, transição de status inválida).
 */
public class RegraNegocioException extends DomainException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
