package aromos.pruebas;

// Programa que corre todos los grupos de pruebas y muestra el resumen.
public class EjecutarPruebas {
    // Ejecuta las pruebas y termina con error si alguna falla.
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
