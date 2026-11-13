package aromos.personas;

import aromos.excepciones.ReglaNegocioException;
import aromos.visitas.Autorizacion;
import aromos.visitas.RegistroAcceso;
import java.time.LocalDate;

// Personal de seguridad que controla el ingreso y egreso de visitas.
public class Guardia extends Personal {
    private String turno;
    private Puesto puesto;

    public Guardia(String nombre, String apellido, String documento, String telefono,
                   String correo, int legajo, LocalDate fechaIngreso, String turno, Puesto puesto) {
        super(nombre, apellido, documento, telefono, correo, legajo, fechaIngreso);
        this.turno = turno;
        this.puesto = puesto;
    }

    // Devuelve el nombre del rol, GUARDIA.
    @Override
    public String obtenerRol() {
        return "Guardia (" + puesto + ")";
    }

    // Indica si la autorizacion esta vigente para habilitar el ingreso.
    public boolean verificarAutorizacion(Autorizacion autorizacion, LocalDate hoy, int hora) {
        return autorizacion != null && autorizacion.estaVigente(hoy, hora);
    }

    // Registra la entrada de una persona al barrio.
    public RegistroAcceso registrarIngreso(Autorizacion autorizacion, LocalDate hoy, int hora)
            throws ReglaNegocioException {
        if (!verificarAutorizacion(autorizacion, hoy, hora)) {
            throw new ReglaNegocioException("No hay autorizacion vigente: no se habilita el ingreso");
        }
        return new RegistroAcceso(autorizacion, this, hoy, hora);
    }

    // Registra la salida de una persona del barrio.
    public void registrarEgreso(RegistroAcceso registro, int hora) throws ReglaNegocioException {
        registro.registrarEgreso(hora);
    }

    public String getTurno() { return turno; }
    public Puesto getPuesto() { return puesto; }
}
