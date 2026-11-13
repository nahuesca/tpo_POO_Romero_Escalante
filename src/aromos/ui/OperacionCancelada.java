package aromos.ui;

// Se lanza cuando el usuario cancela una operacion en un cuadro de dialogo.
public class OperacionCancelada extends RuntimeException {
    public OperacionCancelada() {
        super("Operacion cancelada");
    }
}
