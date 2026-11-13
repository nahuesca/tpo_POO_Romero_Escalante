package aromos.ui;

import aromos.controladores.ControladorBarrio;
import aromos.controladores.ControladorExpensas;
import aromos.controladores.ControladorMantenimiento;
import aromos.controladores.ControladorReclamos;
import aromos.excepciones.ReglaNegocioException;
import aromos.persistencia.ArchivoDatos;
import aromos.personas.Administrador;
import aromos.personas.Barrio;
import aromos.personas.EmpleadoMantenimiento;
import aromos.personas.Guardia;
import aromos.personas.Vecino;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Menu de entrada: la persona elige su rol, se identifica y se abre el menu de ese rol.
// La identificacion es simple (documento o legajo, sin contrasena).
public class MenuPrincipal {
    private Barrio barrio;
    private ArchivoDatos archivo;
    private Consola consola;

    public MenuPrincipal(Barrio barrio, ArchivoDatos archivo, Consola consola) {
        this.barrio = barrio;
        this.archivo = archivo;
        this.consola = consola;
    }

    public void ejecutar() {
        String[] opciones = {
            "Ingresar como VECINO (con el documento)",
            "Ingresar como ADMINISTRADOR (con el legajo)",
            "Ingresar como GUARDIA (con el legajo)",
            "Ingresar como EMPLEADO DE MANTENIMIENTO (con el legajo)",
            "Simular el paso del tiempo (avanzar horas)",
            "Ejecutar las reglas automaticas del sistema"
        };
        try {
            ejecutarReglasAutomaticas();
            int opcion;
            do {
                opcion = consola.menu("BARRIO " + barrio.getNombre().toUpperCase() + " - "
                        + barrio.formatear(barrio.getAhora()), opciones);
                if (opcion == 1) {
                    ingresarVecino();
                } else if (opcion == 2) {
                    ingresarAdministrador();
                } else if (opcion == 3) {
                    ingresarGuardia();
                } else if (opcion == 4) {
                    ingresarEmpleado();
                } else if (opcion == 5) {
                    avanzarTiempo();
                } else if (opcion == 6) {
                    ejecutarReglasAutomaticas();
                }
                guardar();
            } while (opcion != 0);
        } finally {
            // Pase lo que pase (aunque el programa falle) los datos se guardan.
            guardar();
        }
    }

    private void ingresarVecino() {
        String documento = consola.leerTexto("Documento");
        Vecino vecino = barrio.buscarVecino(documento);
        if (vecino == null) {
            consola.error("No hay ningun vecino con ese documento.");
        } else if (barrio.lotesDe(vecino).isEmpty()) {
            consola.error("El vecino no tiene un lote asociado: no puede ingresar.");
        } else {
            new MenuVecino(barrio, consola, vecino).ejecutar();
        }
    }

    private void ingresarAdministrador() {
        int legajo = consola.leerEntero("Legajo");
        Administrador administrador = barrio.getAdministrador();
        if (administrador.getLegajo() != legajo) {
            consola.error("Legajo de administrador incorrecto.");
        } else {
            new MenuAdministrador(barrio, consola, administrador).ejecutar();
        }
    }

    private void ingresarGuardia() {
        Guardia guardia = barrio.buscarGuardia(consola.leerEntero("Legajo"));
        if (guardia == null) {
            consola.error("No hay ningun guardia con ese legajo.");
        } else {
            new MenuGuardia(barrio, consola, guardia).ejecutar();
        }
    }

    private void ingresarEmpleado() {
        EmpleadoMantenimiento empleado = barrio.buscarEmpleado(consola.leerEntero("Legajo"));
        if (empleado == null) {
            consola.error("No hay ningun empleado con ese legajo.");
        } else {
            new MenuEmpleado(barrio, consola, empleado).ejecutar();
        }
    }

    // Permite mostrar las reglas que dependen del tiempo (24 horas, 48 horas, 5 dias) sin esperar.
    private void avanzarTiempo() {
        int horas = consola.leerEntero("Cuantas horas quiere avanzar (24 = un dia, 48 = dos dias)");
        try {
            new ControladorBarrio(barrio).avanzarTiempo(horas);
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
            return;
        }
        consola.ok("Fecha del sistema: " + barrio.formatear(barrio.getAhora()));
        ejecutarReglasAutomaticas();
    }

    // Lo que hace el "Sistema" por su cuenta: escalar reclamos, cerrar reclamos resueltos,
    // marcar lotes morosos y avisar de camaras sin funcionar.
    private void ejecutarReglasAutomaticas() {
        List<String> mensajes = new ArrayList<>();
        mensajes.addAll(new ControladorReclamos(barrio).ejecutarReglasAutomaticas());
        mensajes.addAll(new ControladorExpensas(barrio).verificarMorosidad(barrio.hoy()));
        mensajes.addAll(new ControladorMantenimiento(barrio).revisarEquipos());
        for (String mensaje : mensajes) {
            consola.aviso("[SISTEMA] " + mensaje);
        }
    }

    private void guardar() {
        try {
            archivo.guardar(barrio);
        } catch (IOException e) {
            consola.error("No se pudieron guardar los datos: " + e.getMessage());
        }
    }
}
