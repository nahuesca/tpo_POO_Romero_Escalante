package aromos.expensas;

import java.io.Serializable;

// Presupuesto total del barrio para un mes. Se reparte entre los lotes segun su coeficiente.
public class PresupuestoMensual implements Serializable {
    private int anio;
    private int mes;
    private double total;

    public PresupuestoMensual(int anio, int mes, double total) {
        this.anio = anio;
        this.mes = mes;
        this.total = total;
    }

    // Lo que paga un lote: presupuesto x coeficiente de participacion
    public double calcularExpensa(double coeficiente) {
        return Expensa.redondear(total * coeficiente);
    }

    // Texto como "2026-11"
    public String getPeriodo() {
        if (mes < 10) {
            return anio + "-0" + mes;
        }
        return anio + "-" + mes;
    }

    public int getAnio() { return anio; }
    public int getMes() { return mes; }
    public double getTotal() { return total; }
}
