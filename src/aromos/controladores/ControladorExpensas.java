package aromos.controladores;

import aromos.excepciones.ReglaNegocioException;
import aromos.expensas.Expensa;
import aromos.expensas.MedioPago;
import aromos.expensas.Pago;
import aromos.expensas.PresupuestoMensual;
import aromos.personas.Barrio;
import aromos.personas.Lote;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Controlador de la emision de expensas, los pagos y el calculo de morosidad.
public class ControladorExpensas {
    private Barrio barrio;

    public ControladorExpensas(Barrio barrio) {
        this.barrio = barrio;
    }

    // Emite la expensa de cada lote repartiendo el presupuesto segun su coeficiente de participacion.
    public int emitirExpensas(int anio, int mes, double presupuestoTotal) throws ReglaNegocioException {
        if (mes < 1 || mes > 12) {
            throw new ReglaNegocioException("El mes debe estar entre 1 y 12");
        }
        if (presupuestoTotal <= 0) {
            throw new ReglaNegocioException("El presupuesto debe ser mayor a cero");
        }
        if (barrio.buscarPresupuesto(anio, mes) != null) {
            throw new ReglaNegocioException("Ya se emitieron las expensas de ese periodo");
        }
        PresupuestoMensual presupuesto = new PresupuestoMensual(anio, mes, presupuestoTotal);
        int cantidad = 0;
        for (Lote lote : barrio.consultarLotes()) {
            lote.emitirExpensa(presupuesto);
            cantidad++;
        }
        barrio.agregarPresupuesto(presupuesto);
        return cantidad;
    }

    // Registra el pago de un lote aplicandolo a sus expensas pendientes.
    public Pago registrarPago(Lote lote, double importe, MedioPago medio, LocalDate fecha)
            throws ReglaNegocioException {
        Pago pago = lote.registrarPago(importe, medio, fecha);
        revisarMorosidad(lote, fecha);
        return pago;
    }

    // Corrige un pago ya registrado mediante un ajuste con motivo.
    public void corregirPago(Lote lote, Pago pago, double diferencia, String motivo, LocalDate fecha)
            throws ReglaNegocioException {
        lote.corregirPago(pago, diferencia, motivo, fecha);
        revisarMorosidad(lote, fecha);
    }

    // Devuelve las expensas sin pagar de un lote.
    public List<Expensa> consultarDeudas(int numeroLote) throws ReglaNegocioException {
        Lote lote = barrio.buscarLote(numeroLote);
        if (lote == null) {
            throw new ReglaNegocioException("No existe el lote " + numeroLote);
        }
        List<Expensa> pendientes = new ArrayList<>();
        for (Expensa e : lote.obtenerExpensas()) {
            if (!e.estaPagada()) {
                pendientes.add(e);
            }
        }
        return pendientes;
    }

    // Revisa todos los lotes y devuelve un mensaje por cada cambio de situacion de morosidad.
    public List<String> verificarMorosidad(LocalDate hoy) {
        List<String> mensajes = new ArrayList<>();
        for (Lote lote : barrio.consultarLotes()) {
            String mensaje = revisarMorosidad(lote, hoy);
            if (mensaje != null) {
                mensajes.add(mensaje);
            }
        }
        return mensajes;
    }

    // Marca al lote como moroso (y avisa al titular) cuando tiene mas de 3 periodos impagos, o lo libera si ya no los tiene.
    private String revisarMorosidad(Lote lote, LocalDate hoy) {
        int impagos = lote.contarPeriodosImpagos(hoy);
        if (impagos > Lote.MAX_PERIODOS_IMPAGOS && !lote.isMoroso()) {
            lote.setMoroso(true);
            barrio.notificar(lote.consultarTitular(), "NOTIFICACION FORMAL: el lote " + lote.getNumero()
                    + " tiene " + impagos + " periodos impagos y fue marcado como moroso");
            return "Lote " + lote.getNumero() + " marcado como moroso: se notifico al titular";
        }
        if (impagos == 0 && lote.isMoroso()) {
            lote.setMoroso(false);
            return "Lote " + lote.getNumero() + " regularizo su deuda: ya no es moroso";
        }
        return null;
    }
}
