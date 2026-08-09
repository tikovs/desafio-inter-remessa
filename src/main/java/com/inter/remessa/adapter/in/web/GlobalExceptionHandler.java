package com.inter.remessa.adapter.in.web;

import com.inter.remessa.domain.exception.CnpjJaCadastradoException;
import com.inter.remessa.domain.exception.CpfJaCadastradoException;
import com.inter.remessa.domain.exception.EmailJaCadastradoException;
import com.inter.remessa.domain.exception.InvalidCnpjException;
import com.inter.remessa.domain.exception.InvalidCpfException;
import com.inter.remessa.domain.exception.InvalidEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler({
            EmailJaCadastradoException.class,
            CpfJaCadastradoException.class,
            CnpjJaCadastradoException.class
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
}
