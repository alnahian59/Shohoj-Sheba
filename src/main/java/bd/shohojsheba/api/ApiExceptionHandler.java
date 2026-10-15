package bd.shohojsheba.api;

import bd.shohojsheba.catalog.ServiceNotFoundException;
import bd.shohojsheba.dto.ApiError;
import bd.shohojsheba.request.InvalidRequestException;
import bd.shohojsheba.request.RequestNotFoundException;
import bd.shohojsheba.request.StaleRequestException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = {"bd.shohojsheba.api", "bd.shohojsheba.assistant"})
public class ApiExceptionHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            InvalidRequestException.class})
    public ResponseEntity<ApiError> badRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Please check the submitted fields.");
    }

    @ExceptionHandler({ServiceNotFoundException.class, RequestNotFoundException.class})
    public ResponseEntity<ApiError> notFound(Exception exception) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "The requested item was not found.");
    }

    @ExceptionHandler({StaleRequestException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<ApiError> conflict(Exception exception) {
        return error(HttpStatus.CONFLICT, "STALE_REQUEST", "This request changed. Reload it before updating.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "The request could not be completed.");
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore())
                .body(new ApiError(code, message));
    }
}
