package aromos.ui;

import java.awt.Component;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

// Es una Consola, pero en vez del teclado usa cuadros de dialogo para pedir datos
// y un area de texto para mostrar los resultados.
// Como hereda de Consola, los menus funcionan igual sin cambiar nada.
public class ConsolaGrafica extends Consola {
    private Component ventana;
    private JTextArea txtSalida;

    public ConsolaGrafica(Component ventana, JTextArea txtSalida) {
        super(null); // no se usa el teclado
        this.ventana = ventana;
        this.txtSalida = txtSalida;
    }

    // ---------- mostrar ----------

    private void escribir(String texto) {
        txtSalida.append(texto + "\n");
        txtSalida.setCaretPosition(txtSalida.getDocument().getLength()); // baja hasta el final
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

    // Los errores tambien se muestran en un cuadro para que se vean.
    @Override
    public void error(String texto) {
        escribir("[ERROR] " + texto);
        JOptionPane.showMessageDialog(ventana, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void listar(String mensajeSiVacia, List<?> elementos) {
        if (elementos.isEmpty()) {
            escribir("(" + mensajeSiVacia + ")");
        }
        for (Object elemento : elementos) {
            escribir(" - " + elemento);
        }
    }

    // ---------- pedir datos ----------

    // Si la persona aprieta Cancelar se corta la operacion.
    @Override
    public String leerTexto(String pregunta) {
        String texto = JOptionPane.showInputDialog(ventana, pregunta);
        if (texto == null) {
            throw new OperacionCancelada();
        }
        return texto.trim();
    }

    @Override
    public boolean confirmar(String pregunta) {
        int respuesta = JOptionPane.showConfirmDialog(ventana, pregunta, "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            return true;
        }
        if (respuesta == JOptionPane.NO_OPTION) {
            return false;
        }
        throw new OperacionCancelada(); // cerro el cuadro
    }

    // Lista desplegable. Devuelve la posicion elegida (0, 1, 2...) o -1 si se cancela.
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

    // Devuelve el numero de la opcion (1 = primera) y 0 si se cancela (igual que Consola.menu).
    @Override
    public int menu(String titulo, String[] opciones) {
        return elegirDeLista(titulo, opciones) + 1;
    }

    // Devuelve la posicion elegida (empieza en 0); si se cancela corta la operacion.
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
