package aromos.controladores;

import aromos.excepciones.LimiteExcedidoException;
import aromos.excepciones.NoAutorizadoException;
import aromos.excepciones.ReglaNegocioException;
import aromos.personas.Administrador;
import aromos.personas.Barrio;
import aromos.personas.EmpleadoMantenimiento;
import aromos.personas.Lote;
import aromos.personas.Persona;
import aromos.personas.Vecino;
import aromos.reclamos.EstadoReclamo;
import aromos.reclamos.Reclamo;
import aromos.reclamos.TipoReclamo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Controlador de la presentacion y gestion de solicitudes y reclamos.
public class ControladorReclamos {
    private Barrio barrio;

    public ControladorReclamos(Barrio barrio) {
        this.barrio = barrio;
    }

    // Crea un reclamo si el vecino es titular o esta autorizado en el lote; si no, lanza un error.
    public Reclamo presentarReclamo(Vecino vecino, Lote lote, TipoReclamo tipo, String descripcion)
            throws ReglaNegocioException {
        if (lote == null) {
            throw new ReglaNegocioException("El lote indicado no existe");
        }
        if (!lote.esTitularOAutorizado(vecino)) {
            throw new NoAutorizadoException("Solo el titular del lote " + lote.getNumero()
                    + " o un residente autorizado por el titular puede presentar reclamos");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ReglaNegocioException("El reclamo necesita una descripcion");
        }
        return lote.crearReclamo(vecino, tipo, descripcion.trim(), barrio.getAhora());
    }

    // El administrador asigna un reclamo a un empleado que no supere los 5 reclamos activos.
    public void asignarReclamo(Administrador administrador, Reclamo reclamo, EmpleadoMantenimiento empleado)
            throws ReglaNegocioException {
        if (!empleado.puedeTomarReclamo(barrio.contarReclamosActivosDe(empleado))) {
            throw new LimiteExcedidoException(empleado.getNombre() + " " + empleado.getApellido() + " ya tiene "
                    + EmpleadoMantenimiento.MAX_RECLAMOS_EN_CURSO + " reclamos en curso");
        }
        administrador.asignarReclamo(reclamo, empleado, barrio.getAhora());
    }

    // El empleado responsable pasa el reclamo a EN_CURSO o RESUELTO.
    public void actualizarEstado(EmpleadoMantenimiento empleado, Reclamo reclamo, EstadoReclamo nuevoEstado,
                                 String observacion) throws ReglaNegocioException {
        empleado.actualizarReclamo(reclamo, nuevoEstado, observacion, barrio.getAhora());
    }

    // El administrador cierra un reclamo que ya esta resuelto.
    public void cerrarReclamo(Reclamo reclamo, String observacion) throws ReglaNegocioException {
        reclamo.cambiarEstado(EstadoReclamo.CERRADO, observacion, barrio.getAhora());
    }

    // Reabre un reclamo, solo si lo pide quien lo presento o el titular del lote.
    public void reabrirReclamo(Vecino vecino, Reclamo reclamo) throws ReglaNegocioException {
        boolean esAutor = reclamo.getAutor().equals(vecino);
        boolean esTitular = reclamo.getLote().consultarTitular().equals(vecino);
        if (!esAutor && !esTitular) {
            throw new NoAutorizadoException("Solo quien presento el reclamo o el titular puede reabrirlo");
        }
        reclamo.reabrir(barrio.getAhora());
    }

    // Agrega un comentario de un usuario a un reclamo.
    public void agregarComentario(Persona autor, Reclamo reclamo, String texto) throws ReglaNegocioException {
        if (texto == null || texto.trim().isEmpty()) {
            throw new ReglaNegocioException("El comentario no puede estar vacio");
        }
        reclamo.agregarComentario(autor, texto.trim(), barrio.getAhora());
    }

    // Devuelve los reclamos que presento un vecino o que pertenecen a lotes de los que es titular.
    public List<Reclamo> consultarReclamosDe(Vecino vecino) {
        List<Reclamo> resultado = new ArrayList<>();
        for (Reclamo r : barrio.consultarTodosLosReclamos()) {
            if (r.getAutor().equals(vecino) || r.getLote().consultarTitular().equals(vecino)) {
                resultado.add(r);
            }
        }
        Collections.sort(resultado);
        return resultado;
    }

    // Devuelve los reclamos asignados a un empleado.
    public List<Reclamo> consultarReclamosAsignadosA(EmpleadoMantenimiento empleado) {
        List<Reclamo> resultado = new ArrayList<>();
        for (Reclamo r : barrio.consultarTodosLosReclamos()) {
            if (empleado.equals(r.getResponsable())) {
                resultado.add(r);
            }
        }
        Collections.sort(resultado);
        return resultado;
    }

    // Escala los reclamos de prioridad alta sin atender en 24 horas y cierra los resueltos sin respuesta en 5 dias.
    public List<String> ejecutarReglasAutomaticas() {
        List<String> mensajes = new ArrayList<>();
        int ahora = barrio.getAhora();
        for (Reclamo r : barrio.consultarTodosLosReclamos()) {
            if (r.debeEscalarse(ahora)) {
                r.escalar();
                String mensaje = "Reclamo #" + r.getNumero() + " de prioridad ALTA sin atender hace mas de 24 horas"
                        + " (lote " + r.getLote().getNumero() + "): escalado al administrador";
                barrio.notificar(barrio.getAdministrador(), mensaje);
                mensajes.add(mensaje);
            }
            if (r.debeCerrarse(ahora)) {
                try {
                    r.cambiarEstado(EstadoReclamo.CERRADO, "Cierre automatico: vencio el plazo de 5 dias", ahora);
                    mensajes.add("Reclamo #" + r.getNumero() + " cerrado automaticamente (pasaron 5 dias desde que se resolvio)");
                } catch (ReglaNegocioException e) {
                    mensajes.add("No se pudo cerrar el reclamo #" + r.getNumero() + ": " + e.getMessage());
                }
            }
        }
        return mensajes;
    }
}
