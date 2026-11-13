package aromos.personas;

import aromos.excepciones.NoAutorizadoException;
import aromos.excepciones.ReglaNegocioException;
import aromos.expensas.AjustePago;
import aromos.expensas.Expensa;
import aromos.expensas.MedioPago;
import aromos.expensas.Pago;
import aromos.expensas.PresupuestoMensual;
import aromos.reclamos.Reclamo;
import aromos.reclamos.TipoReclamo;
import aromos.visitas.Autorizacion;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lote implements Serializable {
    // Con mas de 3 periodos impagos el lote es moroso
    public static final int MAX_PERIODOS_IMPAGOS = 3;

    private int numero;
    private double superficie;
    private Vecino titular;
    private Barrio barrio;
    private List<Vecino> residentes = new ArrayList<>();
    private List<Reclamo> reclamos = new ArrayList<>();
    private List<Expensa> expensas = new ArrayList<>();
    private List<Pago> pagos = new ArrayList<>();
    private boolean moroso;

    // Sin "public": los lotes los crea el Barrio (metodo crearLote).
    Lote(int numero, double superficie, Vecino titular, Barrio barrio) {
        this.numero = numero;
        this.superficie = superficie;
        this.titular = titular;
        this.barrio = barrio;
    }

    public Vecino consultarTitular() {
        return titular;
    }

    // Coeficiente de participacion: superficie del lote / superficie total del barrio.
    public double calcularParticipacion() {
        return superficie / barrio.calcularSuperficieTotal();
    }

    public void agregarResidente(Vecino residente) {
        if (!residentes.contains(residente) && !titular.equals(residente)) {
            residentes.add(residente);
        }
    }

    public boolean esTitularOResidente(Vecino vecino) {
        return titular.equals(vecino) || residentes.contains(vecino);
    }

    // Regla: solo el titular o un residente autorizado por el titular puede presentar reclamos.
    public boolean esTitularOAutorizado(Vecino vecino) {
        if (titular.equals(vecino)) {
            return true;
        }
        return residentes.contains(vecino) && vecino.isAutorizadoReclamos();
    }

    // El titular autoriza (o saca la autorizacion) a un residente para reclamar.
    public void autorizarReclamante(Vecino quienAutoriza, Vecino residente, boolean autorizado)
            throws NoAutorizadoException {
        if (!titular.equals(quienAutoriza)) {
            throw new NoAutorizadoException("Solo el titular del lote puede autorizar reclamantes");
        }
        if (!residentes.contains(residente)) {
            throw new NoAutorizadoException("La persona no es residente de este lote");
        }
        residente.setAutorizadoReclamos(autorizado);
    }

    // GRASP Creador: el lote es quien agrupa los reclamos, por eso es quien los crea.
    public Reclamo crearReclamo(Vecino autor, TipoReclamo tipo, String descripcion, int ahora) {
        Reclamo reclamo = new Reclamo(barrio.siguienteNumeroReclamo(), this, autor, tipo, descripcion, ahora);
        reclamos.add(reclamo);
        return reclamo;
    }

    public List<Reclamo> consultarReclamos() {
        return reclamos;
    }

    public List<Autorizacion> consultarAutorizaciones() {
        List<Autorizacion> resultado = new ArrayList<>();
        for (Autorizacion a : barrio.consultarAutorizaciones()) {
            if (a.getLote() == this) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    // ---------- expensas y pagos ----------

    public Expensa emitirExpensa(PresupuestoMensual presupuesto) throws ReglaNegocioException {
        for (Expensa e : expensas) {
            if (e.getPeriodo().equals(presupuesto.getPeriodo())) {
                throw new ReglaNegocioException("Ya se emitio la expensa del periodo " + presupuesto.getPeriodo());
            }
        }
        Expensa expensa = new Expensa(presupuesto, calcularParticipacion());
        expensas.add(expensa);
        Collections.sort(expensas); // orden natural: de la mas antigua a la mas nueva
        return expensa;
    }

    public Pago registrarPago(double importe, MedioPago medio, LocalDate fecha) throws ReglaNegocioException {
        if (importe <= 0) {
            throw new ReglaNegocioException("El importe del pago debe ser mayor a cero");
        }
        if (importe > calcularDeudaTotal(fecha) + Expensa.TOLERANCIA) {
            throw new ReglaNegocioException("El importe supera la deuda del lote");
        }
        Pago pago = new Pago(fecha, importe, medio);
        repartirPago(pago, importe, fecha);
        pagos.add(pago);
        return pago;
    }

    // Reparte el dinero empezando por la expensa mas antigua.
    // En cada expensa se paga primero el interes y despues el capital (lo hace Expensa.imputar).
    private void repartirPago(Pago pago, double monto, LocalDate fecha) {
        double restante = monto;
        for (Expensa e : expensas) {
            if (restante < Expensa.TOLERANCIA) {
                break;
            }
            double usado = e.imputar(restante, fecha);
            if (usado > 0) {
                pago.agregarImputacion(e, usado);
            }
            restante = Expensa.redondear(restante - usado);
        }
    }

    // Un pago nunca se elimina: si hubo un error se corrige con un asiento de ajuste.
    // La diferencia puede ser negativa (el pago era menor) o positiva (era mayor).
    public void corregirPago(Pago pago, double diferencia, String motivo, LocalDate fecha)
            throws ReglaNegocioException {
        if (!pagos.contains(pago)) {
            throw new ReglaNegocioException("El pago no pertenece a este lote");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new ReglaNegocioException("El ajuste necesita un motivo");
        }
        if (diferencia < Expensa.TOLERANCIA && diferencia > -Expensa.TOLERANCIA) {
            throw new ReglaNegocioException("El ajuste no puede ser cero");
        }
        if (pago.calcularImporteEfectivo() + diferencia < 0) {
            throw new ReglaNegocioException("El ajuste dejaria el pago con importe negativo");
        }
        if (diferencia > 0) {
            if (diferencia > calcularDeudaTotal(fecha) + Expensa.TOLERANCIA) {
                throw new ReglaNegocioException("El ajuste supera la deuda del lote");
            }
            repartirPago(pago, diferencia, fecha);
        } else {
            pago.devolver(-diferencia);
        }
        pago.agregarAjuste(new AjustePago(fecha, diferencia, motivo.trim()));
    }

    // Cuenta las expensas vencidas que todavia no se pagaron.
    public int contarPeriodosImpagos(LocalDate hoy) {
        int cantidad = 0;
        for (Expensa e : expensas) {
            if (e.estaImpaga(hoy)) {
                cantidad++;
            }
        }
        return cantidad;
    }

    // Todo lo que debe el lote (capital + intereses), incluidas las expensas que todavia no vencieron.
    public double calcularDeudaTotal(LocalDate hoy) {
        double total = 0;
        for (Expensa e : expensas) {
            total += e.calcularSaldo(hoy);
        }
        return Expensa.redondear(total);
    }

    // Solo lo que ya esta vencido.
    public double calcularDeudaVencida(LocalDate hoy) {
        double total = 0;
        for (Expensa e : expensas) {
            if (e.estaImpaga(hoy)) {
                total += e.calcularSaldo(hoy);
            }
        }
        return Expensa.redondear(total);
    }

    public boolean determinarSiEsMoroso(LocalDate hoy) {
        return contarPeriodosImpagos(hoy) > MAX_PERIODOS_IMPAGOS;
    }

    public List<Expensa> obtenerExpensas() { return expensas; }
    public List<Pago> obtenerPagos() { return pagos; }
    public boolean isMoroso() { return moroso; }
    public void setMoroso(boolean moroso) { this.moroso = moroso; }
    public int getNumero() { return numero; }
    public double getSuperficie() { return superficie; }
    public Barrio getBarrio() { return barrio; }
    public List<Vecino> getResidentes() { return residentes; }

    @Override
    public String toString() {
        return "Lote " + numero + " (" + superficie + " m2, titular " + titular.getNombre() + " "
                + titular.getApellido() + ")";
    }
}
