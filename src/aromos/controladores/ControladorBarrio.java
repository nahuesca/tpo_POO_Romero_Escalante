package aromos.controladores;

import aromos.excepciones.ReglaNegocioException;
import aromos.personas.Barrio;
import aromos.personas.EmpleadoMantenimiento;
import aromos.personas.Guardia;
import aromos.personas.Lote;
import aromos.personas.Puesto;
import aromos.personas.Vecino;

// GRASP Controlador de las tareas de gestion del barrio que hace el administrador:
// dar de alta vecinos, lotes, residentes y personal, y autorizar a un residente a reclamar.
// Tambien avanza el reloj del sistema (para la demostracion).
// Los menus y las ventanas no crean las personas ni cambian el barrio por su cuenta: se lo piden a este controlador.
public class ControladorBarrio {
    private Barrio barrio;

    public ControladorBarrio(Barrio barrio) {
        this.barrio = barrio;
    }

    public Vecino registrarVecino(String nombre, String apellido, String documento, String telefono, String correo)
            throws ReglaNegocioException {
        controlarDatosPersona(nombre, apellido, documento);
        Vecino vecino = new Vecino(nombre.trim(), apellido.trim(), documento.trim(), telefono.trim(),
                correo.trim(), false);
        barrio.registrarVecino(vecino);
        return vecino;
    }

    // El titular tiene que estar registrado como vecino. Es el Barrio quien crea el lote (GRASP Creador).
    public Lote crearLote(int numero, double superficie, String documentoTitular) throws ReglaNegocioException {
        Vecino titular = barrio.buscarVecino(documentoTitular.trim());
        if (titular == null) {
            throw new ReglaNegocioException("No hay un vecino con ese documento. Registrelo primero.");
        }
        return barrio.crearLote(numero, superficie, titular);
    }

    // Un residente es un familiar o inquilino que ya esta registrado como vecino.
    public Vecino agregarResidente(Lote lote, String documentoResidente) throws ReglaNegocioException {
        Vecino residente = barrio.buscarVecino(documentoResidente.trim());
        if (residente == null) {
            throw new ReglaNegocioException("No hay un vecino con ese documento. Registrelo primero.");
        }
        lote.agregarResidente(residente);
        return residente;
    }

    // Solo el titular puede autorizar (o sacar la autorizacion) a un residente. Lo controla el Lote.
    public void autorizarReclamante(Vecino quienAutoriza, Lote lote, Vecino residente, boolean autorizado)
            throws ReglaNegocioException {
        lote.autorizarReclamante(quienAutoriza, residente, autorizado);
    }

    public Guardia registrarGuardia(String nombre, String apellido, String documento, int legajo, String turno,
                                    Puesto puesto) throws ReglaNegocioException {
        controlarDatosPersona(nombre, apellido, documento);
        Guardia guardia = new Guardia(nombre.trim(), apellido.trim(), documento.trim(), "", "", legajo,
                barrio.hoy(), turno.trim(), puesto);
        barrio.registrarGuardia(guardia);
        return guardia;
    }

    public EmpleadoMantenimiento registrarEmpleado(String nombre, String apellido, String documento, int legajo,
                                                   String especialidad) throws ReglaNegocioException {
        controlarDatosPersona(nombre, apellido, documento);
        EmpleadoMantenimiento empleado = new EmpleadoMantenimiento(nombre.trim(), apellido.trim(),
                documento.trim(), "", "", legajo, barrio.hoy(), especialidad.trim());
        barrio.registrarEmpleado(empleado);
        return empleado;
    }

    // Solo se puede avanzar hacia adelante.
    public void avanzarTiempo(int horas) throws ReglaNegocioException {
        if (horas < 0) {
            throw new ReglaNegocioException("Solo se puede avanzar hacia adelante.");
        }
        barrio.avanzarHoras(horas);
    }

    private void controlarDatosPersona(String nombre, String apellido, String documento)
            throws ReglaNegocioException {
        if (nombre.trim().isEmpty() || apellido.trim().isEmpty() || documento.trim().isEmpty()) {
            throw new ReglaNegocioException("Hacen falta el nombre, el apellido y el documento");
        }
    }
}
