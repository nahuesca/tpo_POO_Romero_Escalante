package aromos.personas;

import java.io.Serializable;

// Clase abstracta con los datos que tienen en comun todas las personas del sistema.
// Implementa Serializable para poder guardarla en un archivo.
public abstract class Persona implements Serializable {
    private String nombre;
    private String apellido;
    private String documento;
    private String telefono;
    private String correo;

    protected Persona(String nombre, String apellido, String documento, String telefono, String correo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        this.telefono = telefono;
        this.correo = correo;
    }

    // Cada subclase devuelve su propio rol (se sobreescribe).
    public abstract String obtenerRol();

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDocumento() { return documento; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }

    // Dos personas son la misma si tienen el mismo documento.
    @Override
    public boolean equals(Object otro) {
        if (!(otro instanceof Persona)) {
            return false;
        }
        Persona otraPersona = (Persona) otro;
        return documento.equals(otraPersona.documento);
    }

    @Override
    public int hashCode() {
        return documento.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " (" + obtenerRol() + ", doc " + documento + ")";
    }
}
