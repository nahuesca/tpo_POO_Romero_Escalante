package aromos.ui;

import aromos.controladores.ControladorExpensas;
import aromos.controladores.ControladorMantenimiento;
import aromos.excepciones.ReglaNegocioException;
import aromos.expensas.Expensa;
import aromos.mantenimiento.Equipo;
import aromos.notificaciones.Notificacion;
import aromos.personas.Barrio;
import aromos.personas.Lote;
import aromos.personas.Persona;
import aromos.reclamos.CambioEstado;
import aromos.reclamos.Comentario;
import aromos.reclamos.Reclamo;
import java.time.LocalDate;
import java.util.List;

// Clase base abstracta de los menus de cada rol, con las operaciones comunes.
public abstract class MenuBase {
    protected Barrio barrio;
    protected Consola consola;

    protected MenuBase(Barrio barrio, Consola consola) {
        this.barrio = barrio;
        this.consola = consola;
    }

    public abstract String titulo();

    public abstract String[] opciones();

    public abstract void ejecutarOpcion(int opcion);

    // Repite el menu hasta que el usuario elige salir.
    public void ejecutar() {
        int opcion;
        do {
            opcion = consola.menu(titulo(), opciones());
            if (opcion != 0) {
                ejecutarOpcion(opcion);
            }
        } while (opcion != 0);
    }

    public void setConsola(Consola consola) {
        this.consola = consola;
    }

    // Pide un numero de lote y lo busca.
    protected Lote pedirLote() {
        int numero = consola.leerEntero("Numero de lote");
        Lote lote = barrio.buscarLote(numero);
        if (lote == null) {
            consola.error("No existe el lote " + numero + ".");
        }
        return lote;
    }

    // Pide un numero de reclamo y lo busca.
    protected Reclamo pedirReclamo() {
        int numero = consola.leerEntero("Numero de reclamo");
        Reclamo reclamo = barrio.buscarReclamo(numero);
        if (reclamo == null) {
            consola.error("No existe el reclamo #" + numero + ".");
        }
        return reclamo;
    }

    // Pide el codigo de un equipo y lo busca.
    protected Equipo pedirEquipo() {
        String codigo = consola.leerTexto("Codigo del equipo (ejemplo: CAM-01)");
        Equipo equipo = barrio.buscarEquipo(codigo);
        if (equipo == null) {
            consola.error("No existe el equipo " + codigo + ".");
        }
        return equipo;
    }

    // Muestra los datos, el historial y los comentarios de un reclamo.
    protected void mostrarDetalleReclamo(Reclamo reclamo) {
        consola.linea(reclamo.toString());
        consola.linea("  Presentado por " + reclamo.getAutor().getNombre() + " " + reclamo.getAutor().getApellido()
                + " el " + barrio.formatear(reclamo.getHoraCreacion()));
        consola.linea("  Historial de estados:");
        for (CambioEstado cambio : reclamo.consultarHistorial()) {
            consola.linea("    - " + barrio.formatear(cambio.getHora()) + " -> " + cambio.getNuevoEstado()
                    + " (" + cambio.getObservacion() + ")");
        }
        List<Comentario> comentarios = reclamo.consultarComentarios();
        if (comentarios.isEmpty()) {
            consola.linea("  Sin comentarios.");
        } else {
            consola.linea("  Comentarios:");
        }
        for (Comentario c : comentarios) {
            consola.linea("    - " + barrio.formatear(c.getHora()) + " " + c.getAutor().getNombre() + " "
                    + c.getAutor().getApellido() + " (" + c.getAutor().obtenerRol() + "): " + c.getTexto());
        }
    }

    // Muestra los avisos de una persona.
    protected void mostrarNotificaciones(Persona persona) {
        consola.titulo("Notificaciones");
        List<Notificacion> lista = barrio.consultarNotificaciones(persona);
        if (lista.isEmpty()) {
            consola.linea("(no hay notificaciones)");
        }
        for (Notificacion n : lista) {
            consola.linea(" - " + barrio.formatear(n.getHora()) + ": " + n.getMensaje());
        }
    }

    // Muestra todos los equipos con su estado.
    protected void mostrarEstadoEquipos() {
        consola.titulo("Estado de luminarias y camaras");
        consola.listar("no hay equipos registrados", barrio.consultarEquipos());
    }

    // Muestra las expensas pendientes de un lote con sus intereses.
    protected void mostrarDeudas(Lote lote) {
        LocalDate hoy = barrio.hoy();
        try {
            List<Expensa> pendientes = new ControladorExpensas(barrio).consultarDeudas(lote.getNumero());
            consola.titulo("Deudas del lote " + lote.getNumero() + " al " + hoy);
            if (lote.isMoroso()) {
                consola.linea("*** LOTE MOROSO ***");
            }
            if (pendientes.isEmpty()) {
                consola.linea("(no tiene deudas pendientes)");
            }
            double capital = 0;
            double intereses = 0;
            for (Expensa e : pendientes) {
                double interes = e.calcularInteresPendiente(hoy);
                consola.linea(" - " + e.getPeriodo() + " vence " + e.getVencimiento() + ": capital $"
                        + e.calcularSaldoCapital() + " + interes $" + interes + " (" + e.getEstado() + ")");
                capital = capital + e.calcularSaldoCapital();
                intereses = intereses + interes;
            }
            consola.linea("TOTAL: capital $" + Expensa.redondear(capital) + " + intereses $"
                    + Expensa.redondear(intereses) + " = $" + Expensa.redondear(capital + intereses));
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    // Permite a un vecino o guardia registrar la falla de un equipo.
    protected void reportarFallaDeEquipo() {
        Equipo equipo = pedirEquipo();
        if (equipo == null) {
            return;
        }
        try {
            new ControladorMantenimiento(barrio).reportarFalla(equipo.getCodigo());
            consola.ok("Falla registrada: " + equipo.getTipo() + " " + equipo.getCodigo() + " ahora esta "
                    + equipo.getEstado() + ". El administrador va a generar la orden de mantenimiento.");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }
}
