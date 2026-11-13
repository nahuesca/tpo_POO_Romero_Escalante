package aromos.personas;

import aromos.excepciones.ReglaNegocioException;
import aromos.mantenimiento.OrdenMantenimiento;
import java.time.LocalDate;

public class EmpleadoMantenimiento extends Personal {
    // Maximo de reclamos que puede tener a la vez
    public static final int MAX_RECLAMOS_EN_CURSO = 5;

    private String especialidad;

    public EmpleadoMantenimiento(String nombre, String apellido, String documento, String telefono,
                                 String correo, int legajo, LocalDate fechaIngreso, String especialidad) {
        super(nombre, apellido, documento, telefono, correo, legajo, fechaIngreso);
        this.especialidad = especialidad;
    }

    @Override
    public String obtenerRol() {
        return "Empleado de Mantenimiento (" + especialidad + ")";
    }

    // El empleado sabe cuantos reclamos puede tomar.
    public boolean puedeTomarReclamo(int reclamosActivos) {
        return reclamosActivos < MAX_RECLAMOS_EN_CURSO;
    }

    public void registrarReparacion(OrdenMantenimiento orden, String descripcionTrabajo, LocalDate fecha)
            throws ReglaNegocioException {
        orden.cerrarOrden(this, descripcionTrabajo, fecha);
    }

    public String getEspecialidad() { return especialidad; }
}
