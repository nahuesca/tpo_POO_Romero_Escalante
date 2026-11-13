package aromos.expensas;

import java.io.Serializable;
import java.time.LocalDate;

// Cuota mensual de un lote con su capital, intereses por mora y pagos aplicados.
public class Expensa implements Comparable<Expensa>, Serializable {
    public static final double TASA_MORA_MENSUAL = 0.03;
    public static final int DIA_VENCIMIENTO = 10;
    public static final double TOLERANCIA = 0.005;

    private PresupuestoMensual presupuesto;
    private double importe;
    private LocalDate vencimiento;
    private double capitalPagado;
    private double interesPagado;

    public Expensa(PresupuestoMensual presupuesto, double coeficiente) {
        this.presupuesto = presupuesto;
        this.importe = presupuesto.calcularExpensa(coeficiente);

        this.vencimiento = LocalDate.of(presupuesto.getAnio(), presupuesto.getMes(), DIA_VENCIMIENTO);
    }

    // Redondea un importe a dos decimales.
    public static double redondear(double valor) {
        if (valor < 0) {
            return -redondear(-valor);
        }
        return (long) (valor * 100.0 + 0.5) / 100.0;
    }

    // Calcula cuanto capital falta pagar de la expensa.
    public double calcularSaldoCapital() {
        return redondear(importe - capitalPagado);
    }

    // Cuenta los meses de atraso desde el vencimiento hasta hoy.
    public int calcularMesesAtraso(LocalDate hoy) {
        if (!hoy.isAfter(vencimiento)) {
            return 0;
        }
        int meses = (hoy.getYear() - vencimiento.getYear()) * 12
                + (hoy.getMonthValue() - vencimiento.getMonthValue());
        if (hoy.getDayOfMonth() > vencimiento.getDayOfMonth()) {
            meses++;
        }
        return meses;
    }

    // Calcula el interes total por mora segun los meses de atraso.
    public double calcularInteresMora(LocalDate hoy) {
        return redondear(calcularSaldoCapital() * TASA_MORA_MENSUAL * calcularMesesAtraso(hoy));
    }

    // Calcula el interes por mora que todavia no se pago.
    public double calcularInteresPendiente(LocalDate hoy) {
        double pendiente = redondear(calcularInteresMora(hoy) - interesPagado);
        if (pendiente < 0) {
            return 0;
        }
        return pendiente;
    }

    // Calcula lo que falta pagar entre capital e intereses.
    public double calcularSaldo(LocalDate hoy) {
        return redondear(calcularSaldoCapital() + calcularInteresPendiente(hoy));
    }

    // Indica si la expensa no tiene capital pendiente.
    public boolean estaPagada() {
        return calcularSaldoCapital() <= TOLERANCIA;
    }

    // Indica si ya paso la fecha de vencimiento.
    public boolean estaVencida(LocalDate hoy) {
        return hoy.isAfter(vencimiento);
    }

    // Indica si la expensa esta vencida y todavia tiene capital sin pagar.
    public boolean estaImpaga(LocalDate hoy) {
        return !estaPagada() && estaVencida(hoy);
    }

    // Aplica un monto primero a los intereses y despues al capital, y devuelve cuanto uso realmente.
    public double imputar(double monto, LocalDate hoy) {
        double aInteres = calcularInteresPendiente(hoy);
        if (monto < aInteres) {
            aInteres = monto;
        }
        interesPagado = redondear(interesPagado + aInteres);

        double aCapital = redondear(monto - aInteres);
        if (aCapital > calcularSaldoCapital()) {
            aCapital = calcularSaldoCapital();
        }
        capitalPagado = redondear(capitalPagado + aCapital);
        return redondear(aInteres + aCapital);
    }

    // Resta capital pagado para que vuelva a quedar como deuda.
    public void devolverCapital(double monto) {
        capitalPagado = redondear(capitalPagado - monto);
    }

    public String getEstado() {
        if (estaPagada()) {
            return "PAGADA";
        }
        if (capitalPagado > 0 || interesPagado > 0) {
            return "PARCIAL";
        }
        return "PENDIENTE";
    }

    // Ordena las expensas por periodo, de la mas antigua a la mas nueva.
    @Override
    public int compareTo(Expensa otra) {
        return getPeriodo().compareTo(otra.getPeriodo());
    }

    public String getPeriodo() { return presupuesto.getPeriodo(); }
    public double getImporte() { return importe; }
    public LocalDate getVencimiento() { return vencimiento; }
    public double getCapitalPagado() { return capitalPagado; }
    public double getInteresPagado() { return interesPagado; }

    @Override
    public String toString() {
        return "Periodo " + getPeriodo() + " - importe $" + importe + " - vence " + vencimiento
                + " - " + getEstado();
    }
}
