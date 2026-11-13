package aromos.personas;

import aromos.excepciones.ReglaNegocioException;
import aromos.mantenimiento.OrdenMantenimiento;
import java.time.LocalDate;

// Personal que atiende reclamos y ordenes de mantenimiento.
public class EmpleadoMantenimiento extends Personal {
    public static final int MAX_RECLAMOS_EN_CURSO = 5;

    private String especialidad;

    public EmpleadoMantenimiento(String nombre, String apellido, String documento, String telefono,
                                 String correo, int legajo, LocalDate fechaIngreso, String especialidad) {
        super(nombre, apellido, documento, telefono, correo, legajo, fechaIngreso);
        this.especialidad = especialidad;
    }

    // Devuelve el nombre del rol, EMPLEADO.
    @Override
    public String obtenerRol() {
        return "Empleado de Mantenimiento (" + especialidad + ")";
    }

    // Indica si el empleado puede tomar otro reclamo sin pasar el limite de 5 activos.
    public boolean puedeTomarReclamo(int reclamosActivos) {
        return reclamosActivos < MAX_RECLAMOS_EN_CURSO;
    }

    // Cierra una orden registrando el trabajo realizado.
    public void registrarReparacion(OrdenMantenimiento orden, String descripcionTrabajo, LocalDate fecha)
            throws ReglaNegocioException {
        orden.cerrarOrden(this, descripcionTrabajo, fecha);
    }

    public String getEspecialidad() { return especialidad; }
}
