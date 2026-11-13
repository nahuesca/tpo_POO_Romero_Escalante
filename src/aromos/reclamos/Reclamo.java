package aromos.reclamos;

import aromos.excepciones.ReglaNegocioException;
import aromos.personas.Lote;
import aromos.personas.Persona;
import aromos.personas.Personal;
import aromos.personas.Vecino;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Reclamo de un vecino con su estado, responsable, historial y comentarios.
public class Reclamo implements Comparable<Reclamo>, Serializable {
    public static final int HORAS_ESCALADO = 24;
    public static final int HORAS_REAPERTURA = 5 * 24;

    private int numero;
    private int horaCreacion;
    private TipoReclamo tipo;
    private String descripcion;
    private Prioridad prioridad;
    private EstadoReclamo estado;
    private int horaResolucion;
    private boolean escalado;
    private Lote lote;
    private Vecino autor;
    private Personal responsable;
    private List<CambioEstado> cambios = new ArrayList<>();
    private List<Comentario> comentarios = new ArrayList<>();

    public Reclamo(int numero, Lote lote, Vecino autor, TipoReclamo tipo, String descripcion, int ahora) {
        this.numero = numero;
        this.lote = lote;
        this.autor = autor;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.horaCreacion = ahora;
        this.prioridad = tipo.getPrioridad();
        this.estado = EstadoReclamo.ABIERTO;
        cambios.add(new CambioEstado(ahora, EstadoReclamo.ABIERTO, "Reclamo presentado"));
    }

    // Pasa el reclamo al estado siguiente y lo anota en el historial.
    public void cambiarEstado(EstadoReclamo nuevoEstado, String observacion, int ahora)
            throws ReglaNegocioException {
        if (nuevoEstado != estado.siguiente()) {
            throw new ReglaNegocioException("Un reclamo " + estado + " solo puede pasar a "
                    + estado.siguiente() + ", no a " + nuevoEstado);
        }
        if (nuevoEstado == EstadoReclamo.ASIGNADO && responsable == null) {
            throw new ReglaNegocioException("No se puede pasar a ASIGNADO sin responsable");
        }
        estado = nuevoEstado;
        if (nuevoEstado == EstadoReclamo.RESUELTO) {
            horaResolucion = ahora;
        }
        cambios.add(new CambioEstado(ahora, nuevoEstado, observacion));
    }

    // Asigna un responsable al reclamo y lo pasa a ASIGNADO.
    public void asignarResponsable(Personal nuevoResponsable, int ahora) throws ReglaNegocioException {
        if (estado != EstadoReclamo.ABIERTO) {
            throw new ReglaNegocioException("Solo se puede asignar un reclamo ABIERTO (estado actual: "
                    + estado + ")");
        }
        responsable = nuevoResponsable;
        cambiarEstado(EstadoReclamo.ASIGNADO, "Asignado a " + nuevoResponsable.getNombre() + " "
                + nuevoResponsable.getApellido(), ahora);
    }

    // Reabre un reclamo resuelto, siempre que no hayan pasado 5 dias desde la resolucion.
    public void reabrir(int ahora) throws ReglaNegocioException {
        if (estado != EstadoReclamo.RESUELTO) {
            throw new ReglaNegocioException("Solo se puede reabrir un reclamo RESUELTO (estado actual: "
                    + estado + ")");
        }
        if (ahora - horaResolucion > HORAS_REAPERTURA) {
            throw new ReglaNegocioException("Pasaron mas de 5 dias desde la resolucion: ya no se puede reabrir");
        }
        estado = EstadoReclamo.EN_CURSO;
        cambios.add(new CambioEstado(ahora, EstadoReclamo.EN_CURSO, "Reabierto por el vecino"));
    }

    // Agrega un comentario al reclamo.
    public void agregarComentario(Persona autorComentario, String texto, int ahora) {
        comentarios.add(new Comentario(ahora, autorComentario, texto));
    }

    // Indica si un reclamo de prioridad alta sigue abierto y lleva mas de 24 horas sin atender.
    public boolean debeEscalarse(int ahora) {
        return prioridad == Prioridad.ALTA && estado == EstadoReclamo.ABIERTO && !escalado
                && ahora - horaCreacion > HORAS_ESCALADO;
    }

    // Marca el reclamo como escalado al administrador.
    public void escalar() {
        escalado = true;
    }

    // Indica si un reclamo resuelto lleva 5 dias sin respuesta del vecino.
    public boolean debeCerrarse(int ahora) {
        return estado == EstadoReclamo.RESUELTO && ahora - horaResolucion > HORAS_REAPERTURA;
    }

    // Indica si el reclamo todavia no se resolvio.
    public boolean estaPendiente() {
        return estado == EstadoReclamo.ABIERTO || estado == EstadoReclamo.ASIGNADO
                || estado == EstadoReclamo.EN_CURSO;
    }

    // Ordena los reclamos por numero.
    @Override
    public int compareTo(Reclamo otro) {
        return numero - otro.numero;
    }

    public List<CambioEstado> consultarHistorial() { return cambios; }
    public List<Comentario> consultarComentarios() { return comentarios; }
    public int getNumero() { return numero; }
    public int getHoraCreacion() { return horaCreacion; }
    public TipoReclamo getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public Prioridad getPrioridad() { return prioridad; }
    public EstadoReclamo getEstado() { return estado; }
    public boolean isEscalado() { return escalado; }
    public Lote getLote() { return lote; }
    public Vecino getAutor() { return autor; }
    public Personal getResponsable() { return responsable; }

    @Override
    public String toString() {
        String resp = "sin asignar";
        if (responsable != null) {
            resp = responsable.getNombre() + " " + responsable.getApellido();
        }
        String texto = "#" + numero + " [" + estado + "] " + tipo + " (prioridad " + prioridad + ")";
        if (escalado) {
            texto = texto + " ESCALADO";
        }
        return texto + " - lote " + lote.getNumero() + " - " + descripcion + " - responsable: " + resp;
    }
}
