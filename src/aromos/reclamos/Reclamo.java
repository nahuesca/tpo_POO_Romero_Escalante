package aromos.reclamos;

import aromos.excepciones.ReglaNegocioException;
import aromos.personas.Lote;
import aromos.personas.Persona;
import aromos.personas.Personal;
import aromos.personas.Vecino;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Implementa Comparable: el orden natural de los reclamos es por numero.
// Los momentos (creacion, resolucion) son "horas" del reloj del sistema (ver Barrio).
public class Reclamo implements Comparable<Reclamo>, Serializable {
    public static final int HORAS_ESCALADO = 24;
    public static final int HORAS_REAPERTURA = 5 * 24; // 5 dias

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

    // El reclamo nace ABIERTO y con la prioridad que corresponde a su tipo.
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

    // Solo se puede avanzar al estado siguiente.
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

    // Cada reclamo tiene un unico responsable. Al asignarlo pasa de ABIERTO a ASIGNADO.
    public void asignarResponsable(Personal nuevoResponsable, int ahora) throws ReglaNegocioException {
        if (estado != EstadoReclamo.ABIERTO) {
            throw new ReglaNegocioException("Solo se puede asignar un reclamo ABIERTO (estado actual: "
                    + estado + ")");
        }
        responsable = nuevoResponsable;
        cambiarEstado(EstadoReclamo.ASIGNADO, "Asignado a " + nuevoResponsable.getNombre() + " "
                + nuevoResponsable.getApellido(), ahora);
    }

    // El vecino puede reabrir un reclamo RESUELTO dentro de los 5 dias. Vuelve a EN_CURSO.
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

    public void agregarComentario(Persona autorComentario, String texto, int ahora) {
        comentarios.add(new Comentario(ahora, autorComentario, texto));
    }

    // Prioridad alta, todavia ABIERTO (nadie lo atendio) y pasaron mas de 24 horas: se escala al administrador.
    public boolean debeEscalarse(int ahora) {
        return prioridad == Prioridad.ALTA && estado == EstadoReclamo.ABIERTO && !escalado
                && ahora - horaCreacion > HORAS_ESCALADO;
    }

    public void escalar() {
        escalado = true;
    }

    // Resuelto y el vecino no respondio en 5 dias: se cierra solo.
    public boolean debeCerrarse(int ahora) {
        return estado == EstadoReclamo.RESUELTO && ahora - horaResolucion > HORAS_REAPERTURA;
    }

    // Pendiente = todavia no esta resuelto
    public boolean estaPendiente() {
        return estado == EstadoReclamo.ABIERTO || estado == EstadoReclamo.ASIGNADO
                || estado == EstadoReclamo.EN_CURSO;
    }

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
