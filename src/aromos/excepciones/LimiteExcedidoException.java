package aromos.excepciones;

// Error que se lanza cuando se supera un limite, como los 5 reclamos activos de un empleado.
public class LimiteExcedidoException extends ReglaNegocioException {
    public LimiteExcedidoException(String mensaje) {
        super(mensaje);
    }
}
