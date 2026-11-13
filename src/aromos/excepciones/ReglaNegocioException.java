package aromos.excepciones;

// Error base para todas las reglas del negocio que no se cumplen.
public class ReglaNegocioException extends Exception {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
