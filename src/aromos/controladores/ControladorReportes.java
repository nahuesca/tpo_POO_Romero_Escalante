package aromos.controladores;

import aromos.mantenimiento.Equipo;
import aromos.personas.Barrio;
import aromos.personas.Lote;
import aromos.reclamos.Reclamo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Genera los reportes del administrador sobre reclamos, morosidad y equipos.
public class ControladorReportes {
    private Barrio barrio;

    public ControladorReportes(Barrio barrio) {
        this.barrio = barrio;
    }

    // Devuelve los reclamos sin resolver ordenados por prioridad y antiguedad.
    public List<Reclamo> reclamosPendientes() {
        List<Reclamo> pendientes = new ArrayList<>();
        for (Reclamo r : barrio.consultarTodosLosReclamos()) {
            if (r.estaPendiente()) {
                pendientes.add(r);
            }
        }
        Collections.sort(pendientes, new ComparadorReclamos());
        return pendientes;
    }

    // Devuelve los lotes con deuda vencida, primero los que mas deben.
    public List<Lote> morosidadPorLote(LocalDate hoy) {
        List<Lote> resultado = new ArrayList<>();
        for (Lote lote : barrio.consultarLotes()) {
            if (lote.calcularDeudaVencida(hoy) > 0) {
                resultado.add(lote);
            }
        }
        Collections.sort(resultado, new ComparadorLotesPorDeuda(hoy));
        return resultado;
    }

    // Devuelve los equipos que no estan operativos.
    public List<Equipo> equiposFueraDeServicio() {
        List<Equipo> resultado = new ArrayList<>();
        for (Equipo e : barrio.consultarEquipos()) {
            if (!e.estaOperativo()) {
                resultado.add(e);
            }
        }
        Collections.sort(resultado, new ComparadorEquipos());
        return resultado;
    }
}
