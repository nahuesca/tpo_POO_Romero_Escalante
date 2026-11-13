package aromos.visitas;

import aromos.excepciones.NoAutorizadoException;
import aromos.excepciones.ReglaNegocioException;
import aromos.personas.Lote;
import aromos.personas.TipoVisitante;
import aromos.personas.Vecino;
import aromos.personas.Visitante;
import java.io.Serializable;
import java.time.LocalDate;

// Autorizacion previa de un vecino para que una persona entre al barrio.
// La franja horaria se guarda como horas del dia (0 a 23).
public class Autorizacion implements Serializable {
    public static final int MAX_DIAS_RECURRENTE = 30;

    private Vecino vecino;
    private Visitante visitante;
    private Lote lote;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private int horaDesde;
    private int horaHasta;
    private boolean recurrente;

    public Autorizacion(Vecino vecino, Visitante visitante, Lote lote, LocalDate fechaDesde,
                        LocalDate fechaHasta, int horaDesde, int horaHasta, boolean recurrente)
            throws ReglaNegocioException {
        if (!lote.esTitularOResidente(vecino)) {
            throw new NoAutorizadoException("Solo el titular o un residente del lote puede autorizar visitas");
        }
        if (fechaHasta.isBefore(fechaDesde)) {
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la de inicio");
        }
        if (horaDesde < 0 || horaHasta > 23 || horaDesde >= horaHasta) {
            throw new ReglaNegocioException("Franja horaria invalida (use horas de 0 a 23, y la de inicio menor que la de fin)");
        }
        if (!recurrente && !fechaDesde.equals(fechaHasta)) {
            throw new ReglaNegocioException("Una autorizacion que no es recurrente vale un solo dia");
        }
        if (recurrente) {
            if (visitante.getTipo() != TipoVisitante.PROVEEDOR_HABITUAL) {
                throw new ReglaNegocioException("Solo los proveedores habituales pueden tener autorizacion recurrente");
            }
            if (fechaDesde.plusDays(MAX_DIAS_RECURRENTE - 1).isBefore(fechaHasta)) {
                throw new ReglaNegocioException("La autorizacion recurrente no puede superar los 30 dias");
            }
        }
        this.vecino = vecino;
        this.visitante = visitante;
        this.lote = lote;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.horaDesde = horaDesde;
        this.horaHasta = horaHasta;
        this.recurrente = recurrente;
    }

    // Esta vigente si la fecha y la hora actuales estan dentro de lo autorizado.
    public boolean estaVigente(LocalDate hoy, int hora) {
        boolean fechaValida = !hoy.isBefore(fechaDesde) && !hoy.isAfter(fechaHasta);
        boolean horaValida = hora >= horaDesde && hora <= horaHasta;
        return fechaValida && horaValida;
    }

    public Vecino getVecino() { return vecino; }
    public Visitante getVisitante() { return visitante; }
    public Lote getLote() { return lote; }
    public LocalDate getFechaDesde() { return fechaDesde; }
    public LocalDate getFechaHasta() { return fechaHasta; }
    public int getHoraDesde() { return horaDesde; }
    public int getHoraHasta() { return horaHasta; }
    public boolean isRecurrente() { return recurrente; }

    @Override
    public String toString() {
        String texto = visitante.getNombre() + " " + visitante.getApellido() + " (doc " + visitante.getDocumento()
                + ", " + visitante.getTipo() + ") -> lote " + lote.getNumero() + " del " + fechaDesde + " al "
                + fechaHasta + " de " + horaDesde + " a " + horaHasta + " hs";
        if (recurrente) {
            texto = texto + " [recurrente]";
        }
        return texto;
    }
}
