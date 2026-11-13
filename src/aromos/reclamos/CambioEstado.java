package aromos.reclamos;

import java.io.Serializable;

// Una linea del historial de un reclamo: cuando, a que estado paso y una observacion.
public class CambioEstado implements Serializable {
    private int hora;
    private EstadoReclamo nuevoEstado;
    private String observacion;

    public CambioEstado(int hora, EstadoReclamo nuevoEstado, String observacion) {
        this.hora = hora;
        this.nuevoEstado = nuevoEstado;
        this.observacion = observacion;
    }

    public int getHora() { return hora; }
    public EstadoReclamo getNuevoEstado() { return nuevoEstado; }
    public String getObservacion() { return observacion; }
}
