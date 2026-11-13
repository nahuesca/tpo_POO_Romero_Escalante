package aromos.expensas;

import java.io.Serializable;
import java.time.LocalDate;

// Asiento de ajuste: corrige un pago sin eliminarlo. El importe puede ser positivo o negativo.
public class AjustePago implements Serializable {
    private LocalDate fecha;
    private double importe;
    private String motivo;

    public AjustePago(LocalDate fecha, double importe, String motivo) {
        this.fecha = fecha;
        this.importe = importe;
        this.motivo = motivo;
    }

    public LocalDate getFecha() { return fecha; }
    public double getImporte() { return importe; }
    public String getMotivo() { return motivo; }
}
