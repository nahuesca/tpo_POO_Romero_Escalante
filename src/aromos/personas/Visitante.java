package aromos.personas;

// Persona externa que ingresa al barrio con una autorizacion.
public class Visitante extends Persona {
    private TipoVisitante tipo;

    public Visitante(String nombre, String apellido, String documento, TipoVisitante tipo) {
        super(nombre, apellido, documento, "", "");
        this.tipo = tipo;
    }

    // Devuelve el nombre del rol, VISITANTE.
    @Override
    public String obtenerRol() {
        return "Visitante (" + tipo + ")";
    }

    public TipoVisitante getTipo() { return tipo; }
}
