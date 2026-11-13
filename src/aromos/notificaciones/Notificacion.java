package aromos.notificaciones;

import aromos.personas.Persona;
import java.io.Serializable;

// Aviso que se muestra dentro del sistema, sin mandarse por mail ni SMS.
public class Notificacion implements Serializable {
    private int hora;
    private Persona destinatario;
    private String mensaje;

    public Notificacion(int hora, Persona destinatario, String mensaje) {
        this.hora = hora;
        this.destinatario = destinatario;
        this.mensaje = mensaje;
    }

    public int getHora() { return hora; }
    public Persona getDestinatario() { return destinatario; }
    public String getMensaje() { return mensaje; }
}
