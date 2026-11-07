package br.edu.unicatolica.agendasolidaria.domain.exception;

/**
 * Indica que a operação conflita com o estado atual dos dados (ex.: CPF já cadastrado, horário ocupado).
 */
public class ConflitoException extends DomainException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
