package aromos.excepciones;

// Error que se lanza cuando alguien intenta hacer algo que no le corresponde.
public class NoAutorizadoException extends ReglaNegocioException {
    public NoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
