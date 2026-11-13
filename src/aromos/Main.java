package aromos;

import aromos.persistencia.ArchivoDatos;
import aromos.persistencia.DatosPrueba;
import aromos.personas.Barrio;
import aromos.ui.Consola;
import aromos.ui.MenuPrincipal;
import aromos.ui.VentanaPrincipal;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javax.swing.SwingUtilities;

// Programa principal del sistema del barrio cerrado "Los Aromos".
public class Main {

    public static void main(String[] args) {
        ArchivoDatos archivo = new ArchivoDatos("datos/barrio.dat");
        Consola consola = new Consola(new Scanner(System.in));

        // Si hay datos de una ejecucion anterior se cargan; si no, se usan los datos de prueba.
        Barrio barrio;
        if (archivo.existe()) {
            try {
                barrio = archivo.cargar();
                consola.ok("Datos cargados desde datos/barrio.dat");
            } catch (IOException | ClassNotFoundException e) {
                consola.error("No se pudo leer el archivo (" + e.getMessage() + "). Se usan los datos de prueba.");
                barrio = DatosPrueba.crear();
            }
        } else {
            consola.aviso("Primera ejecucion: se cargan los datos de prueba.");
            barrio = DatosPrueba.crear();
        }

        // Por defecto se abre la interfaz grafica; con el argumento --consola se usa el menu por consola.
        boolean consolaPedida = args.length > 0 && args[0].equals("--consola");
        if (!consolaPedida) {
            Barrio barrioCargado = barrio;
            SwingUtilities.invokeLater(() -> new VentanaPrincipal(barrioCargado, archivo).setVisible(true));
            return;
        }

        try {
            new MenuPrincipal(barrio, archivo, consola).ejecutar();
            consola.linea("Datos guardados. Hasta luego.");
        } catch (NoSuchElementException e) {
            consola.linea("Se cerro la entrada de datos. Los datos se guardaron.");
        }
    }
}
