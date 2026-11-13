# Plan de pruebas - Los Aromos

Las pruebas unitarias se corrieron el 02/11/2026 sobre el código de la entrega (Java 25, Windows 11). La parte de la interfaz gráfica (sección 3) se hizo el 11/11/2026.

## 1. Pruebas unitarias

JUnit no lo vimos en clase, así que armamos una clase simple propia (`Verificador`). Cada verificación imprime `[OK]` o `[FALLA]` y al final muestra un resumen.

| Clase de prueba | Qué prueba | Verificaciones |
|---|---|---|
| `PruebasReclamos` | prioridad según el tipo, estados que solo avanzan, quién puede reclamar, límite de 5 reclamos, reapertura y cierre a los 5 días, escalado a las 24 horas, orden natural | 25 |
| `PruebasExpensas` | importe según el coeficiente, interés del 3 % mensual, pagos (primero interés y después capital, varias expensas, pago mayor a la deuda), morosidad, ajustes, orden natural | 23 |
| `PruebasMantenimiento` | prioridad de las órdenes (cámara siempre alta, luminaria alta si hay más de 3 fallas en el sector), cierre de órdenes, aviso de cámaras a las 48 horas | 16 |
| `PruebasVisitas` | ingreso y egreso, sin autorización, fuera de horario, ingreso sin egreso, autorizaciones recurrentes de hasta 30 días | 13 |
| `PruebasGestion` | altas de vecinos, lotes, residentes y personal (con sus errores), autorización para reclamar, avance del reloj | 20 |
| `PruebasPersistencia` | guardar y cargar el barrio, que los cambios se conserven, archivo inexistente | 10 |
| Total | | 107 |

Se ejecutan desde IntelliJ corriendo `test/aromos/pruebas/EjecutarPruebas.java`, o por consola desde la carpeta del proyecto con `java -cp out aromos.pruebas.EjecutarPruebas` (los pasos para compilar están en el README).

Resultado: 107 verificaciones, 107 OK, 0 fallas. Las primeras 87 se corrieron el 02/11 y se repitieron el 11/11 después de agregar la interfaz gráfica, con el mismo resultado. Las 20 de `PruebasGestion` se agregaron el 12/11 junto con `ControladorBarrio` y se corrieron todas juntas.

Estas pruebas no cubren las clases de `aromos.ui`; eso está en la sección 3. Usan fechas y horas fijas (el reloj del barrio de prueba arranca el 02/03/2026 a las 9 hs), así que siempre dan lo mismo.

## 2. Pruebas funcionales

Las hicimos a mano en el programa, con el menú por consola y partiendo de los datos de prueba del README. Los intereses y las fechas dependen del día en que se ejecuta; los valores de abajo son del 02/11/2026.

### CU01 - Reclamos y solicitudes

