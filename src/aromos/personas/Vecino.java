package aromos.personas;

// Persona duena o residente de un lote del barrio.
public class Vecino extends Persona {
    private boolean autorizadoReclamos;

    public Vecino(String nombre, String apellido, String documento, String telefono,
                  String correo, boolean autorizadoReclamos) {
        super(nombre, apellido, documento, telefono, correo);
        this.autorizadoReclamos = autorizadoReclamos;
    }

    // Devuelve el nombre del rol, VECINO.
    @Override
    public String obtenerRol() {
        return "Vecino";
    }

    public boolean isAutorizadoReclamos() { return autorizadoReclamos; }

    public void setAutorizadoReclamos(boolean autorizadoReclamos) {
        this.autorizadoReclamos = autorizadoReclamos;
    }
}
