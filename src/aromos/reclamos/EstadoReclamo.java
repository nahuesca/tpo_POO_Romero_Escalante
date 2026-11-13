package aromos.reclamos;

// Los estados estan en orden: un reclamo solo puede avanzar al estado siguiente.
public enum EstadoReclamo {
    ABIERTO, ASIGNADO, EN_CURSO, RESUELTO, CERRADO;

    // Devuelve el estado que sigue, o null si ya es el ultimo.
    public EstadoReclamo siguiente() {
        EstadoReclamo[] todos = values();
        if (ordinal() + 1 < todos.length) {
            return todos[ordinal() + 1];
        }
        return null;
    }
}
