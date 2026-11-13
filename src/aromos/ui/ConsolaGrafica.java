package aromos.ui;

import java.awt.Component;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

// Version de Consola que usa ventanas y un area de texto en lugar de la terminal.
public class ConsolaGrafica extends Consola {
    private Component ventana;
    private JTextArea txtSalida;

    public ConsolaGrafica(Component ventana, JTextArea txtSalida) {
        super(null);
        this.ventana = ventana;
        this.txtSalida = txtSalida;
    }

    // Agrega una linea al area de texto.
    private void escribir(String texto) {
        txtSalida.append(texto + "\n");
        txtSalida.setCaretPosition(txtSalida.getDocument().getLength());
    }

    @Override
    public void titulo(String texto) {
        escribir("");
        escribir("=== " + texto + " ===");
    }

    @Override
    public void linea(String texto) {
        escribir(texto);
    }

    @Override
    public void ok(String texto) {
        escribir("[OK] " + texto);
    }

    @Override
    public void aviso(String texto) {
        escribir("[AVISO] " + texto);
    }

    @Override
    public void error(String texto) {
        escribir("[ERROR] " + texto);
        JOptionPane.showMessageDialog(ventana, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Muestra una lista en el area de texto.
    @Override
    public void listar(String mensajeSiVacia, List<?> elementos) {
        if (elementos.isEmpty()) {
            escribir("(" + mensajeSiVacia + ")");
        }
        for (Object elemento : elementos) {
            escribir(" - " + elemento);
        }
    }

    // Pide un texto con un cuadro de dialogo.
    @Override
    public String leerTexto(String pregunta) {
        String texto = JOptionPane.showInputDialog(ventana, pregunta);
        if (texto == null) {
            throw new OperacionCancelada();
        }
        return texto.trim();
    }

    // Pide confirmacion con un cuadro de dialogo.
    @Override
    public boolean confirmar(String pregunta) {
        int respuesta = JOptionPane.showConfirmDialog(ventana, pregunta, "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            return true;
        }
        if (respuesta == JOptionPane.NO_OPTION) {
            return false;
        }
        throw new OperacionCancelada();
    }

    // Muestra un desplegable para elegir una opcion.
    private int elegirDeLista(String titulo, Object[] opciones) {
        Object elegida = JOptionPane.showInputDialog(ventana, titulo, "Elegir",
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        for (int i = 0; i < opciones.length; i++) {
            if (opciones[i] == elegida) {
                return i;
            }
        }
        return -1;
    }

    // Muestra un menu en una ventana y devuelve la opcion elegida.
    @Override
    public int menu(String titulo, String[] opciones) {
        return elegirDeLista(titulo, opciones) + 1;
    }

    // Muestra una lista en una ventana y devuelve la posicion elegida.
    @Override
    public int elegir(String titulo, Object[] opciones) {
        if (opciones.length == 0) {
            error("No hay opciones para elegir.");
            throw new OperacionCancelada();
        }
        int posicion = elegirDeLista(titulo, opciones);
        if (posicion == -1) {
            throw new OperacionCancelada();
        }
        return posicion;
    }
}
