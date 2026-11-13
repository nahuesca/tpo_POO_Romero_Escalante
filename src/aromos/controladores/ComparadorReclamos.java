package aromos.controladores;

import aromos.reclamos.Reclamo;
import java.util.Comparator;

// Ordena los reclamos por prioridad y, a igual prioridad, por antiguedad.
public class ComparadorReclamos implements Comparator<Reclamo> {
    // Compara dos reclamos por prioridad y despues por numero de reclamo mas antiguo.
    @Override
    public int compare(Reclamo a, Reclamo b) {
        if (a.getPrioridad() != b.getPrioridad()) {
            return a.getPrioridad().ordinal() - b.getPrioridad().ordinal();
        }
        return a.getHoraCreacion() - b.getHoraCreacion();
    }
}
