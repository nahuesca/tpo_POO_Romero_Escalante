package aromos.ui;

import aromos.persistencia.ArchivoDatos;
import aromos.personas.Barrio;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// Ventana de un rol con los botones de su menu y el area donde se muestran los resultados.
public class VentanaRol extends JFrame implements ActionListener {
    private MenuBase menu;
    private Barrio barrio;
    private ArchivoDatos archivo;
    private ConsolaGrafica consola;

    private JTextArea txtSalida;
    private String[] opciones;
    private JButton[] botones;
    private JButton btnCerrar;

    public VentanaRol(MenuBase menu, Barrio barrio, ArchivoDatos archivo) {
        super(menu.titulo());
        this.menu = menu;
        this.barrio = barrio;
        this.archivo = archivo;

        txtSalida = new JTextArea();
        txtSalida.setEditable(false);
        txtSalida.setLineWrap(true);
        txtSalida.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        consola = new ConsolaGrafica(this, txtSalida);
        menu.setConsola(consola);

        opciones = menu.opciones();
        botones = new JButton[opciones.length];
        JPanel pnlBotones = new JPanel(new GridLayout(0, 1, 5, 5));
        pnlBotones.setBackground(Color.WHITE);
        for (int i = 0; i < opciones.length; i++) {
            botones[i] = new JButton(opciones[i]);
            botones[i].addActionListener(this);
            pnlBotones.add(botones[i]);
        }
        btnCerrar = new JButton("Cerrar sesion");
        btnCerrar.addActionListener(this);
        pnlBotones.add(btnCerrar);

        JPanel pnlOeste = new JPanel(new BorderLayout());
        pnlOeste.setBackground(Color.WHITE);
        pnlOeste.add(pnlBotones, BorderLayout.NORTH);

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        add(new JScrollPane(pnlOeste), BorderLayout.WEST);
        add(new JScrollPane(txtSalida), BorderLayout.CENTER);

        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        consola.linea("Elija una opcion de la izquierda.");
    }

    // Ejecuta la opcion del menu que corresponde al boton apretado.
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCerrar) {
            dispose();
            return;
        }
        for (int i = 0; i < botones.length; i++) {
            if (e.getSource() == botones[i]) {
                consola.titulo(opciones[i]);
                try {
                    menu.ejecutarOpcion(i + 1);
                } catch (OperacionCancelada ex) {
                    consola.aviso("Operacion cancelada.");
                }
                guardar();
            }
        }
    }

    // Guarda los datos del barrio en el archivo.
    private void guardar() {
        try {
            archivo.guardar(barrio);
        } catch (IOException ex) {
            consola.error("No se pudieron guardar los datos: " + ex.getMessage());
        }
    }
}
