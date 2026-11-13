package aromos.excepciones;

// Se lanza cuando alguien intenta hacer algo que no le corresponde
// (por ejemplo reclamar sin ser titular del lote).
public class NoAutorizadoException extends ReglaNegocioException {

    public NoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
