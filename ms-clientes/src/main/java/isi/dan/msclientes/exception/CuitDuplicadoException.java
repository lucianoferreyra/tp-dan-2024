package isi.dan.msclientes.exception;

public class CuitDuplicadoException extends RuntimeException {

  public CuitDuplicadoException(String message) {
    super(message);
  }

  public CuitDuplicadoException(String cuit, Throwable cause) {
    super("El CUIT ya está registrado: " + cuit, cause);
  }
}