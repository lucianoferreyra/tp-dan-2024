package isi.dan.msclientes.exception;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestControllerException {

    private static final Logger logger = LoggerFactory.getLogger(RestControllerException.class);

    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<ErrorInfo> handleProductNotFoundException(ClienteNotFoundException ex) {
        logger.error("ERROR Buscando Cliente", ex);
        String detalle = ex.getCause() == null ? "Cliente no encontrado" : ex.getCause().getMessage() ;

        return new ResponseEntity<ErrorInfo>(new ErrorInfo(Instant.now(),ex.getMessage(),detalle,HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> handleOtherExceptions(Exception ex) {
        logger.error("ERROR MS CLIENTES", ex);
        String detalle = ex.getCause() == null ? "error en el servidor" : ex.getCause().getMessage() ;
        return new ResponseEntity<ErrorInfo>(new ErrorInfo(Instant.now(),ex.getMessage(),detalle,HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
