package aromos.controladores;

import aromos.reclamos.Reclamo;
import java.util.Comparator;

// Orden personalizado (Comparator): primero los reclamos de mayor prioridad
// y, si tienen la misma prioridad, primero el mas antiguo.
public class ComparadorReclamos implements Comparator<Reclamo> {

    @Override
    public int compare(Reclamo a, Reclamo b) {
        // ALTA esta declarada antes que MEDIA y BAJA en el enum, por eso alcanza con comparar los ordinal
        if (a.getPrioridad() != b.getPrioridad()) {
            return a.getPrioridad().ordinal() - b.getPrioridad().ordinal();
        }
        return a.getHoraCreacion() - b.getHoraCreacion();
    }
}
