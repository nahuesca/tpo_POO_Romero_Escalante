package aromos.mantenimiento;

import java.time.LocalDate;

// Equipo de iluminacion cuya prioridad de falla depende de cuantas fallan en el sector.
public class Luminaria extends Equipo {
    public static final int MAX_FALLAS_NORMALES = 3;

    private String tipoLampara;
    private double potencia;

    public Luminaria(String codigo, Sector sector, LocalDate instalacion, String tipoLampara, double potencia) {
        super(codigo, sector, instalacion);
        this.tipoLampara = tipoLampara;
        this.potencia = potencia;
    }

    // La falla es NORMAL salvo que fallen mas de 3 luminarias del mismo sector.
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
