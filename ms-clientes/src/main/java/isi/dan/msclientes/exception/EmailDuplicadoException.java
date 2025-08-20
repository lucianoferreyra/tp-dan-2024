package isi.dan.msclientes.exception;

public class EmailDuplicadoException extends RuntimeException {

  public EmailDuplicadoException(String message) {
    super(message);
  }

  public EmailDuplicadoException(String email, Throwable cause) {
    super("El email ya está registrado: " + email, cause);
  }
}