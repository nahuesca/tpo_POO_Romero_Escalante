package aromos.controladores;

import aromos.excepciones.ReglaNegocioException;
import aromos.personas.Barrio;
import aromos.personas.EmpleadoMantenimiento;
import aromos.personas.Guardia;
import aromos.personas.Lote;
import aromos.personas.Puesto;
import aromos.personas.Vecino;

// Controlador de las altas de vecinos, lotes, residentes y personal, y del avance del reloj del sistema.
public class ControladorBarrio {
    private Barrio barrio;

    public ControladorBarrio(Barrio barrio) {
        this.barrio = barrio;
    }

    // Da de alta un vecino validando que los datos obligatorios no esten vacios.
    public Vecino registrarVecino(String nombre, String apellido, String documento, String telefono, String correo)
            throws ReglaNegocioException {
        controlarDatosPersona(nombre, apellido, documento);
        Vecino vecino = new Vecino(nombre.trim(), apellido.trim(), documento.trim(), telefono.trim(),
                correo.trim(), false);
        barrio.registrarVecino(vecino);
        return vecino;
    }

    // Crea un lote nuevo a nombre de un titular que ya este registrado como vecino.
    public Lote crearLote(int numero, double superficie, String documentoTitular) throws ReglaNegocioException {
        Vecino titular = barrio.buscarVecino(documentoTitular.trim());
        if (titular == null) {
            throw new ReglaNegocioException("No hay un vecino con ese documento. Registrelo primero.");
        }
        return barrio.crearLote(numero, superficie, titular);
    }

    // Agrega a un vecino ya registrado como residente de un lote.
    public Vecino agregarResidente(Lote lote, String documentoResidente) throws ReglaNegocioException {
        Vecino residente = barrio.buscarVecino(documentoResidente.trim());
        if (residente == null) {
            throw new ReglaNegocioException("No hay un vecino con ese documento. Registrelo primero.");
        }
        lote.agregarResidente(residente);
        return residente;
    }

    // Hace que el titular autorice (o deje de autorizar) a un residente a presentar reclamos.
    public void autorizarReclamante(Vecino quienAutoriza, Lote lote, Vecino residente, boolean autorizado)
            throws ReglaNegocioException {
        lote.autorizarReclamante(quienAutoriza, residente, autorizado);
    }

    // Da de alta un guardia de seguridad.
    public Guardia registrarGuardia(String nombre, String apellido, String documento, int legajo, String turno,
                                    Puesto puesto) throws ReglaNegocioException {
        controlarDatosPersona(nombre, apellido, documento);
        Guardia guardia = new Guardia(nombre.trim(), apellido.trim(), documento.trim(), "", "", legajo,
                barrio.hoy(), turno.trim(), puesto);
        barrio.registrarGuardia(guardia);
        return guardia;
    }

    // Da de alta un empleado de mantenimiento.
    public EmpleadoMantenimiento registrarEmpleado(String nombre, String apellido, String documento, int legajo,
                                                   String especialidad) throws ReglaNegocioException {
        controlarDatosPersona(nombre, apellido, documento);
        EmpleadoMantenimiento empleado = new EmpleadoMantenimiento(nombre.trim(), apellido.trim(),
                documento.trim(), "", "", legajo, barrio.hoy(), especialidad.trim());
        barrio.registrarEmpleado(empleado);
        return empleado;
    }

    // Adelanta el reloj simulado del sistema una cantidad de horas, sin permitir retroceder.
    public void avanzarTiempo(int horas) throws ReglaNegocioException {
        if (horas < 0) {
            throw new ReglaNegocioException("Solo se puede avanzar hacia adelante.");
        }
        barrio.avanzarHoras(horas);
    }

    // Verifica que nombre, apellido y documento no esten vacios.
    private void controlarDatosPersona(String nombre, String apellido, String documento)
            throws ReglaNegocioException {
        if (nombre.trim().isEmpty() || apellido.trim().isEmpty() || documento.trim().isEmpty()) {
            throw new ReglaNegocioException("Hacen falta el nombre, el apellido y el documento");
        }
    }
}
