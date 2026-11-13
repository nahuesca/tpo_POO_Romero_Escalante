package aromos.controladores;

import aromos.personas.Lote;
import java.time.LocalDate;
import java.util.Comparator;

// Orden personalizado: primero el lote que mas debe.
public class ComparadorLotesPorDeuda implements Comparator<Lote> {
    private LocalDate hoy;

    public ComparadorLotesPorDeuda(LocalDate hoy) {
        this.hoy = hoy;
    }

    @Override
    public int compare(Lote a, Lote b) {
        double deudaA = a.calcularDeudaVencida(hoy);
        double deudaB = b.calcularDeudaVencida(hoy);
        if (deudaA > deudaB) {
            return -1;
        }
        if (deudaA < deudaB) {
            return 1;
        }
        return 0;
    }
}
