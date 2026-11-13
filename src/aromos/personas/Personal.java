package aromos.personas;

import aromos.excepciones.NoAutorizadoException;
import aromos.excepciones.ReglaNegocioException;
import aromos.reclamos.EstadoReclamo;
import aromos.reclamos.Reclamo;
import java.time.LocalDate;

// Clase abstracta del personal del barrio, con legajo y fecha de ingreso.
public abstract class Personal extends Persona {
    private int legajo;
    private LocalDate fechaIngreso;

    protected Personal(String nombre, String apellido, String documento, String telefono,
                       String correo, int legajo, LocalDate fechaIngreso) {
        super(nombre, apellido, documento, telefono, correo);
        this.legajo = legajo;
        this.fechaIngreso = fechaIngreso;
    }

    // Devuelve el nombre del rol del personal.
    @Override
    public String obtenerRol() {
        return "Personal";
    }

    // Permite al responsable pasar un reclamo a EN_CURSO o RESUELTO.
    public void actualizarReclamo(Reclamo reclamo, EstadoReclamo nuevoEstado, String observacion, int ahora)
            throws ReglaNegocioException {
        if (!this.equals(reclamo.getResponsable())) {
            throw new NoAutorizadoException("Solo el responsable asignado puede actualizar el reclamo #"
                    + reclamo.getNumero());
        }
        if (nuevoEstado != EstadoReclamo.EN_CURSO && nuevoEstado != EstadoReclamo.RESUELTO) {
            throw new NoAutorizadoException("El personal solo puede pasar un reclamo a EN_CURSO o RESUELTO");
        }
        reclamo.cambiarEstado(nuevoEstado, observacion, ahora);
    }

    public int getLegajo() { return legajo; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
}
