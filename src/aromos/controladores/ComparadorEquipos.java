package aromos.controladores;

import aromos.mantenimiento.Equipo;
import java.util.Comparator;

// Ordena los equipos por sector y, dentro del sector, por codigo.
public class ComparadorEquipos implements Comparator<Equipo> {
    // Compara dos equipos primero por sector y despues por codigo.
    @Override
    public int compare(Equipo a, Equipo b) {
        if (a.getSector() != b.getSector()) {
            return a.getSector().ordinal() - b.getSector().ordinal();
        }
        return a.getCodigo().compareTo(b.getCodigo());
    }
}
