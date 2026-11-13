package aromos.excepciones;

// Se lanza cuando se supera un limite (por ejemplo, mas de 5 reclamos en curso para un empleado).
public class LimiteExcedidoException extends ReglaNegocioException {

    public LimiteExcedidoException(String mensaje) {
        super(mensaje);
    }
}
