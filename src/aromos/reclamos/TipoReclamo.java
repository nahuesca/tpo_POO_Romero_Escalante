package aromos.reclamos;

// Cada tipo de reclamo ya trae su prioridad:
// seguridad es alta, mantenimiento es media, convivencia y administrativo son bajas.
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
