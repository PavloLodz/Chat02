package pl.ldz.chat.exception;

import jakarta.persistence.OptimisticLockException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setTitle("Entity Not Found");
    return problem;
  }

  @ExceptionHandler(OptimisticLockException.class)
  public ProblemDetail handleOptimisticLock(OptimisticLockException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
      HttpStatus.CONFLICT,
      "The resource was modified by another request. Please retry."
    );
    problem.setTitle("Optimistic Lock Conflict");
    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
      .collect(Collectors.toMap(
        FieldError::getField,
        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
        (a, b) -> a
      ));
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
      HttpStatus.UNPROCESSABLE_ENTITY,
      "Validation failed"
    );
    problem.setTitle("Validation Error");
    problem.setProperty("errors", errors);
    return problem;
  }

  @ExceptionHandler({SignatureException.class, MalformedJwtException.class})
  public ProblemDetail handleInvalidToken() {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.UNAUTHORIZED,
        "Invalid JWT token"
    );
    problem.setTitle("Unauthorized");
    return problem;
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ProblemDetail handleExpiredToken() {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.UNAUTHORIZED,
        "JWT token has expired"
    );
    problem.setTitle("Unauthorized");
    return problem;
  }
}
