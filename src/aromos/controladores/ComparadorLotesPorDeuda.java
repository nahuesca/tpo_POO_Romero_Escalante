package aromos.controladores;

import aromos.personas.Lote;
import java.time.LocalDate;
import java.util.Comparator;

// Ordena los lotes poniendo primero al que mas debe.
public class ComparadorLotesPorDeuda implements Comparator<Lote> {
    private LocalDate hoy;

    public ComparadorLotesPorDeuda(LocalDate hoy) {
        this.hoy = hoy;
    }

    // Compara dos lotes por su deuda vencida, de mayor a menor.
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
