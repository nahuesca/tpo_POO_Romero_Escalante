package aromos.reclamos;

// Tipos de reclamo, cada uno con su prioridad.
public enum TipoReclamo {
    SEGURIDAD(Prioridad.ALTA),
    MANTENIMIENTO(Prioridad.MEDIA),
    CONVIVENCIA(Prioridad.BAJA),
    ADMINISTRATIVO(Prioridad.BAJA);

    private Prioridad prioridad;

    TipoReclamo(Prioridad prioridad) {
        this.prioridad = prioridad;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }
}
