# Sistema de gestión del barrio cerrado "Los Aromos"

Trabajo Práctico Obligatorio de Programación Orientada a Objetos, **Propuesta A: Gestión de un barrio cerrado**.

Es un programa en Java con interfaz gráfica (Swing) que ordena la relación entre la administración y los vecinos: reclamos y solicitudes, expensas y pagos (con interés por mora), control de luminarias y cámaras, ingreso de visitas y proveedores, y reportes. Los datos se guardan en un archivo y se conservan entre ejecuciones. También se puede usar con un menú por consola (ver "Cómo ejecutarlo").

## Integrantes

- Nahuel Escalante
- Martina Romero

## Requisitos

- Java 17 o superior (lo probamos con Java 25).
- No usa librerías externas, solo lo que trae Java.

## Cómo ejecutarlo

### Con IntelliJ IDEA

1. `File > Open` y elegir la carpeta del proyecto (la que contiene `src`, `test` y `datos`).
2. `File > Project Structure > Project` y elegir un SDK de Java 17 o superior.
3. Marcar `src` como **Sources** y `test` como **Tests** (clic derecho > `Mark Directory as`).
4. Abrir `src/aromos/Main.java` y ejecutar `main` con la flecha verde. El directorio de trabajo tiene que ser la carpeta del proyecto (es el valor por defecto), porque los datos se guardan en `datos/barrio.dat`.
5. Para las pruebas, ejecutar `test/aromos/pruebas/EjecutarPruebas.java`.

Por defecto `Main` abre la interfaz gráfica. Para usar la consola hay que agregar `--consola` en `Run > Edit Configurations > Program arguments`.

### Desde la consola, sin IntelliJ

Parados en la carpeta del proyecto, primero se compila todo:

```
Windows:      dir /s /b src\*.java test\*.java > fuentes.txt
Linux o Mac:  find src test -name "*.java" > fuentes.txt

javac -encoding UTF-8 -d out @fuentes.txt
```

Y después se ejecuta:

```
java -cp out aromos.Main                      (interfaz gráfica)
java -cp out aromos.Main --consola            (menú por consola)
java -cp out aromos.pruebas.EjecutarPruebas   (pruebas)
```

## Cómo se usa la interfaz gráfica

Al abrir el programa aparece la ventana principal, con fondo blanco y botones simples.

1. Se elige el rol con uno de los botones "Ingresar como..." y se escribe el documento (vecinos) o el legajo (personal) en el cuadro que aparece.
2. Se abre una ventana propia del rol, con un botón por cada función (reclamos, expensas, visitas, equipos, reportes...). Se pueden tener varias ventanas abiertas a la vez.
3. Al apretar un botón, el sistema pide los datos con cuadros de diálogo (texto, lista desplegable o Sí/No) y muestra el resultado en el área de texto de la derecha. Los errores de las reglas de negocio aparecen en un cuadro rojo y también en el área de texto. Si se aprieta Cancelar, la operación se corta sin cambios.
4. "Cerrar sesion" cierra la ventana del rol.

Los datos se guardan solos después de cada acción.

## Datos de prueba

La primera vez que se ejecuta, cuando todavía no existe `datos/barrio.dat`, se cargan datos de ejemplo. Para empezar de cero alcanza con borrar ese archivo.

Se entra apretando el botón del rol y escribiendo el documento o el legajo. No hay contraseña.

