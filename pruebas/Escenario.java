package aromos.pruebas;

import aromos.excepciones.ReglaNegocioException;
import aromos.personas.Administrador;
import aromos.personas.Barrio;
import aromos.personas.EmpleadoMantenimiento;
import aromos.personas.Guardia;
import aromos.personas.Lote;
import aromos.personas.Puesto;
import aromos.personas.Vecino;
import java.time.LocalDate;

// Datos que usan todas las pruebas: un barrio chico con fechas fijas
// para que los resultados siempre sean los mismos.
// El reloj del barrio arranca el 2026-03-02 a las 9 hs.
public class Escenario {
    public Barrio barrio = new Barrio("Los Aromos (pruebas)", LocalDate.of(2026, 3, 2));

    public Administrador admin = new Administrador("Marta", "Gomez", "9001", "111", "m@mail.com", 1,
            LocalDate.of(2020, 1, 1));
    public EmpleadoMantenimiento electricista = new EmpleadoMantenimiento("Carlos", "Rios", "9002", "111",
            "c@mail.com", 21, LocalDate.of(2021, 1, 1), "electricidad");
    public EmpleadoMantenimiento plomero = new EmpleadoMantenimiento("Ana", "Suarez", "9003", "111",
            "a@mail.com", 22, LocalDate.of(2022, 1, 1), "plomeria");
    public Guardia porteria = new Guardia("Juan", "Perez", "9004", "111", "j@mail.com", 11,
            LocalDate.of(2022, 1, 1), "manana", Puesto.PORTERIA);
    public Guardia ronda = new Guardia("Luis", "Diaz", "9005", "111", "l@mail.com", 12,
            LocalDate.of(2022, 1, 1), "noche", Puesto.RONDA);

    public Vecino titular1 = new Vecino("Ana", "Gomez", "1001", "111", "ana@mail.com", true);
    public Vecino titular2 = new Vecino("Beto", "Lopez", "1002", "111", "beto@mail.com", true);
    public Vecino residenteAutorizado = new Vecino("Carla", "Gomez", "1003", "111", "carla@mail.com", true);
    public Vecino residenteSinAutorizar = new Vecino("Dario", "Perez", "1004", "111", "dario@mail.com", false);
    public Vecino ajeno = new Vecino("Eva", "Ruiz", "1005", "111", "eva@mail.com", true);

    // Lote 1: 1000 m2 (25% del barrio). Lote 2: 3000 m2 (75%).
    public Lote lote1;
    public Lote lote2;

    public Escenario() throws ReglaNegocioException {
        barrio.designarAdministrador(admin);
        barrio.registrarEmpleado(electricista);
        barrio.registrarEmpleado(plomero);
        barrio.registrarGuardia(porteria);
        barrio.registrarGuardia(ronda);
        barrio.registrarVecino(titular1);
        barrio.registrarVecino(titular2);
        barrio.registrarVecino(residenteAutorizado);
        barrio.registrarVecino(residenteSinAutorizar);
        barrio.registrarVecino(ajeno);
        lote1 = barrio.crearLote(1, 1000, titular1);
        lote2 = barrio.crearLote(2, 3000, titular2);
        lote1.agregarResidente(residenteAutorizado);
        lote1.agregarResidente(residenteSinAutorizar);
    }
}
