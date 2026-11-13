package aromos.reclamos;

import aromos.personas.Persona;
import java.io.Serializable;

// Comentario que una persona deja en un reclamo.
public class Comentario implements Serializable {
    private int hora;
    private Persona autor;
    private String texto;

    public Comentario(int hora, Persona autor, String texto) {
        this.hora = hora;
        this.autor = autor;
        this.texto = texto;
    }

    public int getHora() { return hora; }
    public Persona getAutor() { return autor; }
    public String getTexto() { return texto; }
}
