# Informe final - Etapa 2 (Los Aromos)

## 1. Cambios respecto del diseño original

La consigna pide actualizar el diagrama y justificar cada cambio, así que acá van los cambios que hicimos entre el diagrama final de la etapa 1 y el código. Los diagramas actualizados están en `documentos/uml`.

### 1.1 Cambios generales

- **Enums en lugar de String.** Los tipos, estados y prioridades (`TipoReclamo`, `EstadoReclamo`, `Prioridad`, `MedioPago`, `EstadoEquipo`, `Sector`, `PrioridadOrden`, `Puesto` y `TipoVisitante`) pasaron a ser enums. Así no se pueden cargar valores inválidos, y además `TipoReclamo` ya sabe su prioridad y `EstadoReclamo.siguiente()` resuelve la regla de que un reclamo solo avanza al estado siguiente. El turno y la especialidad quedaron como `String`.
- **Fechas y horas.** Las fechas son `LocalDate` en vez de `Date`. Las horas son un `int` que cuenta las horas de un reloj propio del sistema, que lleva `Barrio`. Hicimos esto para que las reglas de 24 horas, 48 horas y 5 días se resuelvan restando números, y para poder simular el paso del tiempo en la demostración.
- **Serializable.** Las clases del modelo implementan `Serializable` porque el programa guarda el `Barrio` completo en un archivo.
- **Encapsulamiento.** Los atributos de `Persona` y `Personal` pasaron de `protected` a `private` con getters. El `protected` quedó solo en los constructores de las clases abstractas.
- **Excepciones propias.** Agregamos `ReglaNegocioException`, `NoAutorizadoException` y `LimiteExcedidoException`, que es lo que pide la consigna para las reglas de negocio.

### 1.2 Cambios por clase

**Personas y lotes**

- `Barrio` pasó a ser la clase principal. Tiene las listas de lotes, vecinos, personal, equipos, órdenes, autorizaciones, registros y notificaciones, además del reloj y el método `crearLote()` (GRASP Creador). Lo hicimos así porque hace falta un solo objeto para guardar y cargar todo.
- `Lote` dejó de tener el coeficiente como atributo y ahora lo calcula con `calcularParticipacion()`. `crearReclamo(tipo, desc)` pasó a ser `crearReclamo(autor, tipo, desc, ahora)`, porque el reclamo necesita saber quién lo presentó. También sumamos `esTitularOAutorizado()`, `autorizarReclamante()` y los métodos de expensas y pagos. El mensaje `determinarSiEsMoroso()` del diagrama de "Consultar deudas" lo resuelve el propio lote (Experto).
- `Barrio` y `Lote` pasaron de agregación a **composición**. El lote solo lo crea `Barrio.crearLote()` (su constructor no es público) y necesita al barrio para calcular su coeficiente, así que no puede existir sin él. Los residentes siguen siendo agregación, porque cada `Vecino` se registra por separado.
- A `Vecino` le sacamos `presentarReclamo()`. El camino real es `ControladorReclamos` y después `Lote.crearReclamo()`, como en el diagrama de secuencia.
- A `Administrador` le sacamos `emitirReporte()`, porque los reportes los hace `ControladorReportes`.
- En `Personal` implementamos `actualizarReclamo()`: solo el responsable puede pasar su reclamo a EN_CURSO o RESUELTO.
- `EmpleadoMantenimiento` ahora tiene `puedeTomarReclamo()`, porque es quien conoce su límite de 5.
- En `Guardia` completamos `registrarIngreso()` y `verificarAutorizacion()`, que habían quedado sin hacer.

**Reclamos**

- `Reclamo` suma `debeEscalarse()`, `escalar()`, `debeCerrarse()` y `estaPendiente()` para las reglas automáticas del actor "Sistema" (escalado y cierre), e implementa `Comparable`.
- `Comentario` ahora guarda el autor, porque hay comentarios del vecino y del personal.

**Expensas y pagos**

