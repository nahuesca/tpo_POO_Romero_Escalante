package aromos.mantenimiento;

import aromos.excepciones.ReglaNegocioException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Clase abstracta de un equipo del barrio, que puede ser una camara o una luminaria.
public abstract class Equipo implements Serializable {
    private String codigo;
    private Sector sector;
    private LocalDate instalacion;
    private EstadoEquipo estado = EstadoEquipo.OPERATIVO;
    private int horaFalla;
    private boolean avisoEnviado;
    private List<OrdenMantenimiento> ordenes = new ArrayList<>();

    protected Equipo(String codigo, Sector sector, LocalDate instalacion) {
        this.codigo = codigo;
        this.sector = sector;
        this.instalacion = instalacion;
    }

    public abstract PrioridadOrden prioridadFalla(int fallasEnElSector);

    public abstract String getTipo();

    // Por defecto un equipo no requiere aviso; las subclases pueden cambiarlo.
    public boolean requiereAviso(int ahora) {
        return false;
    }

    // Registra la falla del equipo y guarda la hora en que ocurrio.
    public void registrarFalla(int ahora) throws ReglaNegocioException {
        if (estado != EstadoEquipo.OPERATIVO) {
            throw new ReglaNegocioException("El equipo " + codigo + " ya tiene una falla registrada (" + estado + ")");
        }
        cambiarEstado(EstadoEquipo.CON_FALLA, ahora);
    }

    // Cambia el estado del equipo y registra la hora del cambio.
    public void cambiarEstado(EstadoEquipo nuevoEstado, int ahora) {
        if (nuevoEstado == EstadoEquipo.OPERATIVO) {
            avisoEnviado = false;
        } else if (estado == EstadoEquipo.OPERATIVO) {
            horaFalla = ahora;
        }
        estado = nuevoEstado;
    }

    // Indica si el equipo esta funcionando.
    public boolean estaOperativo() {
        return estado == EstadoEquipo.OPERATIVO;
    }

    // Marca que ya se aviso al administrador para no avisar dos veces.
    public void marcarAvisoEnviado() {
        avisoEnviado = true;
    }

    // Agrega una orden de mantenimiento al historial del equipo.
    public void agregarOrden(OrdenMantenimiento orden) {
        ordenes.add(orden);
    }

    public List<OrdenMantenimiento> consultarOrdenes() { return ordenes; }
    public String getCodigo() { return codigo; }
    public Sector getSector() { return sector; }
    public LocalDate getInstalacion() { return instalacion; }
    public EstadoEquipo getEstado() { return estado; }
    public int getHoraFalla() { return horaFalla; }
    public boolean isAvisoEnviado() { return avisoEnviado; }

    @Override
    public String toString() {
        return getTipo() + " " + codigo + " - " + sector + " - " + estado + " - instalado " + instalacion;
    }
}