| Rol | Cómo ingresar | Quién es |
|---|---|---|
| Administrador | legajo `1` | Marta Gomez |
| Guardia de portería | legajo `11` | Juan Perez (es el único que registra ingresos) |
| Guardia de ronda | legajo `12` | Luis Diaz |
| Electricista | legajo `21` | Carlos Rios |
| Plomera | legajo `22` | Ana Suarez (ya tiene el reclamo #2) |
| Jardinero | legajo `23` | Pedro Luna |
| Vecino titular de los lotes 1, 11 y 12 | documento `30000001` | Laura Fernandez |
| Vecinos titulares de los lotes 2 a 10 | documentos `30000002` a `30000010` | Diego, Sofia, Martin, Julia, Pablo, Valeria, Nicolas, Camila, Federico |
| Residente autorizado a reclamar (lote 1) | documento `30000011` | Marcos Fernandez (familiar) |
| Residente sin autorizar (lote 2) | documento `30000012` | Elena Vega (inquilina) |

Además ya hay cargado lo siguiente para probar:

- **Lote 5** (Julia Sanchez, doc `30000005`): no pagó nada, es moroso y tiene una notificación formal.
- **Lote 6** (Pablo Romero, doc `30000006`): pagó solo dos meses. Tiene deuda con interés pero no es moroso, porque hacen falta más de 3 períodos impagos.
- **Equipos:** cámaras `CAM-01` a `CAM-03` y luminarias `LUM-01` a `LUM-07`. `LUM-03` ya tiene una falla.
- **Reclamos:** el #1 (seguridad, prioridad alta, abierto) y el #2 (mantenimiento, asignado a Ana Suarez).
- **Visitas:** el documento `40111222` (Roberto Paz) está autorizado hoy todo el día. El `40222333` (Ricardo Gas) es proveedor habitual autorizado por 30 días.

### El reloj del sistema

Algunas reglas dependen del tiempo: escalar un reclamo a las 24 horas, cerrarlo a los 5 días, avisar de una cámara a las 48 horas y calcular la mora. Para poder mostrarlas sin esperar, el sistema tiene su propio reloj. Arranca el día de la primera ejecución a las 9:00 y solo avanza cuando se elige "Simular el paso del tiempo (avanzar horas)" en la ventana principal (24 horas es un día). El reloj se guarda junto con los datos y la fecha del sistema se ve arriba, en la ventana principal.

Cada vez que se avanza el reloj, y cada vez que se abre el programa, el sistema aplica sus reglas automáticas y muestra los avisos con la marca `[SISTEMA]`.

### Guion rápido para la demostración

1. En la ventana principal, *Simular el paso del tiempo* con `25` horas: el reclamo #1 (prioridad alta) se escala al administrador.
2. *Ingresar como ADMINISTRADOR* (legajo `1`) y *Ver mis notificaciones*: aparece el aviso.
3. *Ingresar como VECINO* (documento `30000005`) y *Ver mis notificaciones*: está la notificación formal de morosidad.
4. *Ingresar como GUARDIA* (legajo `11`), *Registrar el ingreso de una persona* con el documento `40111222`: se habilita. Si se repite, queda bloqueado hasta registrar el egreso.
5. Con el guardia `11`, *Reportar la falla de una luminaria o camara* con `CAM-02`. Después, en la ventana principal, *Simular* con `50` horas: aparece el aviso de cámara sin funcionar más de 48 horas.

## Estructura del proyecto

```
tpo-completo-romero-escalante/
├── src/aromos/
│   ├── Main.java            programa principal (ventanas; con --consola, menú por consola)
│   ├── personas/            Persona, Vecino, Visitante, Personal (Administrador, Guardia,
│   │                        EmpleadoMantenimiento), Lote, Barrio
│   ├── reclamos/            Reclamo, CambioEstado, Comentario y los enums (tipo, prioridad, estado)
│   ├── expensas/            PresupuestoMensual, Expensa, Pago, AjustePago, MedioPago
│   ├── mantenimiento/       Equipo (abstracta), Camara, Luminaria, OrdenMantenimiento y enums
│   ├── visitas/             Autorizacion, RegistroAcceso
│   ├── notificaciones/      Notificacion
│   ├── controladores/       un controlador por caso de uso, ControladorBarrio (altas y reloj) y 3 comparadores
│   ├── excepciones/         ReglaNegocioException y dos subclases
│   ├── persistencia/        ArchivoDatos (guardar y cargar) y DatosPrueba
│   └── ui/                  Consola y ConsolaGrafica; MenuBase (abstracta), MenuPrincipal y un menú por rol;
│                            VentanaPrincipal y VentanaRol (interfaz gráfica Swing)
├── test/aromos/pruebas/     pruebas (se ejecutan con EjecutarPruebas)
├── datos/                   archivo de datos que genera el programa
├── documentos/              plan de pruebas, informe final, informe de la etapa 1 (PDF) y diagramas
│                            (documentos/uml: .drawio y .png)
└── README.md
```

## Requisitos de la consigna y dónde se ven

- **Paquetes:** `aromos.*` (10 paquetes).
- **Herencia, polimorfismo y clases abstractas:**
  - `Persona` y sus subclases `Vecino`, `Visitante` y `Personal` (que a su vez tiene `Administrador`, `Guardia` y `EmpleadoMantenimiento`), con `obtenerRol()` sobreescrito.
  - `Equipo` con `Camara` y `Luminaria`, que sobreescriben `prioridadFalla()`.
  - `MenuBase` con los menús de cada rol, que sobreescriben `titulo()`, `opciones()` y `ejecutarOpcion()`.
  - `Consola` y `ConsolaGrafica`, que sobreescribe los métodos que leen y muestran datos.
- **Encapsulamiento:** atributos `private` con getters, constructores `protected` en las clases abstractas, y el constructor de `Lote` sin `public` (solo lo usa `Barrio`).
- **Colecciones, `Comparable` y `Comparator`:** `ArrayList` en casi todo el modelo. `Comparable` en `Reclamo` (por número) y `Expensa` (por período). `Comparator` en `ComparadorReclamos`, `ComparadorLotesPorDeuda` y `ComparadorEquipos`, que se usan en los reportes.
- **Excepciones:** `try/catch` en controladores y menús, `finally` en `MenuPrincipal.ejecutar()` (guarda los datos siempre), `try-with-resources` en `ArchivoDatos` y excepciones propias con `throw/throws` en `aromos.excepciones`.
- **Entrada y salida de archivos:** `ArchivoDatos` guarda y carga todo el barrio con serialización (`datos/barrio.dat`).
- **Interfaz:** gráfica con Swing y manejo de eventos (`VentanaPrincipal`, `VentanaRol` y `ConsolaGrafica`: las ventanas implementan `ActionListener` para atender los botones y los datos se piden con cuadros `JOptionPane`), y además un menú por consola para cada rol (`aromos.ui`).
- **SOLID y GRASP en el código:** están explicados en `documentos/informe_final.md`, sección 2.
- **Pruebas:** `test/aromos/pruebas` (107 verificaciones) y `documentos/plan_de_pruebas.md`.

## Supuestos

Los de la etapa 1 se mantienen. Los que agregamos al programar están en `documentos/informe_final.md`, sección 4.
