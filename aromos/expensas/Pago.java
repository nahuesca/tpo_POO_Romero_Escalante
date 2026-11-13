package aromos.expensas;

import aromos.excepciones.ReglaNegocioException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Un pago de un lote. Nunca se elimina: si hubo un error se corrige con un AjustePago.
// Un pago puede cubrir varias expensas, por eso guarda en que expensas se aplico y cuanto en cada una.
public class Pago implements Serializable {
    private LocalDate fecha;
    private double importe;
    private MedioPago medio;
    private List<AjustePago> ajustes = new ArrayList<>();
    private List<Expensa> expensasPagadas = new ArrayList<>();
    private List<Double> montosPagados = new ArrayList<>();

    public Pago(LocalDate fecha, double importe, MedioPago medio) {
        this.fecha = fecha;
        this.importe = Expensa.redondear(importe);
        this.medio = medio;
    }

    public void agregarImputacion(Expensa expensa, double monto) {
        int posicion = expensasPagadas.indexOf(expensa);
        if (posicion == -1) {
            expensasPagadas.add(expensa);
            montosPagados.add(monto);
        } else {
            montosPagados.set(posicion, Expensa.redondear(montosPagados.get(posicion) + monto));
        }
    }

    // Importe original mas todos los ajustes
    public double calcularImporteEfectivo() {
        double total = importe;
        for (AjustePago a : ajustes) {
            total = total + a.getImporte();
        }
        return Expensa.redondear(total);
    }

    // Quita parte del pago de las expensas, empezando por la ultima en la que se aplico.
    // Lo que se devuelve vuelve a quedar como capital sin pagar.
    public void devolver(double monto) throws ReglaNegocioException {
        // primero se verifica que alcance, para no dejar el pago a medio corregir
        double disponible = 0;
        for (int i = 0; i < expensasPagadas.size(); i++) {
            disponible = disponible + menor(montosPagados.get(i), expensasPagadas.get(i).getCapitalPagado());
        }
        if (monto > disponible + Expensa.TOLERANCIA) {
            throw new ReglaNegocioException("El ajuste es mayor que lo que este pago aplico a capital ($"
                    + Expensa.redondear(disponible) + ")");
        }
        double restante = monto;
        for (int i = expensasPagadas.size() - 1; i >= 0 && restante > Expensa.TOLERANCIA; i--) {
            Expensa expensa = expensasPagadas.get(i);
            double quitar = menor(restante, menor(montosPagados.get(i), expensa.getCapitalPagado()));
            expensa.devolverCapital(quitar);
            montosPagados.set(i, Expensa.redondear(montosPagados.get(i) - quitar));
            restante = Expensa.redondear(restante - quitar);
        }
    }

    private double menor(double a, double b) {
        if (a < b) {
            return a;
        }
        return b;
    }

    public void agregarAjuste(AjustePago ajuste) {
        ajustes.add(ajuste);
    }

    public LocalDate getFecha() { return fecha; }
    public double getImporte() { return importe; }
    public MedioPago getMedio() { return medio; }
    public List<AjustePago> getAjustes() { return ajustes; }

    @Override
    public String toString() {
        String texto = fecha + " $" + importe + " (" + medio + ")";
        if (!ajustes.isEmpty()) {
            texto = texto + " - importe efectivo $" + calcularImporteEfectivo() + " tras " + ajustes.size()
                    + " ajuste(s)";
        }
        return texto;
    }
}
