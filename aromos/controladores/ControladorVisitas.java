package aromos.controladores;

import aromos.excepciones.NoAutorizadoException;
import aromos.excepciones.ReglaNegocioException;
import aromos.personas.Barrio;
import aromos.personas.Guardia;
import aromos.personas.Lote;
import aromos.personas.Puesto;
import aromos.personas.TipoVisitante;
import aromos.personas.Vecino;
import aromos.personas.Visitante;
import aromos.visitas.Autorizacion;
import aromos.visitas.RegistroAcceso;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// GRASP Controlador del caso de uso "Registrar el ingreso de visitas y proveedores".
public class ControladorVisitas {
    private Barrio barrio;

    public ControladorVisitas(Barrio barrio) {
        this.barrio = barrio;
    }

    // El vecino autoriza a una persona indicando fecha y franja horaria.
    public Autorizacion autorizarVisita(Vecino vecino, Lote lote, String nombre, String apellido, String documento,
                                        TipoVisitante tipo, LocalDate desde, LocalDate hasta,
                                        int horaDesde, int horaHasta, boolean recurrente)
            throws ReglaNegocioException {
        if (lote == null) {
            throw new ReglaNegocioException("El lote indicado no existe");
        }
        if (nombre.trim().isEmpty() || documento.trim().isEmpty()) {
            throw new ReglaNegocioException("Hace falta el nombre y el documento del visitante");
        }
        Visitante visitante = barrio.buscarVisitante(documento.trim());
        boolean visitanteNuevo = false;
        if (visitante == null) {
            visitante = new Visitante(nombre.trim(), apellido.trim(), documento.trim(), tipo);
            visitanteNuevo = true;
        }
        Autorizacion autorizacion = new Autorizacion(vecino, visitante, lote, desde, hasta, horaDesde, horaHasta,
                recurrente);
        if (visitanteNuevo) {
            barrio.registrarVisitante(visitante);
        }
        barrio.agregarAutorizacion(autorizacion);
        return autorizacion;
    }

    // Busca una autorizacion vigente para ese documento. Si no hay, no se habilita el ingreso.
    public Autorizacion buscarAutorizacionVigente(String documento) throws ReglaNegocioException {
        for (Autorizacion a : barrio.consultarAutorizaciones()) {
            boolean mismoDocumento = a.getVisitante().getDocumento().equals(documento);
            if (mismoDocumento && a.estaVigente(barrio.hoy(), barrio.horaDelDia())) {
                return a;
            }
        }
        throw new ReglaNegocioException("No hay autorizacion vigente para el documento " + documento
                + ": no se habilita el ingreso");
    }

    // Flujo principal: verificar la autorizacion y registrar la hora de ingreso.
    // Flujos alternativos: sin autorizacion no entra; si ya tiene un ingreso abierto queda bloqueado.
    public RegistroAcceso registrarIngreso(Guardia guardia, String documento) throws ReglaNegocioException {
        if (guardia.getPuesto() != Puesto.PORTERIA) {
            throw new NoAutorizadoException("Solo el guardia de porteria puede registrar ingresos");
        }
        Autorizacion autorizacion = buscarAutorizacionVigente(documento);
        if (barrio.buscarRegistroAbierto(documento) != null) {
            throw new ReglaNegocioException("La persona ya tiene un ingreso abierto: primero hay que registrar su egreso");
        }
        RegistroAcceso registro = guardia.registrarIngreso(autorizacion, barrio.hoy(), barrio.horaDelDia());
        barrio.agregarRegistroAcceso(registro);
        return registro;
    }

    public RegistroAcceso registrarEgreso(Guardia guardia, String documento) throws ReglaNegocioException {
        RegistroAcceso registro = barrio.buscarRegistroAbierto(documento);
        if (registro == null) {
            throw new ReglaNegocioException("La persona con documento " + documento + " no tiene un ingreso abierto");
        }
        guardia.registrarEgreso(registro, barrio.horaDelDia());
        return registro;
    }

    // Personas que estan dentro del barrio (entraron y todavia no salieron)
    public List<RegistroAcceso> consultarIngresosAbiertos() {
        List<RegistroAcceso> resultado = new ArrayList<>();
        for (RegistroAcceso r : barrio.consultarRegistros()) {
            if (r.estaAbierto()) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public List<Autorizacion> consultarAutorizacionesDe(Vecino vecino) {
        List<Autorizacion> resultado = new ArrayList<>();
        for (Autorizacion a : barrio.consultarAutorizaciones()) {
            if (a.getVecino().equals(vecino)) {
                resultado.add(a);
            }
        }
        return resultado;
    }
}
