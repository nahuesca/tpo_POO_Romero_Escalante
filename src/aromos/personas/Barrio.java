package aromos.personas;

import aromos.excepciones.ReglaNegocioException;
import aromos.expensas.PresupuestoMensual;
import aromos.mantenimiento.Equipo;
import aromos.mantenimiento.OrdenMantenimiento;
import aromos.notificaciones.Notificacion;
import aromos.reclamos.EstadoReclamo;
import aromos.reclamos.Reclamo;
import aromos.visitas.Autorizacion;
import aromos.visitas.RegistroAcceso;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Clase principal del modelo: guarda lotes, personas, reclamos, equipos y el reloj simulado del sistema.
public class Barrio implements Serializable {
    public static final int MAX_LOTES = 150;
    public static final int HORA_INICIAL = 9;

    private String nombre;
    private Administrador administrador;
    private List<Lote> lotes = new ArrayList<>();
    private List<Vecino> vecinos = new ArrayList<>();
    private List<Guardia> guardias = new ArrayList<>();
    private List<EmpleadoMantenimiento> empleados = new ArrayList<>();
    private List<Visitante> visitantes = new ArrayList<>();
    private List<Equipo> equipos = new ArrayList<>();
    private List<OrdenMantenimiento> ordenes = new ArrayList<>();
    private List<PresupuestoMensual> presupuestos = new ArrayList<>();
    private List<Autorizacion> autorizaciones = new ArrayList<>();
    private List<RegistroAcceso> registros = new ArrayList<>();
    private List<Notificacion> notificaciones = new ArrayList<>();
    private int ultimoNumeroReclamo;
    private int ultimoNumeroOrden;

    private LocalDate fechaInicio;
    private int ahora;

