package aromos.mantenimiento;

import aromos.excepciones.NoAutorizadoException;
import aromos.excepciones.ReglaNegocioException;
import aromos.personas.EmpleadoMantenimiento;
import java.io.Serializable;
import java.time.LocalDate;

public class OrdenMantenimiento implements Serializable {
    private int numero;
    private Equipo equipo;
    private int horaApertura;
    private PrioridadOrden prioridad;
    private String descripcion;
    private EmpleadoMantenimiento tecnico;
    private boolean cerrada;
    private LocalDate fechaCierre;
    private String descripcionReparacion;

    public OrdenMantenimiento(int numero, Equipo equipo, PrioridadOrden prioridad, String descripcion,
                              int horaApertura) {
        this.numero = numero;
        this.equipo = equipo;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
        this.horaApertura = horaApertura;
    }

    // Al asignar un empleado el equipo pasa a EN_REPARACION.
    public void asignarEmpleado(EmpleadoMantenimiento empleado, int ahora) throws ReglaNegocioException {
        if (cerrada) {
            throw new ReglaNegocioException("La orden #" + numero + " ya esta cerrada");
        }
        tecnico = empleado;
        equipo.cambiarEstado(EstadoEquipo.EN_REPARACION, ahora);
    }

    // Regla de negocio: una orden no se puede cerrar sin registrar la fecha, el tecnico y la descripcion.
    // Al cerrarla, el equipo vuelve a estar OPERATIVO.
    public void cerrarOrden(EmpleadoMantenimiento quienCierra, String descripcionTrabajo, LocalDate fecha)
            throws ReglaNegocioException {
        if (cerrada) {
            throw new ReglaNegocioException("La orden #" + numero + " ya esta cerrada");
        }
        if (fecha == null) {
            throw new ReglaNegocioException("Falta la fecha de la reparacion");
        }
        if (quienCierra == null) {
            throw new ReglaNegocioException("Falta el tecnico responsable");
        }
        if (descripcionTrabajo == null || descripcionTrabajo.trim().isEmpty()) {
            throw new ReglaNegocioException("Falta la descripcion del trabajo realizado");
        }
        if (tecnico != null && !tecnico.equals(quienCierra)) {
            throw new NoAutorizadoException("La orden #" + numero + " esta asignada a otro empleado");
        }
        tecnico = quienCierra;
        fechaCierre = fecha;
        descripcionReparacion = descripcionTrabajo.trim();
        cerrada = true;
        equipo.cambiarEstado(EstadoEquipo.OPERATIVO, 0);
    }

    public boolean estaCerrada() { return cerrada; }
    public int getNumero() { return numero; }
    public Equipo getEquipo() { return equipo; }
    public int getHoraApertura() { return horaApertura; }
    public PrioridadOrden getPrioridad() { return prioridad; }
    public String getDescripcion() { return descripcion; }
    public EmpleadoMantenimiento getTecnico() { return tecnico; }
    public LocalDate getFechaCierre() { return fechaCierre; }
    public String getDescripcionReparacion() { return descripcionReparacion; }

    @Override
    public String toString() {
        String estado = "ABIERTA";
        if (cerrada) {
            estado = "CERRADA";
        } else if (tecnico != null) {
            estado = "ASIGNADA";
        }
        String resp = "sin asignar";
        if (tecnico != null) {
            resp = tecnico.getNombre() + " " + tecnico.getApellido();
        }
        return "Orden #" + numero + " [" + estado + "] prioridad " + prioridad + " - " + equipo.getTipo() + " "
                + equipo.getCodigo() + " - " + descripcion + " - tecnico: " + resp;
    }
}
