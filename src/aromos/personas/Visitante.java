package aromos.personas;

public class Visitante extends Persona {
    private TipoVisitante tipo;

    public Visitante(String nombre, String apellido, String documento, TipoVisitante tipo) {
        // de un visitante solo se piden nombre, apellido y documento
        super(nombre, apellido, documento, "", "");
        this.tipo = tipo;
    }

    @Override
    public String obtenerRol() {
        return "Visitante (" + tipo + ")";
    }

    public TipoVisitante getTipo() { return tipo; }
}
