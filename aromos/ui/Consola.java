package aromos.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

// Se encarga de leer datos por teclado (validando que esten bien) y de mostrar mensajes.
public class Consola {
    private Scanner scanner;

    public Consola(Scanner scanner) {
        this.scanner = scanner;
    }

    public void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }

    public void linea(String texto) {
        System.out.println(texto);
    }

    public void ok(String texto) {
        System.out.println("[OK] " + texto);
    }

    public void error(String texto) {
        System.out.println("[ERROR] " + texto);
    }

    public void aviso(String texto) {
        System.out.println("[AVISO] " + texto);
    }

    // Muestra una lista, o un mensaje si esta vacia
    public void listar(String mensajeSiVacia, List<?> elementos) {
        if (elementos.isEmpty()) {
            System.out.println("(" + mensajeSiVacia + ")");
        }
        for (Object elemento : elementos) {
            System.out.println(" - " + elemento);
        }
    }

    public String leerTexto(String pregunta) {
        System.out.print(pregunta + ": ");
        return scanner.nextLine().trim();
    }

    public int leerEntero(String pregunta) {
        while (true) {
            String texto = leerTexto(pregunta);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                error("Ingrese un numero entero.");
            }
        }
    }

    public double leerNumero(String pregunta) {
        while (true) {
            String texto = leerTexto(pregunta).replace(',', '.');
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                error("Ingrese un numero (ejemplo: 1500.50).");
            }
        }
    }

    // Lee una fecha con formato aaaa-mm-dd. Si se aprieta Enter devuelve la fecha por defecto.
    public LocalDate leerFecha(String pregunta, LocalDate porDefecto) {
        while (true) {
            String texto = leerTexto(pregunta + " (aaaa-mm-dd, Enter = " + porDefecto + ")");
            if (texto.isEmpty()) {
                return porDefecto;
            }
            try {
                return LocalDate.parse(texto);
            } catch (DateTimeParseException e) {
                error("Fecha invalida. Ejemplo: 2026-11-04");
            }
        }
    }

    // Lee una hora del dia (numero de 0 a 23)
    public int leerHora(String pregunta) {
        while (true) {
            int hora = leerEntero(pregunta + " (0 a 23)");
            if (hora >= 0 && hora <= 23) {
                return hora;
            }
            error("La hora tiene que estar entre 0 y 23.");
        }
    }

    public boolean confirmar(String pregunta) {
        while (true) {
            String texto = leerTexto(pregunta + " (s/n)").toLowerCase();
            if (texto.equals("s")) {
                return true;
            }
            if (texto.equals("n")) {
                return false;
            }
            error("Responda s o n.");
        }
    }

    // Muestra un menu y devuelve el numero elegido (0 = volver o salir)
    public int menu(String titulo, String[] opciones) {
        titulo(titulo);
        for (int i = 0; i < opciones.length; i++) {
            System.out.println(" " + (i + 1) + ") " + opciones[i]);
        }
        System.out.println(" 0) Volver / Salir");
        while (true) {
            int eleccion = leerEntero("Opcion");
            if (eleccion >= 0 && eleccion <= opciones.length) {
                return eleccion;
            }
            error("Elija una opcion entre 0 y " + opciones.length + ".");
        }
    }

    // Muestra las opciones numeradas y devuelve la posicion elegida (empieza en 0)
    public int elegir(String titulo, Object[] opciones) {
        System.out.println(titulo + ":");
        for (int i = 0; i < opciones.length; i++) {
            System.out.println("  " + (i + 1) + ") " + opciones[i]);
        }
        while (true) {
            int eleccion = leerEntero("Numero");
            if (eleccion >= 1 && eleccion <= opciones.length) {
                return eleccion - 1;
            }
            error("Elija un numero entre 1 y " + opciones.length + ".");
        }
    }
}
