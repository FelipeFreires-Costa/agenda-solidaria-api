package br.edu.unicatolica.agendasolidaria.domain.exception;

public class RecursoNaoEncontradoException extends DomainException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super(recurso + " com id " + id + " não encontrado(a)");
    }
}
