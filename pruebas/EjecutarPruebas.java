package aromos.pruebas;

// Ejecuta todas las pruebas. Hay que correrlo desde la carpeta del proyecto (la que tiene src, test y datos).
public class EjecutarPruebas {

    public static void main(String[] args) {
        Verificador v = new Verificador();
        try {
            new PruebasReclamos().ejecutar(v);
            new PruebasExpensas().ejecutar(v);
            new PruebasMantenimiento().ejecutar(v);
            new PruebasVisitas().ejecutar(v);
            new PruebasGestion().ejecutar(v);
            new PruebasPersistencia().ejecutar(v);
        } catch (Exception e) {
            System.out.println("ERROR INESPERADO en las pruebas: " + e);
            e.printStackTrace();
        }
        v.resumen();
    }
}
