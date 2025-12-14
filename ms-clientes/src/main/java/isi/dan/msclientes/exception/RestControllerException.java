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

    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ErrorInfo> handleUsuarioNotFoundException(UsuarioNotFoundException ex) {
        logger.error("ERROR Buscando Usuario", ex);
        String detalle = ex.getCause() == null ? "Usuario no encontrado" : ex.getCause().getMessage();
        return new ResponseEntity<ErrorInfo>(new ErrorInfo(Instant.now(), ex.getMessage(), detalle, HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DniDuplicadoException.class)
    public ResponseEntity<ErrorInfo> handleDniDuplicadoException(DniDuplicadoException ex) {
        logger.error("ERROR DNI Duplicado", ex);
        String detalle = ex.getCause() == null ? "DNI duplicado" : ex.getCause().getMessage();
        return new ResponseEntity<ErrorInfo>(new ErrorInfo(Instant.now(), ex.getMessage(), detalle, HttpStatus.CONFLICT.value()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EmailUsuarioDuplicadoException.class)
    public ResponseEntity<ErrorInfo> handleEmailUsuarioDuplicadoException(EmailUsuarioDuplicadoException ex) {
        logger.error("ERROR Email de Usuario Duplicado", ex);
        String detalle = ex.getCause() == null ? "Email de usuario duplicado" : ex.getCause().getMessage();
        return new ResponseEntity<ErrorInfo>(new ErrorInfo(Instant.now(), ex.getMessage(), detalle, HttpStatus.CONFLICT.value()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> handleOtherExceptions(Exception ex) {
        logger.error("ERROR MS CLIENTES", ex);
        String detalle = ex.getCause() == null ? "error en el servidor" : ex.getCause().getMessage() ;
        return new ResponseEntity<ErrorInfo>(new ErrorInfo(Instant.now(),ex.getMessage(),detalle,HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
