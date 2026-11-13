package aromos.pruebas;

import aromos.controladores.ControladorReclamos;
import aromos.reclamos.EstadoReclamo;
import aromos.reclamos.Prioridad;
import aromos.reclamos.Reclamo;
import aromos.reclamos.TipoReclamo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Pruebas de la presentacion y gestion de reclamos.
public class PruebasReclamos {
    // Corre todas las pruebas de reclamos.
    public void ejecutar(Verificador v) throws Exception {
        prioridadPorTipo(v);
        estadosSoloAvanzan(v);
        quienPuedeReclamar(v);
        limiteDeCincoReclamos(v);
        reaperturaYCierre(v);
        escalado(v);
        ordenNatural(v);
    }

    // Prueba la prioridad de cada tipo de reclamo.
    private void prioridadPorTipo(Verificador v) throws Exception {
        v.grupo("Reclamos: prioridad segun el tipo");
        Escenario e = new Escenario();
        ControladorReclamos c = new ControladorReclamos(e.barrio);
        Reclamo seguridad = c.presentarReclamo(e.titular1, e.lote1, TipoReclamo.SEGURIDAD, "robo");
        Reclamo mantenimiento = c.presentarReclamo(e.titular1, e.lote1, TipoReclamo.MANTENIMIENTO, "caneria");
        Reclamo convivencia = c.presentarReclamo(e.titular1, e.lote1, TipoReclamo.CONVIVENCIA, "ruido");
        v.verificarIgual("Seguridad tiene prioridad ALTA", Prioridad.ALTA, seguridad.getPrioridad());
        v.verificarIgual("Mantenimiento tiene prioridad MEDIA", Prioridad.MEDIA, mantenimiento.getPrioridad());
        v.verificarIgual("Convivencia tiene prioridad BAJA", Prioridad.BAJA, convivencia.getPrioridad());
        v.verificarIgual("El reclamo nace ABIERTO", EstadoReclamo.ABIERTO, seguridad.getEstado());
    }

