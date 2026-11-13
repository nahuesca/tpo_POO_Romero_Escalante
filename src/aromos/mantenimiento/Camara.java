package aromos.mantenimiento;

import java.time.LocalDate;

// Equipo de seguridad cuyas fallas siempre tienen prioridad alta.
public class Camara extends Equipo {
    public static final int HORAS_AVISO = 48;

    private String resolucion;
    private int diasGrabacion;

    public Camara(String codigo, Sector sector, LocalDate instalacion, String resolucion, int diasGrabacion) {
        super(codigo, sector, instalacion);
        this.resolucion = resolucion;
        this.diasGrabacion = diasGrabacion;
    }

    // La falla de una camara siempre es de prioridad ALTA.
    @Override
    public PrioridadOrden prioridadFalla(int fallasEnElSector) {
        return PrioridadOrden.ALTA;
    }

    @Override
    public String getTipo() {
        return "Camara";
    }

    // Indica si la camara lleva mas de 48 horas sin funcionar y todavia no se aviso.
    @Override
    public boolean requiereAviso(int ahora) {
        return !estaOperativo() && !isAvisoEnviado() && ahora - getHoraFalla() > HORAS_AVISO;
    }

    public String getResolucion() { return resolucion; }
    public int getDiasGrabacion() { return diasGrabacion; }
}
