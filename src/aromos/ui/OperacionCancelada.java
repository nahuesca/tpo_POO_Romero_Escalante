package aromos.ui;

// Se lanza cuando la persona cierra o cancela un cuadro de dialogo en medio de una operacion.
// La ventana la atiende y simplemente avisa que la operacion se cancelo.
public class OperacionCancelada extends RuntimeException {

    public OperacionCancelada() {
        super("Operacion cancelada");
    }
}
