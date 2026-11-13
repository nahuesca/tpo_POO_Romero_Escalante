package aromos.pruebas;

import aromos.controladores.ControladorMantenimiento;
import aromos.mantenimiento.EstadoEquipo;
import aromos.mantenimiento.OrdenMantenimiento;
import aromos.mantenimiento.PrioridadOrden;
import aromos.mantenimiento.Sector;
import java.time.LocalDate;

public class PruebasMantenimiento {
    private static final LocalDate INSTALADO = LocalDate.of(2025, 1, 1);

    public void ejecutar(Verificador v) throws Exception {
        prioridadDeLasOrdenes(v);
        cierreDeUnaOrden(v);
        avisoDeCamaras(v);
    }

    private void prioridadDeLasOrdenes(Verificador v) throws Exception {
        v.grupo("Mantenimiento: prioridad de las ordenes");
        Escenario e = new Escenario();
        ControladorMantenimiento m = new ControladorMantenimiento(e.barrio);
        m.registrarCamara("CAM-1", Sector.ACCESOS, INSTALADO, "1080p", 30);
        for (int i = 1; i <= 4; i++) {
            m.registrarLuminaria("LUM-" + i, Sector.CALLES_INTERNAS, INSTALADO, "LED", 60);
        }

        m.reportarFalla("CAM-1");
        v.verificarIgual("La falla de una camara pone al equipo CON_FALLA", EstadoEquipo.CON_FALLA,
                e.barrio.buscarEquipo("CAM-1").getEstado());
        v.verificarIgual("La orden de una camara es ALTA", PrioridadOrden.ALTA, m.generarOrden("CAM-1", "lente roto").getPrioridad());

        for (int i = 1; i <= 3; i++) {
            m.reportarFalla("LUM-" + i);
            OrdenMantenimiento orden = m.generarOrden("LUM-" + i, "no enciende");
            v.verificarIgual("Luminaria con falla numero " + i + " en el sector: prioridad NORMAL",
                    PrioridadOrden.NORMAL, orden.getPrioridad());
        }
        m.reportarFalla("LUM-4");
        v.verificarIgual("Con mas de 3 luminarias del mismo sector pasa a ALTA", PrioridadOrden.ALTA,
                m.generarOrden("LUM-4", "no enciende").getPrioridad());

        Exception error = null;
        try {
            m.reportarFalla("LUM-1");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("No se reporta dos veces la misma falla", error);
    }

    private void cierreDeUnaOrden(Verificador v) throws Exception {
        v.grupo("Mantenimiento: una orden no se cierra sin fecha, tecnico y descripcion");
        Escenario e = new Escenario();
        ControladorMantenimiento m = new ControladorMantenimiento(e.barrio);
        m.registrarLuminaria("LUM-1", Sector.ACCESOS, INSTALADO, "LED", 60);
        m.reportarFalla("LUM-1");
        OrdenMantenimiento orden = m.generarOrden("LUM-1", "no enciende");
        m.asignarOrden(orden.getNumero(), e.electricista);
        v.verificarIgual("Al asignar la orden el equipo queda EN_REPARACION", EstadoEquipo.EN_REPARACION,
                e.barrio.buscarEquipo("LUM-1").getEstado());

        Exception error = null;
        try {
            m.cerrarOrden(orden.getNumero(), e.electricista, "   ", LocalDate.of(2026, 3, 3));
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Sin descripcion de la reparacion no se cierra", error);

        error = null;
        try {
            m.cerrarOrden(orden.getNumero(), e.electricista, "Cambio de lampara", null);
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Sin fecha no se cierra", error);

        m.cerrarOrden(orden.getNumero(), e.electricista, "Cambio de lampara", LocalDate.of(2026, 3, 3));
        v.verificar("Con todos los datos la orden se cierra", orden.estaCerrada());
        v.verificarIgual("Y el equipo vuelve a OPERATIVO", EstadoEquipo.OPERATIVO,
                e.barrio.buscarEquipo("LUM-1").getEstado());
    }

    private void avisoDeCamaras(Verificador v) throws Exception {
        v.grupo("Mantenimiento: camara sin funcionar mas de 48 horas avisa al administrador");
        Escenario e = new Escenario();
        ControladorMantenimiento m = new ControladorMantenimiento(e.barrio);
        m.registrarCamara("CAM-1", Sector.ACCESOS, INSTALADO, "1080p", 30);
        m.registrarLuminaria("LUM-1", Sector.ACCESOS, INSTALADO, "LED", 60);
        m.reportarFalla("CAM-1");
        m.reportarFalla("LUM-1");

        e.barrio.avanzarHoras(47);
        v.verificarIgual("A las 47 horas no hay aviso", 0, m.revisarEquipos().size());
        e.barrio.avanzarHoras(2);
        v.verificarIgual("A las 49 horas se avisa por la camara (la luminaria no)", 1, m.revisarEquipos().size());
        v.verificarIgual("El administrador recibe la notificacion", 1, e.barrio.consultarNotificaciones(e.admin).size());
        v.verificarIgual("El aviso no se repite", 0, m.revisarEquipos().size());
    }
}
