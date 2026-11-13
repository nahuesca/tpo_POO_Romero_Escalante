package aromos.visitas;

import aromos.excepciones.ReglaNegocioException;
import aromos.personas.Guardia;
import java.io.Serializable;
import java.time.LocalDate;

// Registro de la hora de ingreso y de egreso de una persona al barrio.
public class RegistroAcceso implements Serializable {
    private Autorizacion autorizacion;
    private Guardia guardia;
    private LocalDate fecha;
    private int horaIngreso;
    private int horaEgreso;
    private boolean salio;

    public RegistroAcceso(Autorizacion autorizacion, Guardia guardia, LocalDate fecha, int horaIngreso) {
        this.autorizacion = autorizacion;
        this.guardia = guardia;
        this.fecha = fecha;
        this.horaIngreso = horaIngreso;
    }

    // Registra la hora de salida si la persona todavia estaba dentro.
    public void registrarEgreso(int hora) throws ReglaNegocioException {
        if (salio) {
            throw new ReglaNegocioException("El egreso de esta persona ya fue registrado");
        }
        horaEgreso = hora;
        salio = true;
    }

    // Indica si la persona entro y todavia no salio.
    public boolean estaAbierto() {
        return !salio;
    }

    public Autorizacion getAutorizacion() { return autorizacion; }
    public Guardia getGuardia() { return guardia; }
    public LocalDate getFecha() { return fecha; }
    public int getHoraIngreso() { return horaIngreso; }
    public int getHoraEgreso() { return horaEgreso; }

    @Override
    public String toString() {
        String texto = fecha + " " + autorizacion.getVisitante().getNombre() + " "
                + autorizacion.getVisitante().getApellido() + " -> lote " + autorizacion.getLote().getNumero()
                + " ingreso " + horaIngreso + " hs";
        if (salio) {
            texto = texto + ", egreso " + horaEgreso + " hs";
        } else {
            texto = texto + " (sigue adentro)";
        }
        return texto + " - guardia " + guardia.getNombre() + " " + guardia.getApellido();
    }
}