    // Prueba que los estados solo avanzan al siguiente.
    private void estadosSoloAvanzan(Verificador v) throws Exception {
        v.grupo("Reclamos: los estados solo avanzan al siguiente");
        Escenario e = new Escenario();
        ControladorReclamos c = new ControladorReclamos(e.barrio);
        Reclamo r = c.presentarReclamo(e.titular1, e.lote1, TipoReclamo.MANTENIMIENTO, "caneria");

        Exception error = null;
        try {
            r.cambiarEstado(EstadoReclamo.EN_CURSO, "salto un estado", e.barrio.getAhora());
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Un reclamo ABIERTO no puede pasar directo a EN_CURSO", error);

        c.asignarReclamo(e.admin, r, e.plomero);
        c.actualizarEstado(e.plomero, r, EstadoReclamo.EN_CURSO, "empece");
        c.actualizarEstado(e.plomero, r, EstadoReclamo.RESUELTO, "listo");
        v.verificarIgual("Avanzando de a uno llega a RESUELTO", EstadoReclamo.RESUELTO, r.getEstado());
        v.verificarIgual("El historial guarda cada cambio", 4, r.consultarHistorial().size());

        error = null;
        try {
            c.actualizarEstado(e.electricista, r, EstadoReclamo.EN_CURSO, "no es mio");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Solo el responsable puede actualizar el reclamo", error);
    }

    // Prueba quien puede presentar reclamos.
    private void quienPuedeReclamar(Verificador v) throws Exception {
        v.grupo("Reclamos: solo el titular o una persona autorizada puede reclamar");
        Escenario e = new Escenario();
        ControladorReclamos c = new ControladorReclamos(e.barrio);

        Exception error = null;
        try {
            c.presentarReclamo(e.titular1, e.lote1, TipoReclamo.CONVIVENCIA, "ruido");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificar("El titular puede reclamar", error == null);

        error = null;
        try {
            c.presentarReclamo(e.residenteAutorizado, e.lote1, TipoReclamo.CONVIVENCIA, "ruido");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificar("Un residente autorizado puede reclamar", error == null);

        error = null;
        try {
            c.presentarReclamo(e.residenteSinAutorizar, e.lote1, TipoReclamo.CONVIVENCIA, "ruido");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Un residente sin autorizar NO puede reclamar", error);

        error = null;
        try {
            c.presentarReclamo(e.ajeno, e.lote1, TipoReclamo.CONVIVENCIA, "ruido");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Una persona ajena al lote NO puede reclamar", error);
        v.verificarIgual("Los reclamos rechazados no se crean", 2, e.lote1.consultarReclamos().size());
    }

    // Prueba el limite de 5 reclamos activos por empleado.
    private void limiteDeCincoReclamos(Verificador v) throws Exception {
        v.grupo("Reclamos: ningun empleado puede tener mas de 5 en curso");
        Escenario e = new Escenario();
        ControladorReclamos c = new ControladorReclamos(e.barrio);
        List<Reclamo> lista = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            lista.add(c.presentarReclamo(e.titular1, e.lote1, TipoReclamo.MANTENIMIENTO, "tarea " + i));
        }
        for (int i = 0; i < 5; i++) {
            c.asignarReclamo(e.admin, lista.get(i), e.plomero);
        }
        v.verificarIgual("La plomera tiene 5 reclamos activos", 5, e.barrio.contarReclamosActivosDe(e.plomero));

        Exception error = null;
        try {
            c.asignarReclamo(e.admin, lista.get(5), e.plomero);
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("El sexto reclamo se rechaza", error);
        v.verificarIgual("El reclamo rechazado sigue ABIERTO", EstadoReclamo.ABIERTO, lista.get(5).getEstado());
    }

    // Crea un reclamo ya resuelto para usar en otras pruebas.
    private Reclamo crearResuelto(Escenario e, ControladorReclamos c) throws Exception {
        Reclamo r = c.presentarReclamo(e.titular1, e.lote1, TipoReclamo.MANTENIMIENTO, "caneria");
        c.asignarReclamo(e.admin, r, e.plomero);
        c.actualizarEstado(e.plomero, r, EstadoReclamo.EN_CURSO, "en camino");
        c.actualizarEstado(e.plomero, r, EstadoReclamo.RESUELTO, "resuelto");
        return r;
    }

    // Prueba la reapertura y el cierre de reclamos.
    private void reaperturaYCierre(Verificador v) throws Exception {
        v.grupo("Reclamos: reabrir dentro de 5 dias y cierre automatico");
        Escenario e = new Escenario();
        ControladorReclamos c = new ControladorReclamos(e.barrio);

        Reclamo dentroDelPlazo = crearResuelto(e, c);
        Reclamo fueraDelPlazo = crearResuelto(e, c);

        e.barrio.avanzarHoras(5 * 24);
        c.reabrirReclamo(e.titular1, dentroDelPlazo);
        v.verificarIgual("A los 5 dias todavia se puede reabrir (queda EN_CURSO)", EstadoReclamo.EN_CURSO,
                dentroDelPlazo.getEstado());

        e.barrio.avanzarHoras(1);
        Exception error = null;
        try {
            c.reabrirReclamo(e.titular1, fueraDelPlazo);
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Pasados los 5 dias ya no se puede reabrir", error);

        c.ejecutarReglasAutomaticas();
        v.verificarIgual("El sistema cierra solo el reclamo resuelto vencido", EstadoReclamo.CERRADO,
                fueraDelPlazo.getEstado());
    }

    // Prueba el escalado de reclamos de prioridad alta.
    private void escalado(Verificador v) throws Exception {
        v.grupo("Reclamos: escalado al administrador a las 24 horas");
        Escenario e = new Escenario();
        ControladorReclamos c = new ControladorReclamos(e.barrio);
        Reclamo alta = c.presentarReclamo(e.titular1, e.lote1, TipoReclamo.SEGURIDAD, "robo");
        Reclamo baja = c.presentarReclamo(e.titular1, e.lote1, TipoReclamo.CONVIVENCIA, "ruido");

        e.barrio.avanzarHoras(23);
        v.verificarIgual("A las 23 horas no se escala nada", 0, c.ejecutarReglasAutomaticas().size());
        e.barrio.avanzarHoras(2);
        v.verificarIgual("A las 25 horas se escala un reclamo", 1, c.ejecutarReglasAutomaticas().size());
        v.verificar("Se escala el de prioridad ALTA", alta.isEscalado());
        v.verificar("No se escala el de prioridad BAJA", !baja.isEscalado());
        v.verificarIgual("El administrador recibe la notificacion", 1,
                e.barrio.consultarNotificaciones(e.admin).size());
    }

    // Prueba el orden de los reclamos.
    private void ordenNatural(Verificador v) throws Exception {
        v.grupo("Reclamos: orden natural (Comparable) por numero");
        Escenario e = new Escenario();
        ControladorReclamos c = new ControladorReclamos(e.barrio);
        Reclamo r1 = c.presentarReclamo(e.titular1, e.lote1, TipoReclamo.CONVIVENCIA, "uno");
        Reclamo r2 = c.presentarReclamo(e.titular1, e.lote1, TipoReclamo.CONVIVENCIA, "dos");
        List<Reclamo> lista = new ArrayList<>();
        lista.add(r2);
        lista.add(r1);
        Collections.sort(lista);
        v.verificarIgual("Despues de ordenar, el primero es el reclamo 1", r1, lista.get(0));
    }
}
