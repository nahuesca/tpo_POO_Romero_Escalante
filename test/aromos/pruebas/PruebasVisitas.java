package aromos.pruebas;

import aromos.controladores.ControladorVisitas;
import aromos.personas.TipoVisitante;
import java.time.LocalDate;

// El reloj de las pruebas arranca el 2026-03-02 a las 9 hs.
public class PruebasVisitas {
    private static final LocalDate HOY = LocalDate.of(2026, 3, 2);

    public void ejecutar(Verificador v) throws Exception {
        ingresoYEgreso(v);
        sinAutorizacion(v);
        ingresoSinEgreso(v);
        autorizacionesRecurrentes(v);
    }

    // Autorizacion para hoy, de 9 a 18 hs
    private void autorizarParticular(Escenario e, ControladorVisitas c, String documento) throws Exception {
        c.autorizarVisita(e.titular1, e.lote1, "Roberto", "Paz", documento, TipoVisitante.PARTICULAR,
                HOY, HOY, 9, 18, false);
    }

    private void ingresoYEgreso(Verificador v) throws Exception {
        v.grupo("Visitas: ingreso y egreso con autorizacion vigente");
        Escenario e = new Escenario();
        ControladorVisitas c = new ControladorVisitas(e.barrio);
        autorizarParticular(e, c, "4001");

        c.registrarIngreso(e.porteria, "4001");
        v.verificarIgual("Se registra la hora de ingreso", 9, e.barrio.consultarRegistros().get(0).getHoraIngreso());
        v.verificarIgual("La persona figura adentro", 1, c.consultarIngresosAbiertos().size());

        e.barrio.avanzarHoras(1);
        c.registrarEgreso(e.porteria, "4001");
        v.verificarIgual("Se registra la hora de egreso", 10, e.barrio.consultarRegistros().get(0).getHoraEgreso());
        v.verificarIgual("Ya no figura adentro", 0, c.consultarIngresosAbiertos().size());
    }

    private void sinAutorizacion(Verificador v) throws Exception {
        v.grupo("Visitas: sin autorizacion vigente no se habilita el ingreso");
        Escenario e = new Escenario();
        ControladorVisitas c = new ControladorVisitas(e.barrio);
        autorizarParticular(e, c, "4001");

        Exception error = null;
        try {
            c.registrarIngreso(e.porteria, "9999");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Un documento sin autorizacion no entra", error);

        error = null;
        try {
            c.registrarIngreso(e.ronda, "4001");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Solo el guardia de porteria registra ingresos", error);

        e.barrio.avanzarHoras(11); // ahora son las 20 hs y la autorizacion es de 9 a 18
        error = null;
        try {
            c.registrarIngreso(e.porteria, "4001");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Fuera de la franja horaria no entra", error);

        error = null;
        try {
            c.autorizarVisita(e.ajeno, e.lote1, "Roberto", "Paz", "4002", TipoVisitante.PARTICULAR,
                    HOY, HOY, 9, 18, false);
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Una persona ajena al lote no puede autorizar visitas", error);
    }

    private void ingresoSinEgreso(Verificador v) throws Exception {
        v.grupo("Visitas: no se puede ingresar dos veces sin haber salido");
        Escenario e = new Escenario();
        ControladorVisitas c = new ControladorVisitas(e.barrio);
        autorizarParticular(e, c, "4001");
        c.registrarIngreso(e.porteria, "4001");

        Exception error = null;
        try {
            c.registrarIngreso(e.porteria, "4001");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("El segundo ingreso queda bloqueado", error);

        c.registrarEgreso(e.porteria, "4001");
        error = null;
        try {
            c.registrarIngreso(e.porteria, "4001");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificar("Despues de registrar el egreso puede volver a entrar", error == null);
    }

    private void autorizacionesRecurrentes(Verificador v) throws Exception {
        v.grupo("Visitas: proveedores habituales con autorizacion de hasta 30 dias");
        Escenario e = new Escenario();
        ControladorVisitas c = new ControladorVisitas(e.barrio);

        Exception error = null;
        try {
            c.autorizarVisita(e.titular1, e.lote1, "Ricardo", "Gas", "5001", TipoVisitante.PROVEEDOR_HABITUAL,
                    HOY, HOY.plusDays(29), 8, 18, true);
        } catch (Exception ex) {
            error = ex;
        }
        v.verificar("30 dias es valido", error == null);

        error = null;
        try {
            c.autorizarVisita(e.titular1, e.lote1, "Ricardo", "Gas", "5002", TipoVisitante.PROVEEDOR_HABITUAL,
                    HOY, HOY.plusDays(30), 8, 18, true);
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("31 dias supera el maximo", error);

        error = null;
        try {
            c.autorizarVisita(e.titular1, e.lote1, "Pepe", "Gomez", "5003", TipoVisitante.PARTICULAR,
                    HOY, HOY.plusDays(5), 8, 18, true);
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Solo los proveedores habituales pueden tener autorizacion recurrente", error);
    }
}
