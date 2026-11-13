package aromos.ui;

import aromos.controladores.ControladorMantenimiento;
import aromos.controladores.ControladorReclamos;
import aromos.excepciones.ReglaNegocioException;
import aromos.mantenimiento.OrdenMantenimiento;
import aromos.personas.Barrio;
import aromos.personas.EmpleadoMantenimiento;
import aromos.reclamos.EstadoReclamo;
import aromos.reclamos.Reclamo;
import java.time.LocalDate;

// Menu de consola del empleado de mantenimiento.
public class MenuEmpleado extends MenuBase {
    private EmpleadoMantenimiento empleado;
    private ControladorReclamos reclamos;
    private ControladorMantenimiento mantenimiento;

    public MenuEmpleado(Barrio barrio, Consola consola, EmpleadoMantenimiento empleado) {
        super(barrio, consola);
        this.empleado = empleado;
        this.reclamos = new ControladorReclamos(barrio);
        this.mantenimiento = new ControladorMantenimiento(barrio);
    }

    @Override
    public String titulo() {
        return "EMPLEADO: " + empleado.getNombre() + " " + empleado.getApellido()
                + " (" + empleado.getEspecialidad() + ")";
    }

    // Devuelve las opciones del menu del empleado.
    @Override
    public String[] opciones() {
        return new String[] {
            "Ver mis reclamos asignados",
            "Actualizar el estado de un reclamo (EN_CURSO o RESUELTO)",
            "Comentar un reclamo",
            "Ver mis ordenes de mantenimiento",
            "Cerrar una orden registrando la reparacion"
        };
    }

    // Ejecuta la accion de la opcion elegida.
    @Override
    public void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1: verMisReclamos(); break;
            case 2: actualizarReclamo(); break;
            case 3: comentarReclamo(); break;
            case 4: verMisOrdenes(); break;
            case 5: cerrarOrden(); break;
            default: break;
        }
    }

    // Muestra los reclamos asignados al empleado.
    private void verMisReclamos() {
        consola.titulo("Mis reclamos asignados (" + barrio.contarReclamosActivosDe(empleado) + " activos de "
                + EmpleadoMantenimiento.MAX_RECLAMOS_EN_CURSO + " permitidos)");
        consola.listar("no tiene reclamos asignados", reclamos.consultarReclamosAsignadosA(empleado));
    }

    // Cambia el estado de un reclamo asignado.
    private void actualizarReclamo() {
        Reclamo reclamo = pedirReclamo();
        if (reclamo == null) {
            return;
        }
        EstadoReclamo[] posibles = {EstadoReclamo.EN_CURSO, EstadoReclamo.RESUELTO};
        EstadoReclamo nuevo = posibles[consola.elegir("Nuevo estado", posibles)];
        String observacion = consola.leerTexto("Observacion");
        try {
            reclamos.actualizarEstado(empleado, reclamo, nuevo, observacion);
            consola.ok("Reclamo #" + reclamo.getNumero() + " ahora esta " + reclamo.getEstado() + ".");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    // Agrega un comentario a un reclamo.
    private void comentarReclamo() {
        Reclamo reclamo = pedirReclamo();
        if (reclamo == null) {
            return;
        }
        String texto = consola.leerTexto("Comentario");
        try {
            reclamos.agregarComentario(empleado, reclamo, texto);
            consola.ok("Comentario agregado.");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    // Muestra las ordenes asignadas al empleado.
    private void verMisOrdenes() {
        consola.titulo("Mis ordenes de mantenimiento");
        consola.listar("no tiene ordenes asignadas", mantenimiento.consultarOrdenesDe(empleado));
    }

    // Cierra una orden registrando el trabajo hecho.
    private void cerrarOrden() {
        int numero = consola.leerEntero("Numero de orden");
        OrdenMantenimiento orden = barrio.buscarOrden(numero);
        if (orden == null) {
            consola.error("No existe la orden #" + numero + ".");
            return;
        }
        LocalDate fecha = consola.leerFecha("Fecha de la reparacion", barrio.hoy());
        String trabajo = consola.leerTexto("Descripcion del trabajo realizado");
        try {
            mantenimiento.cerrarOrden(numero, empleado, trabajo, fecha);
            consola.ok("Orden #" + numero + " cerrada. El equipo " + orden.getEquipo().getCodigo()
                    + " vuelve a estar " + orden.getEquipo().getEstado() + ".");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }
}