| ID | Caso | Entrada | Esperado | Obtenido | Estado |
|---|---|---|---|---|---|
| F01 | El titular presenta un reclamo de seguridad | Vecino `30000002` → Presentar un reclamo → SEGURIDAD → "Robo de bicicleta en la calle" | Se crea ABIERTO con prioridad ALTA | `Reclamo #3 registrado. Prioridad ALTA, estado ABIERTO.` y aparece en Ver mis reclamos | OK |
| F02 | Un residente sin autorización intenta reclamar (flujo de error del diagrama de secuencia) | Vecino `30000012` (Elena) → Presentar un reclamo → CONVIVENCIA | Se rechaza y no se crea | `[ERROR] Solo el titular del lote 2 o un residente autorizado por el titular puede presentar reclamos` | OK |
| F03 | Límite de 5 reclamos por empleado | Vecino `30000002` crea 5 reclamos (#3 a #7); el administrador asigna #3 a #7 a Ana Suarez, que ya tenía el #2 | Se asignan 4 y el siguiente se rechaza | #3, #4, #5 y #6 asignados; en el #7: `[ERROR] Ana Suarez ya tiene 5 reclamos en curso` | OK |
| F04 | Un estado no se puede saltear | Empleado `22` → reclamo #2 (ASIGNADO) → RESUELTO | Se rechaza | `[ERROR] Un reclamo ASIGNADO solo puede pasar a EN_CURSO, no a RESUELTO` | OK |
| F05 | Escalado a las 24 horas | Simular 25 horas con el reclamo #1 (prioridad ALTA) sin atender | El sistema lo escala y avisa al administrador | `[SISTEMA] Reclamo #1 de prioridad ALTA sin atender hace mas de 24 horas (lote 1): escalado al administrador`; el administrador ve la notificación | OK |
| F06 | Reabrir un reclamo resuelto | Empleado `22` pone #2 EN_CURSO y RESUELTO; vecino `30000002` → Reabrir #2 | Vuelve a EN_CURSO | `Reclamo #2 reabierto: vuelve a EN_CURSO.` | OK |
| F07 | Cierre automático a los 5 días | Igual que F06 pero sin reabrir; simular 126 horas | El reclamo queda CERRADO y el historial lo registra | `[SISTEMA] Reclamo #2 cerrado automaticamente (pasaron 5 dias desde que se resolvio)` y en el historial `CERRADO (Cierre automatico: vencio el plazo de 5 dias)` | OK |

### CU02 - Expensas, pagos y mora

| ID | Caso | Entrada | Esperado | Obtenido | Estado |
|---|---|---|---|---|---|
| F08 | Emitir expensas | Administrador → Emitir las expensas → 2026, mes 12, $2.000.000 | Una expensa por lote que vence el día 10 | `Se emitieron 12 expensas (vencen el dia 10).` | OK |
| F09 | Consultar deudas con interés | Administrador → Consultar deudas → lote 6 | Interés del 3 % por cada mes de atraso | Sep: capital $208092.49 + interés $12485.55 (2 meses); Oct: interés $6242.77 (1 mes); Nov: interés $0.0 (vence el 10); total $643005.79 | OK |
| F10 | Pago parcial | Administrador → Registrar el pago → lote 6 → $100.000 en efectivo | Se aplica primero al interés y después al capital, empezando por el período más viejo | Septiembre queda con capital $120578.04 (se pagaron $12485.55 de interés y $87514.45 de capital); octubre y noviembre sin cambios | OK |
| F11 | Pago mayor que la deuda | Lote 6 → $99.999.999 | Se rechaza | `[ERROR] El importe supera la deuda del lote` | OK |
| F12 | Corregir un pago con un ajuste | Lote 6 → pago de $100.000 → Corregir un pago → -$40.000, motivo "Error de carga" | El pago no se elimina; su importe efectivo baja y la deuda sube | `El pago no se elimino: ... importe efectivo $60000.0 tras 1 ajuste(s)`; el capital de septiembre vuelve a $160578.04 | OK |
| F13 | Lote moroso y notificación formal | Vecino `30000005` → Ver mis notificaciones | Notificación por más de 3 períodos impagos | `NOTIFICACION FORMAL: el lote 5 tiene 4 periodos impagos y fue marcado como moroso` | OK |

### CU03 - Luminarias y cámaras

| ID | Caso | Entrada | Esperado | Obtenido | Estado |
|---|---|---|---|---|---|
| F14 | Falla de cámara, orden y reparación | Guardia `11` reporta `CAM-01`; el administrador genera y asigna la orden; el empleado `21` intenta cerrarla sin descripción y después con descripción | Orden de prioridad ALTA; sin descripción no se cierra; al cerrarla el equipo vuelve a OPERATIVO | `Camara CAM-01 ahora esta CON_FALLA`; `Orden #1 [ABIERTA] prioridad ALTA`; `[ERROR] Falta la descripcion del trabajo realizado`; luego `Orden #1 cerrada. El equipo CAM-01 vuelve a estar OPERATIVO.` | OK |
| F15 | Prioridad de las luminarias según el sector | Con `LUM-03` fallando se genera su orden; después fallan `LUM-01`, `LUM-02` y `LUM-04` (4 en `CALLES_INTERNAS`) y se genera la de `LUM-04` | La primera es NORMAL; con más de 3 en el sector pasa a ALTA | `Orden #1 ... Luminaria LUM-03 ... prioridad NORMAL`; `Orden #2 ... Luminaria LUM-04 ... prioridad ALTA` | OK |
| F16 | Cámara sin funcionar más de 48 horas | Guardia reporta `CAM-02`; simular 50 horas; administrador → notificaciones | Aviso al administrador | `[SISTEMA] Camara CAM-02 (CALLES_INTERNAS) lleva mas de 48 horas sin funcionar`, visible en las notificaciones | OK |

### CU04 - Ingreso de visitas

| ID | Caso | Entrada | Esperado | Obtenido | Estado |
|---|---|---|---|---|---|
| F17 | Ingreso y egreso con autorización vigente | Guardia `11` → ingreso `40111222`; ver personas adentro; egreso | Se registran las dos horas | `Ingreso habilitado a las 9 hs.`; figura "sigue adentro"; después del egreso ya no hay nadie dentro | OK |
| F18 | Sin autorización no se habilita | Guardia `11` → ingreso `99999999` | Se rechaza | `[ERROR] No hay autorizacion vigente para el documento 99999999: no se habilita el ingreso` | OK |
| F19 | Segundo ingreso sin egreso | Guardia `11` → ingreso `40111222` dos veces seguidas | El segundo queda bloqueado | `[ERROR] La persona ya tiene un ingreso abierto: primero hay que registrar su egreso` | OK |
| F20 | Autorización recurrente de más de 30 días | Vecino `30000001` → Autorizar una visita → proveedor habitual, recurrente, hasta 2026-12-10 | Se rechaza | `[ERROR] La autorizacion recurrente no puede superar los 30 dias` | OK |

### CU05 y persistencia

| ID | Caso | Entrada | Esperado | Obtenido | Estado |
|---|---|---|---|---|---|
| F21 | Reportes del administrador | Reclamos pendientes, Equipos fuera de servicio y Morosidad por lote | Listados actualizados | Reclamos #1 (alta) y #2 (media); equipo `LUM-03 CON_FALLA`; morosidad: lote 5 ($596531.78, 4 períodos, MOROSO) y lote 6 ($434913.3, 2 períodos) | OK |
| F22 | Los datos se conservan entre ejecuciones | Ejecución 1: vecino `30000003` presenta un reclamo y se sale. Ejecución 2: administrador → reclamos pendientes | El reclamo está en la segunda ejecución | `Datos cargados desde datos/barrio.dat` y aparece `#3 [ABIERTO] MANTENIMIENTO ... lote 3 - Pozo en la calle` | OK |
| F23 | Archivo de datos ilegible | `datos/barrio.dat` con contenido inválido | El programa avisa y no se cae | `[ERROR] No se pudo leer el archivo (invalid stream header...). Se usan los datos de prueba.` | OK |

### Lo que no probamos a mano

- Reabrir un reclamo fuera de plazo: por el menú no se llega, porque al avanzar el reloj el sistema ya lo cerró. Esa regla sí está en las pruebas unitarias.
- Estas pruebas funcionales se hicieron por consola. La interfaz gráfica se prueba en la sección 3.

## 3. Pruebas de la interfaz gráfica (Swing)

Las hicimos el 11/11/2026 (Java 25, Windows 11) con los datos de prueba del README y el reloj en 2026-11-11 09:00.

Cómo se hicieron: armamos un programa de prueba que abre las ventanas reales (`VentanaPrincipal` y `VentanaRol`), aprieta los botones con `doClick()` y contesta los cuadros de diálogo por programa (escribe el texto, elige un elemento de la lista o aprieta Cancelar). Después lee el área de texto de la ventana y la compara con lo esperado. Se usan las mismas clases y los mismos eventos que cuando lo usa una persona, pero no es una persona haciendo clic. También sacamos capturas de la ventana principal y de la del administrador y las revisamos a mano para ver el aspecto (fondo blanco, botones a la izquierda, resultados a la derecha).

| ID | Caso | Entrada | Esperado | Obtenido | Estado |
|---|---|---|---|---|---|
| G01 | Ingresar con un documento válido | Ventana principal → Ingresar como VECINO → `30000005` | Se abre la ventana del vecino, con un botón por opción | Se abrió una ventana de rol más (de 2 a 3 abiertas) | OK |
| G02 | Ingresar con un documento inexistente | Ingresar como VECINO → `99999999` | Error y no se abre ninguna ventana | Cuadro de error y `[ERROR] No hay ningun vecino con ese documento.`; no se abrió ninguna ventana | OK |
| G03 | Presentar un reclamo con listas y texto | Vecino `30000001` → Presentar un reclamo o solicitud → lote (lista) → tipo SEGURIDAD (lista) → "Prueba desde la GUI" | Se crea ABIERTO con prioridad ALTA | `[OK] Reclamo #3 registrado. Prioridad ALTA, estado ABIERTO.`; aparece en Ver mis reclamos junto al #1 | OK |
| G04 | Dato inválido en un cuadro | Ver mis reclamos → número de reclamo: `abc` y después `0` | Error, vuelve a pedir el dato y después sigue | `[ERROR] Ingrese un numero entero.` (cuadro de error); al ingresar `0` terminó normalmente | OK |
| G05 | Cancelar un cuadro de texto | Ver mis reclamos → Cancelar en el número de reclamo | La operación se corta sin error | `[AVISO] Operacion cancelada.` | OK |
| G06 | Cancelar una lista desplegable | Consultar deudas de expensas → Cancelar al elegir el lote | La operación se corta sin error | `[AVISO] Operacion cancelada.` | OK |
| G07 | Consultar deudas desde la ventana del vecino | Vecino `30000001` → Consultar deudas de expensas | Muestra las deudas del lote 1 | `Deudas del lote 1 al 2026-11-11` y `(no tiene deudas pendientes)`, total `$0.0` | OK |
| G08 | Avanzar el reloj desde la ventana principal | Simular el paso del tiempo → `25` | Avanza la fecha, se actualiza la etiqueta y el sistema escala los reclamos de prioridad alta | `[OK] Fecha del sistema: 2026-11-12 10:00`; `[SISTEMA] Reclamo #1 ... escalado al administrador` y lo mismo para el #3; la etiqueta de arriba muestra la nueva fecha | OK |
| G09 | Reportes del administrador | Administrador → Reclamos pendientes, Morosidad por lote y Estado de luminarias y cámaras | Listados con los datos de prueba | Reclamos #1 (alta) y #2 (media); lote 5 ($756069.35, 5 períodos, MOROSO) y lote 6 ($661734.11, 3 períodos); `LUM-03 CON_FALLA` | OK |
| G10 | Detalle de un reclamo | Administrador → Ver el detalle de un reclamo → `1` | Muestra el reclamo escalado y su historial | `#1 [ABIERTO] SEGURIDAD (prioridad ALTA) ESCALADO`, presentado por Laura Fernandez, historial con `ABIERTO (Reclamo presentado)` | OK |
| G11 | Submenú de altas y regla de negocio | Administrador → Altas → Registrar un vecino (doc `55555555`); repetir con el mismo documento; Cancelar para salir del submenú | La primera vez se registra, la segunda se rechaza y Cancelar vuelve a la ventana | `[OK] Vecino registrado.`; después `[ERROR] Ya hay un vecino con documento 55555555` (cuadro de error) | OK |
| G12 | Los botones siguen andando después de un error o una cancelación | Administrador → Equipos fuera de servicio, después de G11 | Responde normalmente | Lista `LUM-03 CON_FALLA` | OK |
| G13 | El modo consola sigue funcionando | `java -cp out aromos.Main --consola`, con entradas por teclado: un dato inválido, un documento inexistente y salir con `0` | Muestra el menú de consola, valida los datos y guarda al salir | Menú de consola con las seis opciones, `[ERROR] No hay ningun vecino con ese documento.` y `Datos guardados. Hasta luego.` | OK |

### Lo que no probamos en la interfaz gráfica

- Con una persona haciendo clic: solo se verificó de forma automática y con las capturas.
- Las ventanas del guardia y del empleado, y el ingreso como administrador, guardia o empleado desde la ventana principal. Usan el mismo código que las ventanas probadas (`VentanaRol` genera los botones de cualquier menú), pero no apretamos sus botones.
- El botón Cerrar sesion. Los datos se guardan después de cada acción en la ventana; la lógica de guardado se probó por consola (F22), pero no verificamos el archivo desde la ventana.
