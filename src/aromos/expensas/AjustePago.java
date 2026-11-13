package aromos.expensas;

import java.io.Serializable;
import java.time.LocalDate;

// Correccion de un pago con su fecha, importe y motivo; los pagos nunca se borran.
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