- `Pago` pertenece al lote y guarda en qué expensas se aplicó y cuánto en cada una. Un pago puede cubrir varias expensas, y esto permite corregirlo con un ajuste sin borrarlo.
- `Expensa` ya no guarda el estado, lo calcula. Sumamos `calcularInteresMora()`, `calcularInteresPendiente()`, `calcularSaldo()` e `imputar()`, e implementa `Comparable` por período. La expensa calcula su propio interés (Experto).
- `PresupuestoMensual` cambió `periodo` por `anio` y `mes`, para poder calcular el vencimiento y ordenar sin interpretar texto.

**Mantenimiento y visitas**

- `Equipo` ahora recibe la cantidad de fallas del sector en `prioridadFalla()`, y suma `getTipo()` y `requiereAviso()`. La regla de "más de 3 luminarias del mismo sector" necesita ese dato, y cada subclase decide sin usar `if` por tipo (polimorfismo).
- `OrdenMantenimiento` suma número, equipo y `asignarEmpleado()`, y `cerrarOrden()` valida fecha, técnico y descripción.
- `Autorizacion` y `RegistroAcceso` validan en el constructor y controlan que no haya un ingreso abierto.
- `Notificacion` es una clase nueva para los avisos que se muestran dentro del sistema (era un supuesto de la etapa 1).

**Controladores, persistencia e interfaz**

- Sumamos `ControladorMantenimiento`, `ControladorVisitas` y `ControladorReportes`, más tres clases `Comparador...` (GRASP Controlador).
- `ControladorBarrio` lo agregamos el 12/11/2026. Antes, las altas de vecinos, lotes, residentes y personal, la autorización de reclamantes y el avance del reloj las hacían los menús directamente sobre `Barrio`, creando los objetos ahí mismo. Ahora los menús y las ventanas se lo piden al controlador.
- `ArchivoDatos` y `DatosPrueba` son nuevas: guardan y cargan con serialización (requisito de entrada y salida) y cargan los datos de ejemplo.
- La "pantalla" del diseño pasó a ser un menú por consola para cada rol (`Consola`, `MenuBase`, `MenuPrincipal`, `MenuVecino`, `MenuAdministrador`, `MenuGuardia` y `MenuEmpleado`), porque la consigna admite consola con menú.
- Después sumamos la interfaz gráfica con Swing (`ConsolaGrafica`, `VentanaPrincipal`, `VentanaRol` y `OperacionCancelada`), con fondo blanco y un botón por opción. `MenuBase` ahora declara `titulo()`, `opciones()` y `ejecutarOpcion()`, y `ConsolaGrafica` hereda de `Consola` y pide los datos con cuadros de diálogo. Las ventanas implementan `ActionListener` para atender los botones. La profesora pidió interfaces gráficas con manejo de eventos, y para no duplicar código la consola y las ventanas usan los mismos menús y los mismos controladores.

### 1.3 Diagramas actualizados

Todos están en `documentos/uml`, cada uno con el archivo editable `.drawio` (se abre en app.diagrams.net) y la imagen `.png`. Los diagramas de clases llevan su nota de cambios.

- `01_personas_lotes_final`: personas, herencia, `Lote` y `Barrio` (con la composición y los residentes como agregación).
- `02_reclamos_final`: reclamos, historial, comentarios y enums.
- `03_expensas_pagos_final`: expensas, pagos, ajustes y presupuesto.
- `04_mantenimiento_final`: `Equipo`, `Camara`, `Luminaria` y órdenes.
- `05_visitas_autorizaciones_final`: autorizaciones y registros de acceso.
- `06_controladores_reportes`: controladores (incluido `ControladorBarrio`) y comparadores. Es nuevo.
- `07_menus_persistencia_excepciones`: menús, ventanas Swing, consola gráfica, archivo de datos y excepciones. También es nuevo, y lo actualizamos el 11/11/2026 con la interfaz gráfica.
- `08_secuencia_consultar_deudas` y `09_secuencia_presentar_reclamo`: los dos diagramas de secuencia actualizados.
- `UC01` a `UC05` y `UC_general_sistema`: los casos de uso, sin cambios respecto de la etapa 1.

## 2. SOLID y GRASP en el código

