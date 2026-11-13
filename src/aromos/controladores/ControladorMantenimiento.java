package aromos.controladores;

import aromos.excepciones.ReglaNegocioException;
import aromos.mantenimiento.Camara;
import aromos.mantenimiento.Equipo;
import aromos.mantenimiento.EstadoEquipo;
import aromos.mantenimiento.Luminaria;
import aromos.mantenimiento.OrdenMantenimiento;
import aromos.mantenimiento.PrioridadOrden;
import aromos.mantenimiento.Sector;
import aromos.personas.Barrio;
import aromos.personas.EmpleadoMantenimiento;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Controlador del control de luminarias y camaras de seguridad y de sus ordenes de mantenimiento.
public class ControladorMantenimiento {
    private Barrio barrio;

    public ControladorMantenimiento(Barrio barrio) {
        this.barrio = barrio;
    }

    // Da de alta una camara de seguridad.
    public Camara registrarCamara(String codigo, Sector sector, LocalDate instalacion, String resolucion,
                                  int diasGrabacion) throws ReglaNegocioException {
        Camara camara = new Camara(codigo.trim(), sector, instalacion, resolucion, diasGrabacion);
        barrio.registrarEquipo(camara);
        return camara;
    }

    // Da de alta una luminaria.
    public Luminaria registrarLuminaria(String codigo, Sector sector, LocalDate instalacion, String tipoLampara,
                                        double potencia) throws ReglaNegocioException {
        Luminaria luminaria = new Luminaria(codigo.trim(), sector, instalacion, tipoLampara, potencia);
        barrio.registrarEquipo(luminaria);
        return luminaria;
    }

    // Registra la falla de un equipo y lo pasa al estado CON_FALLA.
    public Equipo reportarFalla(String codigoEquipo) throws ReglaNegocioException {
        Equipo equipo = buscarEquipo(codigoEquipo);
        equipo.registrarFalla(barrio.getAhora());
        return equipo;
    }

    // Genera la orden de mantenimiento de un equipo con la prioridad que define el propio equipo.
    public OrdenMantenimiento generarOrden(String codigoEquipo, String descripcion) throws ReglaNegocioException {
        Equipo equipo = buscarEquipo(codigoEquipo);
        if (equipo.estaOperativo()) {
            throw new ReglaNegocioException("El equipo " + equipo.getCodigo() + " esta operativo: no hay falla registrada");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ReglaNegocioException("La orden necesita una descripcion del trabajo");
        }
        for (OrdenMantenimiento o : equipo.consultarOrdenes()) {
            if (!o.estaCerrada()) {
                throw new ReglaNegocioException("El equipo ya tiene la orden abierta #" + o.getNumero());
            }
        }
        PrioridadOrden prioridad = equipo.prioridadFalla(barrio.contarEquiposConFalla(equipo));
        OrdenMantenimiento orden = new OrdenMantenimiento(barrio.siguienteNumeroOrden(), equipo, prioridad,
                descripcion.trim(), barrio.getAhora());
        equipo.agregarOrden(orden);
        barrio.agregarOrden(orden);
        return orden;
    }

    // Asigna una orden de mantenimiento a un empleado.
    public void asignarOrden(int numeroOrden, EmpleadoMantenimiento empleado) throws ReglaNegocioException {
        OrdenMantenimiento orden = barrio.buscarOrden(numeroOrden);
        if (orden == null) {
            throw new ReglaNegocioException("No existe la orden #" + numeroOrden);
        }
        orden.asignarEmpleado(empleado, barrio.getAhora());
    }

    // Cierra una orden registrando fecha, tecnico y descripcion del trabajo realizado.
    public void cerrarOrden(int numeroOrden, EmpleadoMantenimiento empleado, String descripcionTrabajo,
                            LocalDate fecha) throws ReglaNegocioException {
        OrdenMantenimiento orden = barrio.buscarOrden(numeroOrden);
        if (orden == null) {
            throw new ReglaNegocioException("No existe la orden #" + numeroOrden);
        }
        empleado.registrarReparacion(orden, descripcionTrabajo, fecha);
    }

    // Cambia manualmente el estado de un equipo.
    public void cambiarEstadoEquipo(String codigoEquipo, EstadoEquipo nuevoEstado) throws ReglaNegocioException {
        buscarEquipo(codigoEquipo).cambiarEstado(nuevoEstado, barrio.getAhora());
    }

    // Devuelve las ordenes de mantenimiento asignadas a un empleado.
    public List<OrdenMantenimiento> consultarOrdenesDe(EmpleadoMantenimiento empleado) {
        List<OrdenMantenimiento> resultado = new ArrayList<>();
        for (OrdenMantenimiento o : barrio.consultarOrdenes()) {
            if (empleado.equals(o.getTecnico())) {
                resultado.add(o);
            }
        }
        return resultado;
    }

    // Devuelve los equipos con falla que todavia no tienen una orden abierta.
    public List<Equipo> consultarEquiposSinOrden() {
        List<Equipo> resultado = new ArrayList<>();
        for (Equipo e : barrio.consultarEquipos()) {
            boolean tieneOrdenAbierta = false;
            for (OrdenMantenimiento o : e.consultarOrdenes()) {
                if (!o.estaCerrada()) {
                    tieneOrdenAbierta = true;
                }
            }
            if (!e.estaOperativo() && !tieneOrdenAbierta) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    // Regla del sistema: avisa al administrador de las camaras que llevan mas de 48 horas sin funcionar.
    public List<String> revisarEquipos() {
        List<String> mensajes = new ArrayList<>();
        for (Equipo e : barrio.consultarEquipos()) {
            if (e.requiereAviso(barrio.getAhora())) {
                e.marcarAvisoEnviado();
                String mensaje = e.getTipo() + " " + e.getCodigo() + " (" + e.getSector()
                        + ") lleva mas de " + Camara.HORAS_AVISO + " horas sin funcionar";
                barrio.notificar(barrio.getAdministrador(), mensaje);
                mensajes.add(mensaje);
            }
        }
        return mensajes;
    }

    // Busca un equipo por codigo y falla si no existe.
    private Equipo buscarEquipo(String codigo) throws ReglaNegocioException {
        Equipo equipo = barrio.buscarEquipo(codigo);
        if (equipo == null) {
            throw new ReglaNegocioException("No existe el equipo con codigo " + codigo);
        }
        return equipo;
    }
}
