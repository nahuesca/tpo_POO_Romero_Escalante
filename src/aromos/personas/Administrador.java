package aromos.personas;

import aromos.excepciones.ReglaNegocioException;
import aromos.reclamos.Reclamo;
import java.time.LocalDate;

public class Administrador extends Personal {

    public Administrador(String nombre, String apellido, String documento, String telefono,
                         String correo, int legajo, LocalDate fechaIngreso) {
        super(nombre, apellido, documento, telefono, correo, legajo, fechaIngreso);
    }

    @Override
    public String obtenerRol() {
        return "Administrador";
    }

    // Asigna el reclamo a un empleado (el limite de 5 reclamos se controla en ControladorReclamos).
    public void asignarReclamo(Reclamo reclamo, EmpleadoMantenimiento empleado, int ahora)
            throws ReglaNegocioException {
        reclamo.asignarResponsable(empleado, ahora);
    }
}
