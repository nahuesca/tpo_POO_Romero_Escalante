package aromos.pruebas;

import aromos.controladores.ControladorReclamos;
import aromos.persistencia.ArchivoDatos;
import aromos.persistencia.DatosPrueba;
import aromos.personas.Barrio;
import aromos.reclamos.Reclamo;
import aromos.reclamos.TipoReclamo;
import java.io.File;

// Pruebas de guardado y carga de datos.
public class PruebasPersistencia {
    private static final String RUTA = "datos/prueba_temporal.dat";

    // Prueba que lo guardado se carga igual.
    public void ejecutar(Verificador v) throws Exception {
        v.grupo("Persistencia: guardar y cargar el barrio en un archivo");
        ArchivoDatos archivo = new ArchivoDatos(RUTA);
        new File(RUTA).delete();
        v.verificar("Antes de guardar el archivo no existe", !archivo.existe());

        Barrio original = DatosPrueba.crear();
        v.verificar("En los datos de prueba el lote 5 es moroso", original.buscarLote(5).isMoroso());
        archivo.guardar(original);
        v.verificar("Despues de guardar el archivo existe", archivo.existe());

        Barrio cargado = archivo.cargar();
        v.verificarIgual("Se cargan los mismos lotes", original.consultarLotes().size(), cargado.consultarLotes().size());
        v.verificarIgual("Se cargan los mismos reclamos", original.consultarTodosLosReclamos().size(),
                cargado.consultarTodosLosReclamos().size());
        v.verificar("El lote 5 sigue siendo moroso", cargado.buscarLote(5).isMoroso());
        v.verificar("Los objetos siguen relacionados (el reclamo conoce a su lote)",
                cargado.buscarLote(1).consultarReclamos().get(0).getLote() == cargado.buscarLote(1));

        int cantidad = cargado.consultarTodosLosReclamos().size();
        new ControladorReclamos(cargado).presentarReclamo(cargado.buscarVecino("30000003"), cargado.buscarLote(3),
                TipoReclamo.CONVIVENCIA, "Ruido de obra");
        archivo.guardar(cargado);
        Barrio recargado = archivo.cargar();
        v.verificarIgual("El reclamo nuevo se conserva", cantidad + 1, recargado.consultarTodosLosReclamos().size());
        Reclamo ultimo = recargado.buscarReclamo(cantidad + 1);
        v.verificarIgual("Y mantiene su descripcion", "Ruido de obra", ultimo.getDescripcion());
        new File(RUTA).delete();

        Exception error = null;
        try {
            new ArchivoDatos("datos/no_existe.dat").cargar();
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Cargar un archivo que no existe da error", error);
    }
}
