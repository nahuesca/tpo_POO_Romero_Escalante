package aromos.ui;

import aromos.controladores.ControladorBarrio;
import aromos.controladores.ControladorExpensas;
import aromos.controladores.ControladorReclamos;
import aromos.controladores.ControladorVisitas;
import aromos.excepciones.ReglaNegocioException;
import aromos.expensas.MedioPago;
import aromos.expensas.Pago;
import aromos.personas.Barrio;
import aromos.personas.Lote;
import aromos.personas.TipoVisitante;
import aromos.personas.Vecino;
import aromos.reclamos.Reclamo;
import aromos.reclamos.TipoReclamo;
import aromos.visitas.Autorizacion;
import java.time.LocalDate;
import java.util.List;

public class MenuVecino extends MenuBase {
    private Vecino vecino;
    private ControladorReclamos reclamos;
    private ControladorExpensas expensas;
    private ControladorVisitas visitas;
    private ControladorBarrio gestion;

    public MenuVecino(Barrio barrio, Consola consola, Vecino vecino) {
        super(barrio, consola);
        this.vecino = vecino;
        this.reclamos = new ControladorReclamos(barrio);
        this.expensas = new ControladorExpensas(barrio);
        this.visitas = new ControladorVisitas(barrio);
        this.gestion = new ControladorBarrio(barrio);
    }

    @Override
    public String titulo() {
        return "VECINO: " + vecino.getNombre() + " " + vecino.getApellido();
    }

    @Override
    public String[] opciones() {
        return new String[] {
            "Presentar un reclamo o solicitud",
            "Ver mis reclamos y su seguimiento",
            "Comentar un reclamo",
            "Reabrir un reclamo resuelto",
            "Consultar deudas de expensas",
            "Registrar un pago de expensas",
            "Autorizar una visita o proveedor",
            "Ver mis autorizaciones de visita",
            "Reportar la falla de una luminaria o camara",
            "Ver estado de luminarias y camaras",
            "Autorizar a un residente a presentar reclamos",
            "Ver mis notificaciones"
        };
    }

    @Override
    public void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1: presentarReclamo(); break;
            case 2: verMisReclamos(); break;
            case 3: comentarReclamo(); break;
            case 4: reabrirReclamo(); break;
            case 5: mostrarDeudas(elegirMiLote()); break;
            case 6: registrarPago(); break;
            case 7: autorizarVisita(); break;
            case 8: verMisAutorizaciones(); break;
            case 9: reportarFallaDeEquipo(); break;
            case 10: mostrarEstadoEquipos(); break;
            case 11: autorizarReclamante(); break;
            case 12: mostrarNotificaciones(vecino); break;
            default: break;
        }
    }

    // Si el vecino tiene un solo lote se usa ese; si tiene varios, elige uno.
    private Lote elegirMiLote() {
        List<Lote> lotes = barrio.lotesDe(vecino);
        if (lotes.size() == 1) {
            return lotes.get(0);
        }
        return lotes.get(consola.elegir("Sus lotes", lotes.toArray()));
    }

    private void presentarReclamo() {
        Lote lote = elegirMiLote();
        TipoReclamo tipo = TipoReclamo.values()[consola.elegir("Tipo de reclamo", TipoReclamo.values())];
        String descripcion = consola.leerTexto("Descripcion");
        try {
            Reclamo reclamo = reclamos.presentarReclamo(vecino, lote, tipo, descripcion);
            consola.ok("Reclamo #" + reclamo.getNumero() + " registrado. Prioridad " + reclamo.getPrioridad()
                    + ", estado " + reclamo.getEstado() + ".");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void verMisReclamos() {
        List<Reclamo> lista = reclamos.consultarReclamosDe(vecino);
        consola.titulo("Mis reclamos");
        consola.listar("no tiene reclamos", lista);
        if (!lista.isEmpty()) {
            int numero = consola.leerEntero("Numero de reclamo para ver el detalle (0 = ninguno)");
            if (numero != 0) {
                Reclamo reclamo = barrio.buscarReclamo(numero);
                if (reclamo != null && lista.contains(reclamo)) {
                    mostrarDetalleReclamo(reclamo);
                } else {
                    consola.error("Ese reclamo no esta entre los suyos.");
                }
            }
        }
    }

    private void comentarReclamo() {
        Reclamo reclamo = pedirReclamo();
        if (reclamo == null) {
            return;
        }
        String texto = consola.leerTexto("Comentario");
        try {
            reclamos.agregarComentario(vecino, reclamo, texto);
            consola.ok("Comentario agregado al reclamo #" + reclamo.getNumero() + ".");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void reabrirReclamo() {
        Reclamo reclamo = pedirReclamo();
        if (reclamo == null) {
            return;
        }
        try {
            reclamos.reabrirReclamo(vecino, reclamo);
            consola.ok("Reclamo #" + reclamo.getNumero() + " reabierto: vuelve a EN_CURSO.");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void registrarPago() {
        Lote lote = elegirMiLote();
        mostrarDeudas(lote);
        double importe = consola.leerNumero("Importe a pagar");
        MedioPago medio = MedioPago.values()[consola.elegir("Medio de pago", MedioPago.values())];
        try {
            Pago pago = expensas.registrarPago(lote, importe, medio, barrio.hoy());
            consola.ok("Pago registrado: " + pago);
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void autorizarVisita() {
        Lote lote = elegirMiLote();
        String nombre = consola.leerTexto("Nombre del visitante");
        String apellido = consola.leerTexto("Apellido del visitante");
        String documento = consola.leerTexto("Documento del visitante");
        TipoVisitante tipo = TipoVisitante.values()[consola.elegir("Tipo de visitante", TipoVisitante.values())];
        boolean recurrente = false;
        if (tipo == TipoVisitante.PROVEEDOR_HABITUAL) {
            recurrente = consola.confirmar("Autorizacion recurrente (hasta 30 dias)");
        }
        LocalDate desde = consola.leerFecha("Fecha desde", barrio.hoy());
        LocalDate hasta = desde;
        if (recurrente) {
            hasta = consola.leerFecha("Fecha hasta", desde.plusDays(29));
        }
        int horaDesde = consola.leerHora("Hora desde");
        int horaHasta = consola.leerHora("Hora hasta");
        try {
            Autorizacion autorizacion = visitas.autorizarVisita(vecino, lote, nombre, apellido, documento, tipo,
                    desde, hasta, horaDesde, horaHasta, recurrente);
            consola.ok("Autorizacion registrada: " + autorizacion);
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }

    private void verMisAutorizaciones() {
        consola.titulo("Mis autorizaciones de visita");
        consola.listar("no tiene autorizaciones", visitas.consultarAutorizacionesDe(vecino));
    }

    private void autorizarReclamante() {
        Lote lote = elegirMiLote();
        List<Vecino> residentes = lote.getResidentes();
        if (residentes.isEmpty()) {
            consola.aviso("El lote " + lote.getNumero() + " no tiene otros residentes.");
            return;
        }
        Vecino residente = residentes.get(consola.elegir("Residentes del lote " + lote.getNumero(), residentes.toArray()));
        boolean autorizar = consola.confirmar("Autorizar a " + residente.getNombre()
                + " a presentar reclamos (n = sacarle la autorizacion)");
        try {
            gestion.autorizarReclamante(vecino, lote, residente, autorizar);
            consola.ok("Listo: se actualizo la autorizacion de " + residente.getNombre() + ".");
        } catch (ReglaNegocioException e) {
            consola.error(e.getMessage());
        }
    }
}
