package aromos.mantenimiento;

import java.time.LocalDate;

public class Camara extends Equipo {
    public static final int HORAS_AVISO = 48;

    private String resolucion;
    private int diasGrabacion;

    public Camara(String codigo, Sector sector, LocalDate instalacion, String resolucion, int diasGrabacion) {
        super(codigo, sector, instalacion);
        this.resolucion = resolucion;
        this.diasGrabacion = diasGrabacion;
    }

    // Una falla de camara siempre es de prioridad alta.
    @Override
    public PrioridadOrden prioridadFalla(int fallasEnElSector) {
        return PrioridadOrden.ALTA;
    }

    @Override
    public String getTipo() {
        return "Camara";
    }

    // Si la camara lleva mas de 48 horas sin funcionar hay que avisar al administrador (una sola vez).
    @Override
    public boolean requiereAviso(int ahora) {
        return !estaOperativo() && !isAvisoEnviado() && ahora - getHoraFalla() > HORAS_AVISO;
    }

    public String getResolucion() { return resolucion; }
    public int getDiasGrabacion() { return diasGrabacion; }
}
