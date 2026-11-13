package aromos.mantenimiento;

import aromos.excepciones.ReglaNegocioException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Clase abstracta: un equipo del barrio puede ser una Camara o una Luminaria.
// Si se agrega otro tipo de equipo, se crea otra subclase y no hay que tocar el resto del codigo.
public abstract class Equipo implements Serializable {
    private String codigo;
    private Sector sector;
    private LocalDate instalacion;
    private EstadoEquipo estado = EstadoEquipo.OPERATIVO;
    private int horaFalla;            // hora (del reloj del sistema) en que dejo de funcionar
    private boolean avisoEnviado;     // para avisar una sola vez al administrador
    private List<OrdenMantenimiento> ordenes = new ArrayList<>();

    protected Equipo(String codigo, Sector sector, LocalDate instalacion) {
        this.codigo = codigo;
        this.sector = sector;
        this.instalacion = instalacion;
    }

    // Cada subclase decide la prioridad de su falla (se sobreescribe).
    // Recibe cuantos equipos de su tipo y sector estan fallando.
    public abstract PrioridadOrden prioridadFalla(int fallasEnElSector);

    // Nombre del tipo de equipo, para mostrar
    public abstract String getTipo();

    // Por defecto no se avisa nada; la camara lo sobreescribe.
    public boolean requiereAviso(int ahora) {
        return false;
    }

    public void registrarFalla(int ahora) throws ReglaNegocioException {
        if (estado != EstadoEquipo.OPERATIVO) {
            throw new ReglaNegocioException("El equipo " + codigo + " ya tiene una falla registrada (" + estado + ")");
        }
        cambiarEstado(EstadoEquipo.CON_FALLA, ahora);
    }

    public void cambiarEstado(EstadoEquipo nuevoEstado, int ahora) {
        if (nuevoEstado == EstadoEquipo.OPERATIVO) {
            avisoEnviado = false;
        } else if (estado == EstadoEquipo.OPERATIVO) {
            horaFalla = ahora; // empieza a contar el tiempo sin funcionar
        }
        estado = nuevoEstado;
    }

    public boolean estaOperativo() {
        return estado == EstadoEquipo.OPERATIVO;
    }

    public void marcarAvisoEnviado() {
        avisoEnviado = true;
    }

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
