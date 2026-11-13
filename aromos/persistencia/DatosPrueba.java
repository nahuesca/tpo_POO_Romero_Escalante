package aromos.persistencia;

import aromos.controladores.ControladorExpensas;
import aromos.controladores.ControladorMantenimiento;
import aromos.controladores.ControladorReclamos;
import aromos.controladores.ControladorVisitas;
import aromos.excepciones.ReglaNegocioException;
import aromos.expensas.Expensa;
import aromos.expensas.MedioPago;
import aromos.mantenimiento.Sector;
import aromos.personas.Administrador;
import aromos.personas.Barrio;
import aromos.personas.EmpleadoMantenimiento;
import aromos.personas.Guardia;
import aromos.personas.Lote;
import aromos.personas.Puesto;
import aromos.personas.TipoVisitante;
import aromos.personas.Vecino;
import aromos.reclamos.Reclamo;
import aromos.reclamos.TipoReclamo;
import java.time.LocalDate;
import java.util.List;

// Datos de ejemplo. Se cargan la primera vez que se ejecuta el programa
// (cuando todavia no existe el archivo de datos).
public class DatosPrueba {

    public static Barrio crear() {
        try {
            return armarBarrio();
        } catch (ReglaNegocioException e) {
            throw new IllegalStateException("Los datos de prueba tienen un error: " + e.getMessage());
        }
    }

