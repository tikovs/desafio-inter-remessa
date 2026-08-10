package com.inter.remessa.adapter.in.web;

import com.inter.remessa.domain.exception.cotacao.CotacaoUnavailableException;
import com.inter.remessa.domain.exception.pessoa.CnpjAlreadyRegisteredException;
import com.inter.remessa.domain.exception.pessoa.CpfAlreadyRegisteredException;
import com.inter.remessa.domain.exception.pessoa.EmailAlreadyRegisteredException;
import com.inter.remessa.domain.exception.pessoa.InvalidCnpjException;
import com.inter.remessa.domain.exception.pessoa.InvalidCpfException;
import com.inter.remessa.domain.exception.pessoa.InvalidEmailException;
import com.inter.remessa.domain.exception.remessa.LimiteExceededException;
import com.inter.remessa.domain.exception.remessa.SaldoInsufficientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler({
            EmailAlreadyRegisteredException.class,
            CpfAlreadyRegisteredException.class,
            CnpjAlreadyRegisteredException.class
    })
    ProblemDetail handleConflict(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({
            InvalidEmailException.class,
            InvalidCpfException.class,
            InvalidCnpjException.class
    })
    ProblemDetail handleBadRequest(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({SaldoInsufficientException.class, LimiteExceededException.class})
    ProblemDetail handleUnprocessable(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(CotacaoUnavailableException.class)
    ProblemDetail handleCotacaoIndisponivel(CotacaoUnavailableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }
}
