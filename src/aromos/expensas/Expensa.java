package aromos.expensas;

import java.io.Serializable;
import java.time.LocalDate;

// Expensa mensual de un lote. Orden natural (Comparable): por periodo, de la mas antigua a la mas nueva.
// GRASP Experto: la expensa tiene el importe y el vencimiento, por eso ella misma calcula su interes.
public class Expensa implements Comparable<Expensa>, Serializable {
    public static final double TASA_MORA_MENSUAL = 0.03; // 3% por mes
    public static final int DIA_VENCIMIENTO = 10;
    public static final double TOLERANCIA = 0.005; // para comparar numeros con decimales

    private PresupuestoMensual presupuesto;
    private double importe;
    private LocalDate vencimiento;
    private double capitalPagado;
    private double interesPagado;

    public Expensa(PresupuestoMensual presupuesto, double coeficiente) {
        this.presupuesto = presupuesto;
        this.importe = presupuesto.calcularExpensa(coeficiente);
        // vence el dia 10 del mes del periodo
        this.vencimiento = LocalDate.of(presupuesto.getAnio(), presupuesto.getMes(), DIA_VENCIMIENTO);
    }

    // Redondea a 2 decimales (centavos)
    public static double redondear(double valor) {
        if (valor < 0) {
            return -redondear(-valor);
        }
        return (long) (valor * 100.0 + 0.5) / 100.0;
    }

    public double calcularSaldoCapital() {
        return redondear(importe - capitalPagado);
    }

    // Cada mes empezado despues del vencimiento cuenta como un mes de atraso.
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

    // Interes punitorio: 3% mensual sobre el capital que falta pagar.
    public double calcularInteresMora(LocalDate hoy) {
        return redondear(calcularSaldoCapital() * TASA_MORA_MENSUAL * calcularMesesAtraso(hoy));
    }

    // Interes que todavia no se pago (los pagos se aplican primero a los intereses).
    public double calcularInteresPendiente(LocalDate hoy) {
        double pendiente = redondear(calcularInteresMora(hoy) - interesPagado);
        if (pendiente < 0) {
            return 0;
        }
        return pendiente;
    }

    // Capital que falta pagar + interes pendiente
    public double calcularSaldo(LocalDate hoy) {
        return redondear(calcularSaldoCapital() + calcularInteresPendiente(hoy));
    }

    public boolean estaPagada() {
        return calcularSaldoCapital() <= TOLERANCIA;
    }

    public boolean estaVencida(LocalDate hoy) {
        return hoy.isAfter(vencimiento);
    }

    // Periodo impago = expensa vencida que todavia no se pago
    public boolean estaImpaga(LocalDate hoy) {
        return !estaPagada() && estaVencida(hoy);
    }

    // Aplica un monto a esta expensa: primero al interes y despues al capital.
    // Devuelve cuanto uso realmente.
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

    // Se usa cuando se corrige un pago hacia abajo: el capital vuelve a quedar sin pagar.
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

    @Override
    public int compareTo(Expensa otra) {
        // el periodo "2026-09" es menor que "2026-10", asi que alcanza con comparar los textos
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
