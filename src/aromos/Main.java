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

// Programa principal: carga o crea los datos del barrio y abre la interfaz grafica o el menu por consola.
public class Main {
    // Arranca el sistema; con el argumento --consola usa el menu de texto y sin argumentos abre las ventanas.
    public static void main(String[] args) {
        ArchivoDatos archivo = new ArchivoDatos("datos/barrio.dat");
        Consola consola = new Consola(new Scanner(System.in));

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
