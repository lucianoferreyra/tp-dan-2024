package isi.dan.ms_productos.exception;

import java.time.Instant;

public record ErrorInfo(Instant fecha, String description, String detalle, Integer codigo) {
}    
