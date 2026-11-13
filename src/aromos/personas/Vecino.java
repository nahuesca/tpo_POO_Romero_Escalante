package aromos.personas;

public class Vecino extends Persona {
    // true si el titular del lote lo autorizo a presentar reclamos (se usa para familiares e inquilinos)
    private boolean autorizadoReclamos;

    public Vecino(String nombre, String apellido, String documento, String telefono,
                  String correo, boolean autorizadoReclamos) {
        super(nombre, apellido, documento, telefono, correo);
        this.autorizadoReclamos = autorizadoReclamos;
    }

    @Override
    public String obtenerRol() {
        return "Vecino";
    }

    public boolean isAutorizadoReclamos() { return autorizadoReclamos; }

    public void setAutorizadoReclamos(boolean autorizadoReclamos) {
        this.autorizadoReclamos = autorizadoReclamos;
    }
}