La consigna pide que los patrones elegidos en la etapa 1 se vean en el código y no solo en el informe. Así quedó:

### SOLID

- **SRP (responsabilidad única).** Separamos en capas. `aromos.ui` solo muestra y pide datos (el menú recorre las expensas para mostrarlas y suma los totales, pero no decide ninguna regla), `aromos.controladores` coordina cada caso de uso y las clases del modelo tienen las reglas. `ArchivoDatos` es lo único que sabe cómo se guarda todo.
- **OCP (abierto/cerrado).** `Equipo` es abstracta y cada subclase define su `prioridadFalla()`. Si hubiera que agregar otro tipo de equipo, se crea una subclase y `ControladorMantenimiento` no cambia.
- **LSP (sustitución de Liskov).** `Reclamo` guarda un `Personal` como responsable y `Personal.actualizarReclamo()` funciona igual con cualquier subtipo. Los controladores y menús usan `Persona` o `Personal` sin preguntar cuál es el tipo concreto.

### GRASP

- **Experto.** `Expensa.calcularInteresMora()` (la expensa tiene el importe y el vencimiento), `Reclamo.debeEscalarse()` y `EmpleadoMantenimiento.puedeTomarReclamo()`.
- **Controlador.** `ControladorReclamos`, `ControladorExpensas`, `ControladorMantenimiento`, `ControladorVisitas`, `ControladorReportes` y `ControladorBarrio`. Todo lo que cambia datos desde un menú o una ventana pasa por uno de ellos. Los menús solo consultan al `Barrio` para buscar lo que la persona eligió (un lote, un reclamo, un equipo) y mostrarlo.
- **Polimorfismo.** `Equipo.prioridadFalla()` y `Camara.requiereAviso()`, y también `Persona.obtenerRol()`. No hay `if` según el tipo de equipo ni de persona.
- **Creador.** `Lote.crearReclamo()`, `Lote.emitirExpensa()`, `Lote.registrarPago()` y `Barrio.crearLote()`: crea quien agrupa o contiene. Las personas (`Vecino`, `Guardia`, `Empleado`) las crea `ControladorBarrio` al darlas de alta, porque existen antes de entrar al barrio y no dependen de él.

### Los diagramas de secuencia y el código

- **Consultar deudas.** `ControladorExpensas.consultarDeudas()` busca el lote y devuelve las expensas sin pagar. Después el menú calcula con `Expensa.calcularInteresPendiente()` y consulta `Lote.isMoroso()` (el estado de moroso lo mantiene el sistema con `Lote.determinarSiEsMoroso()`).
- **Presentar reclamo** (con `alt` y flujo de error). `ControladorReclamos.presentarReclamo()` llama a `Lote.esTitularOAutorizado()`. Si da `false`, lanza `NoAutorizadoException`. Si da `true`, el lote crea el `Reclamo` en estado ABIERTO con `Lote.crearReclamo()`.

## 3. Dificultades encontradas

- **Aprender Java desde cero.** Los dos empezamos con Java al comienzo del cuatrimestre, así que aprendimos un lenguaje nuevo mientras armábamos el trabajo. Al principio nos costó la sintaxis, entender cómo se organizan los paquetes y usar bien herencia, clases abstractas y colecciones. Varias cosas las escribimos, las probamos y las tuvimos que rehacer cuando entendimos mejor cómo funcionaban.
- **Reglas que dependen del tiempo.** Probar "24 horas", "48 horas" o "5 días" esperando no se puede. Lo resolvimos con un reloj propio del sistema que cuenta horas y que se puede avanzar desde el menú.
- **Pagos parciales y correcciones.** Un pago puede cubrir varias expensas y después hay que poder corregirlo sin borrarlo. La solución fue que cada `Pago` recuerde en qué expensas se aplicó y cuánto, así se puede devolver ese capital cuando el ajuste es hacia abajo.
- **Guardar las relaciones entre objetos.** `Lote`, `Reclamo`, `Barrio` y las demás se referencian entre sí. Con serialización se guardan solas; con archivos de texto habría que reconstruirlas a mano.
- **Pasar del modelo al código.** Al programar vimos que algunas cosas del diagrama de la etapa 1 no funcionaban igual en la práctica, por ejemplo quién crea los lotes o dónde se controla el límite de reclamos. Lo ajustamos y dejamos los cambios en la sección 1.
- **Sumar la interfaz gráfica.** Hacer las ventanas con Swing fue nuevo para nosotros. Para no duplicar código reutilizamos los menús de la consola y armamos las ventanas encima.
- **Organización y tiempos.** Coordinar entre los dos y llegar a las fechas de entrega en medio de otras materias nos obligó a repartirnos el trabajo y a dejar lo más pesado (pruebas y documentación) para el final.

