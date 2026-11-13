package aromos.pruebas;

import aromos.controladores.ControladorBarrio;
import aromos.excepciones.NoAutorizadoException;
import aromos.personas.Puesto;
import aromos.personas.Vecino;

// Pruebas de las altas de vecinos, lotes, personal y del reloj del sistema.
public class PruebasGestion {
    // Corre todas las pruebas de gestion.
    public void ejecutar(Verificador v) throws Exception {
        vecinos(v);
        lotes(v);
        residentesYAutorizacion(v);
        personal(v);
        reloj(v);
    }

    // Prueba el alta de vecinos.
    private void vecinos(Verificador v) throws Exception {
        v.grupo("Gestion: alta de vecinos");
        Escenario e = new Escenario();
        ControladorBarrio c = new ControladorBarrio(e.barrio);

        Vecino nuevo = c.registrarVecino("Gabi", "Soto", "2001", "111", "g@mail.com");
        v.verificar("El vecino queda registrado", e.barrio.buscarVecino("2001") == nuevo);
        v.verificar("Un vecino nuevo no esta autorizado a reclamar", !nuevo.isAutorizadoReclamos());

        Exception error = null;
        try {
            c.registrarVecino("Otra", "Persona", "2001", "", "");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("No se puede repetir el documento", error);

        error = null;
        try {
            c.registrarVecino("  ", "Soto", "2002", "", "");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Faltan datos: no se registra", error);
    }

    // Prueba la creacion de lotes.
    private void lotes(Verificador v) throws Exception {
        v.grupo("Gestion: alta de lotes");
        Escenario e = new Escenario();
        ControladorBarrio c = new ControladorBarrio(e.barrio);

        c.crearLote(3, 500, "1003");
        v.verificarIgual("El lote nuevo se crea con su titular", "Carla", e.barrio.buscarLote(3).consultarTitular().getNombre());
        c.crearLote(4, 500, "1003");
        v.verificarIgual("Una misma persona puede ser titular de varios lotes", 2,
                contarLotesDeTitular(e, "1003"));

        Exception error = null;
        try {
            c.crearLote(5, 500, "9999");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("El titular tiene que estar registrado", error);

        error = null;
        try {
            c.crearLote(1, 500, "1002");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("No se puede repetir el numero de lote", error);
    }

    // Cuenta los lotes de un titular.
    private int contarLotesDeTitular(Escenario e, String documento) {
        int cantidad = 0;
        for (aromos.personas.Lote l : e.barrio.consultarLotes()) {
            if (l.consultarTitular().getDocumento().equals(documento)) {
                cantidad++;
            }
        }
        return cantidad;
    }

    // Prueba los residentes y la autorizacion para reclamar.
    private void residentesYAutorizacion(Verificador v) throws Exception {
        v.grupo("Gestion: residentes y autorizacion para reclamar");
        Escenario e = new Escenario();
        ControladorBarrio c = new ControladorBarrio(e.barrio);

        c.registrarVecino("Hugo", "Paz", "4001", "111", "h@mail.com");
        Vecino residente = c.agregarResidente(e.lote2, "4001");
        v.verificar("El vecino pasa a ser residente del lote 2", e.lote2.esTitularOResidente(residente));
        v.verificar("Un residente nuevo no puede reclamar hasta que el titular lo autorice",
                !e.lote2.esTitularOAutorizado(residente));

        c.autorizarReclamante(e.titular2, e.lote2, residente, true);
        v.verificar("Despues de que el titular lo autoriza puede reclamar", e.lote2.esTitularOAutorizado(residente));

        Exception error = null;
        try {
            c.autorizarReclamante(e.residenteAutorizado, e.lote1, e.residenteSinAutorizar, true);
        } catch (Exception ex) {
            error = ex;
        }
        v.verificar("Solo el titular puede autorizar", error instanceof NoAutorizadoException);

        error = null;
        try {
            c.agregarResidente(e.lote2, "9999");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("El residente tiene que estar registrado", error);
    }

    // Prueba el alta de guardias y empleados.
    private void personal(Verificador v) throws Exception {
        v.grupo("Gestion: alta de personal");
        Escenario e = new Escenario();
        ControladorBarrio c = new ControladorBarrio(e.barrio);

        c.registrarGuardia("Nico", "Vega", "3001", 13, "tarde", Puesto.MONITOREO);
        v.verificarIgual("El guardia queda registrado", "Vega", e.barrio.buscarGuardia(13).getApellido());
        c.registrarEmpleado("Pepe", "Luna", "3002", 23, "jardineria");
        v.verificarIgual("El empleado queda registrado", "jardineria", e.barrio.buscarEmpleado(23).getEspecialidad());

        Exception error = null;
        try {
            c.registrarGuardia("Otro", "Guardia", "3003", 13, "noche", Puesto.RONDA);
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("No se puede repetir el legajo de un guardia", error);

        error = null;
        try {
            c.registrarEmpleado("Otro", "Empleado", "3004", 21, "plomeria");
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("No se puede repetir el legajo de un empleado", error);
    }

    // Prueba el avance del reloj del sistema.
    private void reloj(Verificador v) throws Exception {
        v.grupo("Gestion: reloj del sistema");
        Escenario e = new Escenario();
        ControladorBarrio c = new ControladorBarrio(e.barrio);

        int antes = e.barrio.getAhora();
        c.avanzarTiempo(24);
        v.verificarIgual("Avanzar 24 horas suma un dia", antes + 24, e.barrio.getAhora());

        Exception error = null;
        try {
            c.avanzarTiempo(-1);
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("No se puede retroceder", error);
        v.verificarIgual("El reloj no cambio", antes + 24, e.barrio.getAhora());
    }
}
