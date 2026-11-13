package aromos.personas;

import aromos.excepciones.ReglaNegocioException;
import aromos.reclamos.Reclamo;
import java.time.LocalDate;

// Personal que gestiona el barrio, asigna reclamos y emite expensas.
public class Administrador extends Personal {
    public Administrador(String nombre, String apellido, String documento, String telefono,
                         String correo, int legajo, LocalDate fechaIngreso) {
        super(nombre, apellido, documento, telefono, correo, legajo, fechaIngreso);
    }

    // Devuelve el nombre del rol, ADMINISTRADOR.
    @Override
    public String obtenerRol() {
        return "Administrador";
    }

    // Asigna un reclamo a un empleado de mantenimiento.
    public void asignarReclamo(Reclamo reclamo, EmpleadoMantenimiento empleado, int ahora)
            throws ReglaNegocioException {
        reclamo.asignarResponsable(empleado, ahora);
    }
}
