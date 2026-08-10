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
import com.inter.remessa.domain.exception.remessa.WalletNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
    ProblemDetail handleCotacaoUnavailable(CotacaoUnavailableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(WalletNotFoundException.class)
    ProblemDetail handleWalletNotFound(WalletNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}
