package br.edu.unicatolica.agendasolidaria.domain.exception;

/**
 * Exceção base do domínio. Não depende de nenhum framework.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String mensagem) {
        super(mensagem);
    }
}