    private static Barrio armarBarrio() throws ReglaNegocioException {
        LocalDate hoy = LocalDate.now();
        Barrio barrio = new Barrio("Los Aromos", hoy);

        // ----- personal -----
        barrio.designarAdministrador(new Administrador("Marta", "Gomez", "20111222", "1155550001",
                "marta@losaromos.com", 1, hoy.minusYears(5)));
        barrio.registrarGuardia(new Guardia("Juan", "Perez", "25111001", "1155550011", "juan@losaromos.com",
                11, hoy.minusYears(3), "manana", Puesto.PORTERIA));
        barrio.registrarGuardia(new Guardia("Luis", "Diaz", "25111002", "1155550012", "luis@losaromos.com",
                12, hoy.minusYears(2), "noche", Puesto.RONDA));
        EmpleadoMantenimiento plomera = new EmpleadoMantenimiento("Ana", "Suarez", "27111002", "1155550022",
                "ana@losaromos.com", 22, hoy.minusYears(2), "plomeria");
        barrio.registrarEmpleado(new EmpleadoMantenimiento("Carlos", "Rios", "27111001", "1155550021",
                "carlos@losaromos.com", 21, hoy.minusYears(4), "electricidad"));
        barrio.registrarEmpleado(plomera);
        barrio.registrarEmpleado(new EmpleadoMantenimiento("Pedro", "Luna", "27111003", "1155550023",
                "pedro@losaromos.com", 23, hoy.minusYears(1), "jardineria"));

        // ----- vecinos y lotes -----
        String[] nombres = {"Laura", "Diego", "Sofia", "Martin", "Julia", "Pablo", "Valeria", "Nicolas", "Camila", "Federico"};
        String[] apellidos = {"Fernandez", "Martinez", "Lopez", "Garcia", "Sanchez", "Romero", "Torres", "Alvarez", "Ruiz", "Molina"};
        double[] superficies = {600, 650, 700, 800, 600, 900, 750, 600, 1000, 850};
        Vecino[] titulares = new Vecino[nombres.length];
        for (int i = 0; i < nombres.length; i++) {
            titulares[i] = new Vecino(nombres[i], apellidos[i], String.valueOf(30000001 + i),
                    "11555" + (50100 + i), nombres[i].toLowerCase() + "@mail.com", true);
            barrio.registrarVecino(titulares[i]);
            barrio.crearLote(i + 1, superficies[i], titulares[i]);
        }
        // una misma persona puede ser titular de mas de un lote
        barrio.crearLote(11, 700, titulares[0]);
        barrio.crearLote(12, 500, titulares[0]);

        // residentes del lote 1 (familiar autorizado) y del lote 2 (inquilina sin autorizar)
        Vecino familiar = new Vecino("Marcos", "Fernandez", "30000011", "1155550111", "marcos@mail.com", true);
        Vecino inquilina = new Vecino("Elena", "Vega", "30000012", "1155550112", "elena@mail.com", false);
        barrio.registrarVecino(familiar);
        barrio.registrarVecino(inquilina);
        barrio.buscarLote(1).agregarResidente(familiar);
        barrio.buscarLote(2).agregarResidente(inquilina);

        // ----- expensas de los ultimos 5 meses y pagos -----
        ControladorExpensas expensas = new ControladorExpensas(barrio);
        for (int atras = 4; atras >= 0; atras--) {
            LocalDate mes = hoy.minusMonths(atras);
            expensas.emitirExpensas(mes.getYear(), mes.getMonthValue(), 2000000);
        }
        for (Lote lote : barrio.consultarLotes()) {
            if (lote.getNumero() == 5) {
                continue; // el lote 5 no paga nada: queda moroso
            }
            List<Expensa> lista = lote.obtenerExpensas();
            for (int i = 0; i < lista.size(); i++) {
                if (lote.getNumero() == 6 && i >= 2) {
                    break; // el lote 6 solo pago los dos primeros meses
                }
                // paga el dia 5 de cada mes (si ese dia todavia no llego, no paga)
                LocalDate fechaPago = hoy.minusMonths(4 - i).withDayOfMonth(5);
                if (!fechaPago.isAfter(hoy)) {
                    expensas.registrarPago(lote, lista.get(i).getImporte(), MedioPago.TRANSFERENCIA, fechaPago);
                }
            }
        }
        expensas.verificarMorosidad(hoy);

        // ----- equipos -----
        ControladorMantenimiento mantenimiento = new ControladorMantenimiento(barrio);
        LocalDate instalacion = hoy.minusYears(1);
        mantenimiento.registrarCamara("CAM-01", Sector.ACCESOS, instalacion, "1080p", 30);
        mantenimiento.registrarCamara("CAM-02", Sector.CALLES_INTERNAS, instalacion, "1080p", 15);
        mantenimiento.registrarCamara("CAM-03", Sector.ESPACIOS_COMUNES, instalacion, "4K", 30);
        for (int i = 1; i <= 5; i++) {
            mantenimiento.registrarLuminaria("LUM-0" + i, Sector.CALLES_INTERNAS, instalacion, "LED", 60);
        }
        mantenimiento.registrarLuminaria("LUM-06", Sector.ACCESOS, instalacion, "LED", 100);
        mantenimiento.registrarLuminaria("LUM-07", Sector.ESPACIOS_COMUNES, instalacion, "Sodio", 150);
        mantenimiento.reportarFalla("LUM-03");

        // ----- reclamos -----
        ControladorReclamos reclamos = new ControladorReclamos(barrio);
        reclamos.presentarReclamo(titulares[0], barrio.buscarLote(1), TipoReclamo.SEGURIDAD,
                "Falta iluminacion en la esquina del lote 1");
        Reclamo perdida = reclamos.presentarReclamo(titulares[1], barrio.buscarLote(2), TipoReclamo.MANTENIMIENTO,
                "Perdida de agua en la vereda");
        reclamos.asignarReclamo(barrio.getAdministrador(), perdida, plomera);

        // ----- autorizaciones de visita (valen todo el dia de hoy, de 0 a 23 hs) -----
        ControladorVisitas visitas = new ControladorVisitas(barrio);
        visitas.autorizarVisita(titulares[0], barrio.buscarLote(1), "Roberto", "Paz", "40111222",
                TipoVisitante.PARTICULAR, hoy, hoy, 0, 23, false);
        visitas.autorizarVisita(titulares[0], barrio.buscarLote(1), "Ricardo", "Gas", "40222333",
                TipoVisitante.PROVEEDOR_HABITUAL, hoy, hoy.plusDays(29), 0, 23, true);
        return barrio;
    }
}