## 4. Supuestos agregados en la etapa 2

1. Se ingresa con el documento (vecinos) o el legajo (personal), sin contraseña.
2. Una "persona autorizada" a reclamar es un residente del lote (familiar o inquilino) que el titular habilitó.
3. Los reclamos "en curso" de un empleado cuentan los ASIGNADOS y los EN_CURSO.
4. "Atendido" (regla de las 24 horas) significa que el reclamo ya no está ABIERTO. Se escala una sola vez.
5. Reabrir un reclamo resuelto es la única vez que se retrocede de estado: vuelve a EN_CURSO. Se puede hasta exactamente 5 días después de resolverlo.
6. La expensa de un período vence el día 10 del mismo mes del período.
7. El interés es el 3 % del capital impago por cada mes empezado después del vencimiento (un día tarde cuenta como un mes).
8. No se aceptan pagos ni ajustes mayores que la deuda del lote.
9. Un período impago es una expensa vencida sin pagar. El lote es moroso con más de 3 y deja de serlo cuando no le quedan períodos vencidos sin pagar. La notificación se manda una sola vez.
10. El coeficiente de un lote es su superficie dividida por la superficie total del barrio.
11. El reporte de "equipos fuera de servicio" incluye todo equipo que no está operativo (con falla, en reparación o fuera de servicio).
12. El aviso por una cámara se manda una sola vez por falla, cuando lleva más de 48 horas sin funcionar.
13. Solo el guardia de portería registra ingresos.
14. Una autorización común vale un día. La recurrente es solo para proveedores habituales y dura hasta 30 días. La puede dar el titular o un residente del lote.
15. La franja horaria de una autorización son horas enteras de 0 a 23.
16. Una orden de mantenimiento solo la cierra el empleado asignado. Un equipo no puede tener dos órdenes abiertas a la vez.
17. El reloj del sistema arranca el día de la primera ejecución a las 9:00 y solo avanza con la opción "Simular el paso del tiempo".
18. Las altas del administrador (vecinos, lotes, residentes y personal) son funciones de apoyo: sirven para cargar los datos que necesitan los cinco casos de uso, pero no tienen un caso de uso propio en el diagrama.

## 5. Integrantes

- Nahuel Escalante
- Martina Romero

## 6. Reflexión del grupo

Hacer este trabajo nos ayudó a entender mejor la programación orientada a objetos. Pasar de un enunciado a diagramas, y de los diagramas a un programa que funciona, nos mostró para qué sirve cada concepto de la materia.

Aprendimos herencia y polimorfismo, colecciones con `Comparable` y `Comparator`, excepciones propias para las reglas de negocio y guardado de datos en archivos. También aprendimos a escribir pruebas para encontrar errores antes de la entrega.

Lo que más nos sirvió fue separar las clases en controladores y modelo: cuando una regla cambiaba, sabíamos dónde tocar. El polimorfismo en los equipos (cámaras y luminarias) nos evitó llenar el código de `if`.

Si lo hiciéramos de nuevo, empezaríamos antes con las pruebas y con el repositorio, haríamos commits más chicos y seguidos, y pensaríamos mejor el modelo antes de programar para no tener que corregir tanto después.

Nos ayudamos con Claude (inteligencia artificial) para resolver dudas, revisar el código y armar parte de la documentación. Lo revisamos entre los dos y nos aseguramos de poder explicar y modificar todo lo que entregamos.
