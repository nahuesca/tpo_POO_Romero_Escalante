package aromos.pruebas;

// Herramienta simple para chequear condiciones y contar las pruebas que pasan o fallan.
public class Verificador {
    private int total;
    private int fallas;

    // Marca el comienzo de un grupo de pruebas.
    public void grupo(String nombre) {
        System.out.println();
        System.out.println("== " + nombre);
    }

    // Registra si una condicion se cumple.
    public void verificar(String descripcion, boolean condicion) {
        total++;
        if (condicion) {
            System.out.println("  [OK]    " + descripcion);
        } else {
            fallas++;
            System.out.println("  [FALLA] " + descripcion);
        }
    }

    // Registra si el valor obtenido es igual al esperado.
    public void verificarIgual(String descripcion, Object esperado, Object obtenido) {
        total++;
        if (esperado.equals(obtenido)) {
            System.out.println("  [OK]    " + descripcion + " (" + obtenido + ")");
        } else {
            fallas++;
            System.out.println("  [FALLA] " + descripcion + " -> esperado " + esperado + ", obtenido " + obtenido);
        }
    }

    // Compara dos numeros con decimales aceptando una diferencia de un centavo.
    public void verificarNumero(String descripcion, double esperado, double obtenido) {
        total++;
        double diferencia = esperado - obtenido;
        if (diferencia < 0.01 && diferencia > -0.01) {
            System.out.println("  [OK]    " + descripcion + " (" + obtenido + ")");
        } else {
            fallas++;
            System.out.println("  [FALLA] " + descripcion + " -> esperado " + esperado + ", obtenido " + obtenido);
        }
    }

    // Registra si ocurrio el error que se esperaba.
    public void verificarError(String descripcion, Exception error) {
        total++;
        if (error != null) {
            System.out.println("  [OK]    " + descripcion + " -> " + error.getMessage());
        } else {
            fallas++;
            System.out.println("  [FALLA] " + descripcion + " -> tendria que haber dado error y no lo dio");
        }
    }

    // Muestra el total de pruebas correctas y fallidas.
    public boolean resumen() {
        System.out.println();
        System.out.println("==========================================");
        System.out.println("Verificaciones: " + total + " - OK: " + (total - fallas) + " - FALLAS: " + fallas);
        System.out.println("==========================================");
        return fallas == 0;
    }
}
