package isi.dan.msclientes.enums;

public enum EstadoObra {
    HABILITADA("Habilitada"),
    PENDIENTE("Pendiente"),
    FINALIZADA("Finalizada");

    private final String descripcion;

    EstadoObra(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isActiva() {
        return this == HABILITADA;
    }

    public boolean isPendiente() {
        return this == PENDIENTE;
    }

    public boolean isFinalizada() {
        return this == FINALIZADA;
    }
}
