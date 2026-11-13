package aromos.expensas;

import java.io.Serializable;

// Presupuesto total del barrio para un mes, que se reparte entre los lotes.
public class PresupuestoMensual implements Serializable {
    private int anio;
    private int mes;
    private double total;

    public PresupuestoMensual(int anio, int mes, double total) {
        this.anio = anio;
        this.mes = mes;
        this.total = total;
    }

    // Calcula la parte del presupuesto que le toca a un lote segun su coeficiente.
    public double calcularExpensa(double coeficiente) {
        return Expensa.redondear(total * coeficiente);
    }

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
