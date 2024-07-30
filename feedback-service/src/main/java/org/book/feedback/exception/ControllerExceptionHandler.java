    package org.book.feedback.exception;

    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.http.ProblemDetail;
    import org.springframework.http.ResponseEntity;
    import org.springframework.validation.ObjectError;
    import org.springframework.web.bind.annotation.ControllerAdvice;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.support.WebExchangeBindException;
    import reactor.core.publisher.Mono;

    @ControllerAdvice
    public class ControllerExceptionHandler {

        @ExceptionHandler(WebExchangeBindException.class)
        public Mono<ResponseEntity<ProblemDetail>> handleWebExchangeBindException(WebExchangeBindException ex) {
            ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            problemDetail.setProperty("errors", ex.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList()
            );

            return Mono.just(ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(problemDetail)
            );
        }
    }
