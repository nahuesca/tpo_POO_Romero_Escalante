package aromos.ui;

import aromos.controladores.ControladorBarrio;
import aromos.controladores.ControladorExpensas;
import aromos.controladores.ControladorMantenimiento;
import aromos.controladores.ControladorReclamos;
import aromos.controladores.ControladorReportes;
import aromos.excepciones.ReglaNegocioException;
import aromos.expensas.MedioPago;
import aromos.expensas.Pago;
import aromos.mantenimiento.Equipo;
import aromos.mantenimiento.EstadoEquipo;
import aromos.mantenimiento.OrdenMantenimiento;
import aromos.mantenimiento.Sector;
import aromos.personas.Administrador;
import aromos.personas.Barrio;
import aromos.personas.EmpleadoMantenimiento;
import aromos.personas.Lote;
import aromos.personas.Puesto;
import aromos.personas.Vecino;
import aromos.reclamos.Reclamo;
import java.time.LocalDate;
import java.util.List;

public class MenuAdministrador extends MenuBase {
    private Administrador administrador;
    private ControladorReclamos reclamos;
    private ControladorExpensas expensas;
    private ControladorMantenimiento mantenimiento;
    private ControladorReportes reportes;
    private ControladorBarrio gestion;

    public MenuAdministrador(Barrio barrio, Consola consola, Administrador administrador) {
        super(barrio, consola);
        this.administrador = administrador;
        this.reclamos = new ControladorReclamos(barrio);
        this.expensas = new ControladorExpensas(barrio);
        this.mantenimiento = new ControladorMantenimiento(barrio);
        this.reportes = new ControladorReportes(barrio);
        this.gestion = new ControladorBarrio(barrio);
    }

    @Override
    public String titulo() {
        return "ADMINISTRADOR: " + administrador.getNombre() + " " + administrador.getApellido();
    }

    @Override
    public String[] opciones() {
        return new String[] {
            "REPORTE: reclamos pendientes",
            "Asignar un reclamo a un empleado",
            "Cerrar un reclamo resuelto",
            "Ver el detalle de un reclamo",
            "Emitir las expensas de un mes",
            "Registrar el pago de un lote",
            "Corregir un pago (asiento de ajuste)",
            "Consultar las deudas de un lote",
            "REPORTE: morosidad por lote",
            "Registrar una camara o luminaria",
            "Ver estado de luminarias y camaras",
            "Generar una orden de mantenimiento",
            "Asignar una orden de mantenimiento a un empleado",
            "Cambiar el estado de un equipo",
            "REPORTE: equipos fuera de servicio",
            "Altas: vecinos, lotes, residentes y personal",
            "Ver mis notificaciones"
        };
    }

