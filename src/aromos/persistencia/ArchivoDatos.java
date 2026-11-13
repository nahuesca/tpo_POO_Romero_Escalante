package aromos.persistencia;

import aromos.personas.Barrio;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// Guarda y carga el barrio completo en un archivo mediante serializacion.
public class ArchivoDatos {
    private String ruta;

    public ArchivoDatos(String ruta) {
        this.ruta = ruta;
    }

    // Indica si ya existe el archivo de datos.
    public boolean existe() {
        return new File(ruta).exists();
    }

    // Guarda el barrio en el archivo, creando la carpeta si hace falta.
    public void guardar(Barrio barrio) throws IOException {
        File carpeta = new File(ruta).getAbsoluteFile().getParentFile();
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        try (FileOutputStream archivo = new FileOutputStream(ruta);
             ObjectOutputStream salida = new ObjectOutputStream(archivo)) {
            salida.writeObject(barrio);
        }
    }

    // Lee el archivo y devuelve el barrio guardado.
    public Barrio cargar() throws IOException, ClassNotFoundException {
        try (FileInputStream archivo = new FileInputStream(ruta);
             ObjectInputStream entrada = new ObjectInputStream(archivo)) {
            return (Barrio) entrada.readObject();
        }
    }
}
