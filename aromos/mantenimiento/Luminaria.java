package aromos.mantenimiento;

import java.time.LocalDate;

public class Luminaria extends Equipo {
    // Si fallan mas de 3 luminarias del mismo sector, la prioridad pasa a alta.
    public static final int MAX_FALLAS_NORMALES = 3;

    private String tipoLampara;
    private double potencia;

    public Luminaria(String codigo, Sector sector, LocalDate instalacion, String tipoLampara, double potencia) {
        super(codigo, sector, instalacion);
        this.tipoLampara = tipoLampara;
        this.potencia = potencia;
    }

    // Prioridad normal, salvo que haya mas de 3 luminarias del mismo sector con falla.
    @Override
    public PrioridadOrden prioridadFalla(int fallasEnElSector) {
        if (fallasEnElSector > MAX_FALLAS_NORMALES) {
            return PrioridadOrden.ALTA;
        }
        return PrioridadOrden.NORMAL;
    }

    @Override
    public String getTipo() {
        return "Luminaria";
    }

    public String getTipoLampara() { return tipoLampara; }
    public double getPotencia() { return potencia; }
}
