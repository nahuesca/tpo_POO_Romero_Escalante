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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// Ventana de entrada: se elige el rol y se escribe el documento (vecino) o el legajo (personal).
// Al ingresar se abre la ventana de ese rol (VentanaRol).
public class VentanaPrincipal extends JFrame implements ActionListener {
    private Barrio barrio;
    private ArchivoDatos archivo;
    private ConsolaGrafica consola;

    private JLabel lblFecha;
    private JTextArea txtSalida;
    private JButton btnVecino;
    private JButton btnAdministrador;
    private JButton btnGuardia;
    private JButton btnEmpleado;
    private JButton btnTiempo;
    private JButton btnReglas;

    public VentanaPrincipal(Barrio barrio, ArchivoDatos archivo) {
        super("Barrio " + barrio.getNombre());
        this.barrio = barrio;
        this.archivo = archivo;

        txtSalida = new JTextArea();
        txtSalida.setEditable(false);
        txtSalida.setLineWrap(true);
        txtSalida.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        consola = new ConsolaGrafica(this, txtSalida);

        lblFecha = new JLabel();
        lblFecha.setFont(lblFecha.getFont().deriveFont(Font.BOLD, 14f));
        lblFecha.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        btnVecino = new JButton("Ingresar como VECINO (con el documento)");
        btnAdministrador = new JButton("Ingresar como ADMINISTRADOR (con el legajo)");
        btnGuardia = new JButton("Ingresar como GUARDIA (con el legajo)");
        btnEmpleado = new JButton("Ingresar como EMPLEADO DE MANTENIMIENTO (con el legajo)");
        btnTiempo = new JButton("Simular el paso del tiempo (avanzar horas)");
        btnReglas = new JButton("Ejecutar las reglas automaticas del sistema");
        btnVecino.addActionListener(this);
        btnAdministrador.addActionListener(this);
        btnGuardia.addActionListener(this);
        btnEmpleado.addActionListener(this);
        btnTiempo.addActionListener(this);
        btnReglas.addActionListener(this);

        JPanel pnlBotones = new JPanel(new GridLayout(0, 1, 5, 5));
        pnlBotones.setBackground(Color.WHITE);
        pnlBotones.add(btnVecino);
        pnlBotones.add(btnAdministrador);
        pnlBotones.add(btnGuardia);
        pnlBotones.add(btnEmpleado);
        pnlBotones.add(btnTiempo);
        pnlBotones.add(btnReglas);

        JPanel pnlOeste = new JPanel(new BorderLayout());
        pnlOeste.setBackground(Color.WHITE);
        pnlOeste.add(pnlBotones, BorderLayout.NORTH);

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        add(lblFecha, BorderLayout.NORTH);
        add(pnlOeste, BorderLayout.WEST);
        add(new JScrollPane(txtSalida), BorderLayout.CENTER);

        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Igual que en la consola, al abrir el sistema se aplican las reglas automaticas.
        ejecutarReglasAutomaticas();
        guardar();
        actualizarFecha();
    }

    // Se ejecuta cada vez que se aprieta un boton
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == btnVecino) {
                ingresarVecino();
            } else if (e.getSource() == btnAdministrador) {
                ingresarAdministrador();
            } else if (e.getSource() == btnGuardia) {
                ingresarGuardia();
            } else if (e.getSource() == btnEmpleado) {
                ingresarEmpleado();
            } else if (e.getSource() == btnTiempo) {
                avanzarTiempo();
            } else if (e.getSource() == btnReglas) {
                ejecutarReglasAutomaticas();
            }
        } catch (OperacionCancelada ex) {
            consola.aviso("Operacion cancelada.");
        }
        guardar();
        actualizarFecha();
    }

    private void ingresarVecino() {
        String documento = consola.leerTexto("Documento");
        Vecino vecino = barrio.buscarVecino(documento);
        if (vecino == null) {
            consola.error("No hay ningun vecino con ese documento.");
        } else if (barrio.lotesDe(vecino).isEmpty()) {
            consola.error("El vecino no tiene un lote asociado: no puede ingresar.");
        } else {
            abrirVentana(new MenuVecino(barrio, consola, vecino));
        }
    }

    private void ingresarAdministrador() {
        int legajo = consola.leerEntero("Legajo");
        Administrador administrador = barrio.getAdministrador();
        if (administrador.getLegajo() != legajo) {
            consola.error("Legajo de administrador incorrecto.");
        } else {
            abrirVentana(new MenuAdministrador(barrio, consola, administrador));
        }
    }

    private void ingresarGuardia() {
        Guardia guardia = barrio.buscarGuardia(consola.leerEntero("Legajo"));
        if (guardia == null) {
            consola.error("No hay ningun guardia con ese legajo.");
        } else {
            abrirVentana(new MenuGuardia(barrio, consola, guardia));
        }
    }

    private void ingresarEmpleado() {
        EmpleadoMantenimiento empleado = barrio.buscarEmpleado(consola.leerEntero("Legajo"));
        if (empleado == null) {
            consola.error("No hay ningun empleado con ese legajo.");
        } else {
            abrirVentana(new MenuEmpleado(barrio, consola, empleado));
        }
    }

    private void abrirVentana(MenuBase menu) {
        new VentanaRol(menu, barrio, archivo).setVisible(true);
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

    private void actualizarFecha() {
        lblFecha.setText("Fecha del sistema: " + barrio.formatear(barrio.getAhora()));
    }

    private void guardar() {
        try {
            archivo.guardar(barrio);
        } catch (IOException ex) {
            consola.error("No se pudieron guardar los datos: " + ex.getMessage());
        }
    }
}