    @Override
    public void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1: reportePendientes(); break;
            case 2: asignarReclamo(); break;
            case 3: cerrarReclamo(); break;
            case 4: verDetalleReclamo(); break;
            case 5: emitirExpensas(); break;
            case 6: registrarPago(); break;
            case 7: corregirPago(); break;
            case 8: consultarDeudasDeLote(); break;
            case 9: reporteMorosidad(); break;
            case 10: registrarEquipo(); break;
            case 11: mostrarEstadoEquipos(); break;
            case 12: generarOrden(); break;
            case 13: asignarOrden(); break;
            case 14: cambiarEstadoEquipo(); break;
            case 15: reporteEquipos(); break;
            case 16: menuAltas(); break;
            case 17: mostrarNotificaciones(administrador); break;
            default: break;
        }
    }

    // ---------- reclamos ----------

    private void reportePendientes() {
        consola.titulo("Reclamos pendientes (primero los de mayor prioridad)");
        consola.listar("no hay reclamos pendientes", reportes.reclamosPendientes());
    }

    private void asignarReclamo() {
        Reclamo reclamo = pedirReclamo();
        if (reclamo == null) {
            return;
        }
        List<EmpleadoMantenimiento> empleados = barrio.consultarEmpleados();
        EmpleadoMantenimiento empleado = empleados.get(consola.elegir("Empleados", empleados.toArray()));
        try {
            reclamos.asignarReclamo(administrador, reclamo, empleado);
            consola.ok("Reclamo #" + reclamo.getNumero() + " asignado a " + empleado.getNombre() + " "
                    + empleado.getApellido() + ".");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void cerrarReclamo() {
        Reclamo reclamo = pedirReclamo();
        if (reclamo == null) {
            return;
        }
        String observacion = consola.leerTexto("Observacion de cierre");
        try {
            reclamos.cerrarReclamo(reclamo, observacion);
            consola.ok("Reclamo #" + reclamo.getNumero() + " cerrado.");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void verDetalleReclamo() {
        Reclamo reclamo = pedirReclamo();
        if (reclamo != null) {
            mostrarDetalleReclamo(reclamo);
        }
    }

    // ---------- expensas ----------

    private void emitirExpensas() {
        int anio = consola.leerEntero("Anio (ejemplo: " + barrio.hoy().getYear() + ")");
        int mes = consola.leerEntero("Mes (1 a 12)");
        double presupuesto = consola.leerNumero("Presupuesto total del mes");
        try {
            int cantidad = expensas.emitirExpensas(anio, mes, presupuesto);
            consola.ok("Se emitieron " + cantidad + " expensas (vencen el dia 10).");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void registrarPago() {
        Lote lote = pedirLote();
        if (lote == null) {
            return;
        }
        mostrarDeudas(lote);
        double importe = consola.leerNumero("Importe del pago");
        MedioPago medio = MedioPago.values()[consola.elegir("Medio de pago", MedioPago.values())];
        LocalDate fecha = consola.leerFecha("Fecha del pago", barrio.hoy());
        try {
            Pago pago = expensas.registrarPago(lote, importe, medio, fecha);
            consola.ok("Pago registrado: " + pago);
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void corregirPago() {
        Lote lote = pedirLote();
        if (lote == null) {
            return;
        }
        List<Pago> pagos = lote.obtenerPagos();
        if (pagos.isEmpty()) {
            consola.aviso("El lote " + lote.getNumero() + " no tiene pagos registrados.");
            return;
        }
        Pago pago = pagos.get(consola.elegir("Pagos del lote " + lote.getNumero(), pagos.toArray()));
        double diferencia = consola.leerNumero("Diferencia a ajustar (negativa si el pago era menor)");
        String motivo = consola.leerTexto("Motivo del ajuste");
        try {
            expensas.corregirPago(lote, pago, diferencia, motivo, barrio.hoy());
            consola.ok("Ajuste registrado. El pago no se elimino: " + pago);
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void consultarDeudasDeLote() {
        Lote lote = pedirLote();
        if (lote != null) {
            mostrarDeudas(lote);
        }
    }

    private void reporteMorosidad() {
        LocalDate hoy = barrio.hoy();
        consola.titulo("Morosidad por lote (primero los que mas deben)");
        List<Lote> lista = reportes.morosidadPorLote(hoy);
        if (lista.isEmpty()) {
            consola.linea("(no hay lotes con deuda vencida)");
        }
        for (Lote lote : lista) {
            String texto = " - Lote " + lote.getNumero() + " (" + lote.consultarTitular().getNombre() + " "
                    + lote.consultarTitular().getApellido() + "): deuda vencida $" + lote.calcularDeudaVencida(hoy)
                    + ", " + lote.contarPeriodosImpagos(hoy) + " periodos impagos";
            if (lote.isMoroso()) {
                texto = texto + "  *** MOROSO ***";
            }
            consola.linea(texto);
        }
    }

    // ---------- mantenimiento ----------

    private void registrarEquipo() {
        String[] tipos = {"Camara", "Luminaria"};
        int tipo = consola.elegir("Tipo de equipo", tipos);
        String codigo = consola.leerTexto("Codigo (ejemplo: CAM-04)");
        Sector sector = Sector.values()[consola.elegir("Sector", Sector.values())];
        LocalDate instalacion = consola.leerFecha("Fecha de instalacion", barrio.hoy());
        try {
            if (tipo == 0) {
                String resolucion = consola.leerTexto("Resolucion (ejemplo: 1080p)");
                int dias = consola.leerEntero("Dias de grabacion que almacena");
                mantenimiento.registrarCamara(codigo, sector, instalacion, resolucion, dias);
            } else {
                String lampara = consola.leerTexto("Tipo de lampara (ejemplo: LED)");
                double potencia = consola.leerNumero("Potencia en watts");
                mantenimiento.registrarLuminaria(codigo, sector, instalacion, lampara, potencia);
            }
            consola.ok("Equipo " + codigo + " registrado.");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void generarOrden() {
        consola.titulo("Equipos con falla y sin orden de mantenimiento");
        List<Equipo> pendientes = mantenimiento.consultarEquiposSinOrden();
        consola.listar("no hay equipos esperando una orden", pendientes);
        if (pendientes.isEmpty()) {
            return;
        }
        Equipo equipo = pedirEquipo();
        if (equipo == null) {
            return;
        }
        String descripcion = consola.leerTexto("Descripcion del trabajo a realizar");
        try {
            OrdenMantenimiento orden = mantenimiento.generarOrden(equipo.getCodigo(), descripcion);
            consola.ok("Generada: " + orden);
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void asignarOrden() {
        consola.titulo("Ordenes de mantenimiento");
        consola.listar("no hay ordenes", barrio.consultarOrdenes());
        int numero = consola.leerEntero("Numero de orden a asignar (0 = cancelar)");
        if (numero == 0) {
            return;
        }
        List<EmpleadoMantenimiento> empleados = barrio.consultarEmpleados();
        EmpleadoMantenimiento empleado = empleados.get(consola.elegir("Empleados", empleados.toArray()));
        try {
            mantenimiento.asignarOrden(numero, empleado);
            consola.ok("Orden #" + numero + " asignada a " + empleado.getNombre() + " " + empleado.getApellido() + ".");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void cambiarEstadoEquipo() {
        Equipo equipo = pedirEquipo();
        if (equipo == null) {
            return;
        }
        EstadoEquipo nuevo = EstadoEquipo.values()[consola.elegir("Nuevo estado", EstadoEquipo.values())];
        try {
            mantenimiento.cambiarEstadoEquipo(equipo.getCodigo(), nuevo);
            consola.ok(equipo.getCodigo() + " ahora esta " + equipo.getEstado() + ".");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void reporteEquipos() {
        consola.titulo("Equipos fuera de servicio (con falla, en reparacion o fuera de servicio)");
        consola.listar("todos los equipos estan operativos", reportes.equiposFueraDeServicio());
    }

    // ---------- altas ----------

    private void menuAltas() {
        String[] opciones = {"Registrar un vecino", "Crear un lote", "Agregar un residente a un lote",
            "Registrar un guardia", "Registrar un empleado de mantenimiento", "Ver los lotes del barrio"};
        int opcion;
        do {
            opcion = consola.menu("ALTAS", opciones);
            switch (opcion) {
                case 1: registrarVecino(); break;
                case 2: crearLote(); break;
                case 3: agregarResidente(); break;
                case 4: registrarGuardia(); break;
                case 5: registrarEmpleado(); break;
                case 6: consola.listar("no hay lotes", barrio.consultarLotes()); break;
                default: break;
            }
        } while (opcion != 0);
    }

    private void registrarVecino() {
        String nombre = consola.leerTexto("Nombre");
        String apellido = consola.leerTexto("Apellido");
        String documento = consola.leerTexto("Documento");
        String telefono = consola.leerTexto("Telefono");
        String correo = consola.leerTexto("Correo electronico");
        try {
            gestion.registrarVecino(nombre, apellido, documento, telefono, correo);
            consola.ok("Vecino registrado.");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void crearLote() {
        int numero = consola.leerEntero("Numero de lote");
        double superficie = consola.leerNumero("Superficie en m2");
        String documento = consola.leerTexto("Documento del titular (tiene que estar registrado como vecino)");
        try {
            Lote lote = gestion.crearLote(numero, superficie, documento);
            consola.ok("Lote creado: " + lote);
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void agregarResidente() {
        Lote lote = pedirLote();
        if (lote == null) {
            return;
        }
        String documento = consola.leerTexto("Documento del residente (familiar o inquilino, ya registrado como vecino)");
        try {
            Vecino residente = gestion.agregarResidente(lote, documento);
            consola.ok(residente.getNombre() + " " + residente.getApellido() + " es residente del lote "
                    + lote.getNumero() + ".");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void registrarGuardia() {
        String nombre = consola.leerTexto("Nombre");
        String apellido = consola.leerTexto("Apellido");
        String documento = consola.leerTexto("Documento");
        int legajo = consola.leerEntero("Legajo");
        String turno = consola.leerTexto("Turno (manana, tarde o noche)");
        Puesto puesto = Puesto.values()[consola.elegir("Puesto", Puesto.values())];
        try {
            gestion.registrarGuardia(nombre, apellido, documento, legajo, turno, puesto);
            consola.ok("Guardia registrado.");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void registrarEmpleado() {
        String nombre = consola.leerTexto("Nombre");
        String apellido = consola.leerTexto("Apellido");
        String documento = consola.leerTexto("Documento");
        int legajo = consola.leerEntero("Legajo");
        String especialidad = consola.leerTexto("Especialidad (electricidad, plomeria o jardineria)");
        try {
            gestion.registrarEmpleado(nombre, apellido, documento, legajo, especialidad);
            consola.ok("Empleado registrado.");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }
}
