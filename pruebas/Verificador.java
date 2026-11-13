package aromos.pruebas;

// Clase simple para hacer las pruebas (no usamos JUnit porque no lo vimos en clase).
// Cada verificacion imprime [OK] o [FALLA] y al final se muestra un resumen.
public class Verificador {
    private int total;
    private int fallas;

    public void grupo(String nombre) {
        System.out.println();
        System.out.println("== " + nombre);
    }

    public void verificar(String descripcion, boolean condicion) {
        total++;
        if (condicion) {
            System.out.println("  [OK]    " + descripcion);
        } else {
            fallas++;
            System.out.println("  [FALLA] " + descripcion);
        }
    }

    public void verificarIgual(String descripcion, Object esperado, Object obtenido) {
        total++;
        if (esperado.equals(obtenido)) {
            System.out.println("  [OK]    " + descripcion + " (" + obtenido + ")");
        } else {
            fallas++;
            System.out.println("  [FALLA] " + descripcion + " -> esperado " + esperado + ", obtenido " + obtenido);
        }
    }

    // Para comparar numeros con decimales (acepta una diferencia de 1 centavo)
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

    // La prueba atrapa la excepcion con try/catch y la pasa aca: tiene que haberse producido un error.
    public void verificarError(String descripcion, Exception error) {
        total++;
        if (error != null) {
            System.out.println("  [OK]    " + descripcion + " -> " + error.getMessage());
        } else {
            fallas++;
            System.out.println("  [FALLA] " + descripcion + " -> tendria que haber dado error y no lo dio");
        }
    }

    // Devuelve true si todas las verificaciones salieron bien
    public boolean resumen() {
        System.out.println();
        System.out.println("==========================================");
        System.out.println("Verificaciones: " + total + " - OK: " + (total - fallas) + " - FALLAS: " + fallas);
        System.out.println("==========================================");
        return fallas == 0;
    }
}