    public Barrio(String nombre, LocalDate fechaInicio) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.ahora = HORA_INICIAL;
    }

    public int getAhora() {
        return ahora;
    }

    // Adelanta el reloj simulado del sistema.
    public void avanzarHoras(int horas) {
        ahora = ahora + horas;
    }

    // Devuelve la fecha actual segun el reloj del sistema.
    public LocalDate hoy() {
        return fechaInicio.plusDays(ahora / 24);
    }

    // Devuelve la hora actual del dia segun el reloj del sistema.
    public int horaDelDia() {
        return ahora % 24;
    }

    // Convierte un numero de horas en un texto como 2026-11-02 09:00.
    public String formatear(int hora) {
        int horaDelDia = hora % 24;
        String texto = fechaInicio.plusDays(hora / 24) + " ";
        if (horaDelDia < 10) {
            texto = texto + "0";
        }
        return texto + horaDelDia + ":00";
    }

    // Crea un lote del barrio y lo agrega a la lista.
    public Lote crearLote(int numero, double superficie, Vecino titular) throws ReglaNegocioException {
        if (lotes.size() >= MAX_LOTES) {
            throw new ReglaNegocioException("El barrio ya tiene los " + MAX_LOTES + " lotes");
        }
        if (buscarLote(numero) != null) {
            throw new ReglaNegocioException("Ya existe el lote " + numero);
        }
        if (superficie <= 0) {
            throw new ReglaNegocioException("La superficie debe ser mayor a cero");
        }
        if (titular == null) {
            throw new ReglaNegocioException("El lote necesita un titular");
        }
        Lote lote = new Lote(numero, superficie, titular, this);
        lotes.add(lote);
        return lote;
    }

    public List<Lote> consultarLotes() {
        return lotes;
    }

    public Lote buscarLote(int numero) {
        for (Lote l : lotes) {
            if (l.getNumero() == numero) {
                return l;
            }
        }
        return null;
    }

    // Devuelve los lotes de los que un vecino es titular.
    public List<Lote> lotesDe(Vecino vecino) {
        List<Lote> resultado = new ArrayList<>();
        for (Lote l : lotes) {
            if (l.esTitularOResidente(vecino)) {
                resultado.add(l);
            }
        }
        return resultado;
    }

    // Suma la superficie de todos los lotes.
    public double calcularSuperficieTotal() {
        double total = 0;
        for (Lote l : lotes) {
            total = total + l.getSuperficie();
        }
        return total;
    }

    // Agrega un vecino y evita documentos repetidos.
    public void registrarVecino(Vecino vecino) throws ReglaNegocioException {
        if (buscarVecino(vecino.getDocumento()) != null) {
            throw new ReglaNegocioException("Ya hay un vecino con documento " + vecino.getDocumento());
        }
        vecinos.add(vecino);
    }

    public Vecino buscarVecino(String documento) {
        for (Vecino v : vecinos) {
            if (v.getDocumento().equals(documento)) {
                return v;
            }
        }
        return null;
    }

    // Agrega un guardia y evita legajos repetidos.
    public void registrarGuardia(Guardia guardia) throws ReglaNegocioException {
        if (buscarGuardia(guardia.getLegajo()) != null) {
            throw new ReglaNegocioException("Ya hay un guardia con legajo " + guardia.getLegajo());
        }
        guardias.add(guardia);
    }

    public Guardia buscarGuardia(int legajo) {
        for (Guardia g : guardias) {
            if (g.getLegajo() == legajo) {
                return g;
            }
        }
        return null;
    }

    // Agrega un empleado de mantenimiento y evita legajos repetidos.
    public void registrarEmpleado(EmpleadoMantenimiento empleado) throws ReglaNegocioException {
        if (buscarEmpleado(empleado.getLegajo()) != null) {
            throw new ReglaNegocioException("Ya hay un empleado con legajo " + empleado.getLegajo());
        }
        empleados.add(empleado);
    }

    public EmpleadoMantenimiento buscarEmpleado(int legajo) {
        for (EmpleadoMantenimiento e : empleados) {
            if (e.getLegajo() == legajo) {
                return e;
            }
        }
        return null;
    }

    public List<EmpleadoMantenimiento> consultarEmpleados() {
        return empleados;
    }

    public void registrarVisitante(Visitante visitante) {
        visitantes.add(visitante);
    }

    public Visitante buscarVisitante(String documento) {
        for (Visitante v : visitantes) {
            if (v.getDocumento().equals(documento)) {
                return v;
            }
        }
        return null;
    }

    // Devuelve el numero que le toca al proximo reclamo.
    public int siguienteNumeroReclamo() {
        ultimoNumeroReclamo++;
        return ultimoNumeroReclamo;
    }

    // Junta los reclamos de todos los lotes.
    public List<Reclamo> consultarTodosLosReclamos() {
        List<Reclamo> resultado = new ArrayList<>();
        for (Lote l : lotes) {
            resultado.addAll(l.consultarReclamos());
        }
        return resultado;
    }

    public Reclamo buscarReclamo(int numero) {
        for (Reclamo r : consultarTodosLosReclamos()) {
            if (r.getNumero() == numero) {
                return r;
            }
        }
        return null;
    }

    // Cuenta los reclamos activos de un integrante del personal.
    public int contarReclamosActivosDe(Personal responsable) {
        int cantidad = 0;
        for (Reclamo r : consultarTodosLosReclamos()) {
            boolean activo = r.getEstado() == EstadoReclamo.ASIGNADO || r.getEstado() == EstadoReclamo.EN_CURSO;
            if (activo && responsable.equals(r.getResponsable())) {
                cantidad++;
            }
        }
        return cantidad;
    }

    // Agrega un equipo y evita codigos repetidos.
    public void registrarEquipo(Equipo equipo) throws ReglaNegocioException {
        if (buscarEquipo(equipo.getCodigo()) != null) {
            throw new ReglaNegocioException("Ya existe un equipo con codigo " + equipo.getCodigo());
        }
        equipos.add(equipo);
    }

    public Equipo buscarEquipo(String codigo) {
        for (Equipo e : equipos) {
            if (e.getCodigo().equalsIgnoreCase(codigo)) {
                return e;
            }
        }
        return null;
    }

    public List<Equipo> consultarEquipos() {
        return equipos;
    }

    // Cuenta los equipos con falla del mismo tipo y sector que otro equipo.
    public int contarEquiposConFalla(Equipo referencia) {
        int cantidad = 0;
        for (Equipo e : equipos) {
            boolean mismoTipo = e.getTipo().equals(referencia.getTipo());
            if (mismoTipo && e.getSector() == referencia.getSector() && !e.estaOperativo()) {
                cantidad++;
            }
        }
        return cantidad;
    }

    // Devuelve el numero que le toca a la proxima orden de mantenimiento.
    public int siguienteNumeroOrden() {
        ultimoNumeroOrden++;
        return ultimoNumeroOrden;
    }

    public void agregarOrden(OrdenMantenimiento orden) {
        ordenes.add(orden);
    }

    public OrdenMantenimiento buscarOrden(int numero) {
        for (OrdenMantenimiento o : ordenes) {
            if (o.getNumero() == numero) {
                return o;
            }
        }
        return null;
    }

    public List<OrdenMantenimiento> consultarOrdenes() {
        return ordenes;
    }

    public void agregarPresupuesto(PresupuestoMensual presupuesto) {
        presupuestos.add(presupuesto);
    }

    public PresupuestoMensual buscarPresupuesto(int anio, int mes) {
        for (PresupuestoMensual p : presupuestos) {
            if (p.getAnio() == anio && p.getMes() == mes) {
                return p;
            }
        }
        return null;
    }

    public void agregarAutorizacion(Autorizacion autorizacion) {
        autorizaciones.add(autorizacion);
    }

    public List<Autorizacion> consultarAutorizaciones() {
        return autorizaciones;
    }

    public void agregarRegistroAcceso(RegistroAcceso registro) {
        registros.add(registro);
    }

    public List<RegistroAcceso> consultarRegistros() {
        return registros;
    }

    // Busca el ingreso de una persona que todavia no salio.
    public RegistroAcceso buscarRegistroAbierto(String documentoVisitante) {
        for (RegistroAcceso r : registros) {
            if (r.estaAbierto() && r.getAutorizacion().getVisitante().getDocumento().equals(documentoVisitante)) {
                return r;
            }
        }
        return null;
    }

    // Guarda un aviso para una persona.
    public void notificar(Persona destinatario, String mensaje) {
        notificaciones.add(new Notificacion(ahora, destinatario, mensaje));
    }

    // Devuelve los avisos de una persona.
    public List<Notificacion> consultarNotificaciones(Persona destinatario) {
        List<Notificacion> resultado = new ArrayList<>();
        for (Notificacion n : notificaciones) {
            if (n.getDestinatario().equals(destinatario)) {
                resultado.add(n);
            }
        }
        return resultado;
    }

    // Define quien es el administrador del barrio.
    public void designarAdministrador(Administrador administrador) {
        this.administrador = administrador;
    }

    public String getNombre() { return nombre; }
    public Administrador getAdministrador() { return administrador; }
}
