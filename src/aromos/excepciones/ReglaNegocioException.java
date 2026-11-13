package aromos.excepciones;

// Excepcion propia: se lanza cuando no se cumple una regla de negocio del barrio.
// Extiende Exception, por eso es "checked": hay que atraparla con try/catch o declararla con throws.
public class ReglaNegocioException extends Exception {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
