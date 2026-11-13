package aromos.persistencia;

import aromos.personas.Barrio;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// Guarda y carga todo el barrio en un archivo, usando serializacion de objetos.
// Todas las clases del modelo implementan Serializable.
public class ArchivoDatos {
    private String ruta;

    public ArchivoDatos(String ruta) {
        this.ruta = ruta;
    }

    public boolean existe() {
        return new File(ruta).exists();
    }

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

    // readObject devuelve Object, por eso hay que hacer el cast a Barrio.
    public Barrio cargar() throws IOException, ClassNotFoundException {
        try (FileInputStream archivo = new FileInputStream(ruta);
             ObjectInputStream entrada = new ObjectInputStream(archivo)) {
            return (Barrio) entrada.readObject();
        }
    }
}
