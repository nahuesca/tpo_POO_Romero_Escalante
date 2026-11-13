package aromos.ui;

import aromos.controladores.ControladorVisitas;
import aromos.excepciones.ReglaNegocioException;
import aromos.personas.Barrio;
import aromos.personas.Guardia;
import aromos.visitas.Autorizacion;
import aromos.visitas.RegistroAcceso;

// Menu de consola del guardia de seguridad.
public class MenuGuardia extends MenuBase {
    private Guardia guardia;
    private ControladorVisitas visitas;

    public MenuGuardia(Barrio barrio, Consola consola, Guardia guardia) {
        super(barrio, consola);
        this.guardia = guardia;
        this.visitas = new ControladorVisitas(barrio);
    }

    @Override
    public String titulo() {
        return "GUARDIA: " + guardia.getNombre() + " " + guardia.getApellido() + " (" + guardia.getPuesto() + ")";
    }

    // Devuelve las opciones del menu del guardia.
    @Override
    public String[] opciones() {
        return new String[] {
            "Registrar el ingreso de una persona",
            "Registrar el egreso de una persona",
            "Ver las personas que estan dentro del barrio",
            "Ver el historial de accesos",
            "Reportar la falla de una luminaria o camara",
            "Ver estado de luminarias y camaras"
        };
    }

    // Ejecuta la accion de la opcion elegida.
    @Override
    public void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1: registrarIngreso(); break;
            case 2: registrarEgreso(); break;
            case 3: verPersonasAdentro(); break;
            case 4: verHistorial(); break;
            case 5: reportarFallaDeEquipo(); break;
            case 6: mostrarEstadoEquipos(); break;
            default: break;
        }
    }

    // Registra el ingreso de una persona verificando su autorizacion.
    private void registrarIngreso() {
        String documento = consola.leerTexto("Documento de la persona que llega");
        try {
            Autorizacion autorizacion = visitas.buscarAutorizacionVigente(documento);
            consola.linea("Autorizacion vigente: " + autorizacion);
            RegistroAcceso registro = visitas.registrarIngreso(guardia, documento);
            consola.ok("Ingreso habilitado a las " + registro.getHoraIngreso() + " hs.");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    // Registra la salida de una persona.
    private void registrarEgreso() {
        String documento = consola.leerTexto("Documento de la persona que se retira");
        try {
            RegistroAcceso registro = visitas.registrarEgreso(guardia, documento);
            consola.ok("Egreso registrado a las " + registro.getHoraEgreso() + " hs.");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    // Muestra las personas que estan dentro del barrio.
    private void verPersonasAdentro() {
        consola.titulo("Personas dentro del barrio");
        consola.listar("no hay nadie dentro del barrio", visitas.consultarIngresosAbiertos());
    }

    // Muestra el historial de accesos.
    private void verHistorial() {
        consola.titulo("Historial de accesos");
        consola.listar("no hay accesos registrados", barrio.consultarRegistros());
    }
}
