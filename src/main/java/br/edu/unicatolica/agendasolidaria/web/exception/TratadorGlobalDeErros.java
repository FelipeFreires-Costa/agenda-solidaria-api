package br.edu.unicatolica.agendasolidaria.web.exception;

import br.edu.unicatolica.agendasolidaria.domain.exception.ConflitoException;
import br.edu.unicatolica.agendasolidaria.domain.exception.RecursoNaoEncontradoException;
import br.edu.unicatolica.agendasolidaria.domain.exception.RegraNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte exceções em respostas padronizadas no formato Problem Details (RFC 9457).
 */
@RestControllerAdvice
public class TratadorGlobalDeErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorGlobalDeErros.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", e.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ProblemDetail conflito(ConflitoException e) {
        return problema(HttpStatus.CONFLICT, "Conflito", e.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail regraNegocio(RegraNegocioException e) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos estão inválidos");
        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail validacaoParametros(HandlerMethodValidationException e) {
        return problema(HttpStatus.BAD_REQUEST, "Dados inválidos", "Um ou mais itens da requisição estão inválidos");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ProblemDetail requisicaoMalformada(Exception e) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Verifique o formato dos dados enviados (datas em ISO-8601, ex.: 2026-11-16T09:00:00) "
                        + "e os parâmetros obrigatórios");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception e) {
        if (e instanceof ErrorResponse erroDoSpring) {
            return erroDoSpring.getBody();
        }
        LOG.error("Erro inesperado", e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado");
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
