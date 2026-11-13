package aromos.pruebas;

import aromos.controladores.ControladorExpensas;
import aromos.expensas.Expensa;
import aromos.expensas.MedioPago;
import aromos.expensas.Pago;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PruebasExpensas {

    public void ejecutar(Verificador v) throws Exception {
        calculoDeLaExpensa(v);
        interesPorMora(v);
        pagos(v);
        morosidad(v);
        ajustes(v);
        ordenNatural(v);
    }

    private void calculoDeLaExpensa(Verificador v) throws Exception {
        v.grupo("Expensas: importe = presupuesto x coeficiente");
        Escenario e = new Escenario();
        ControladorExpensas c = new ControladorExpensas(e.barrio);
        c.emitirExpensas(2026, 1, 1000000);
        v.verificarNumero("El lote 1 (1000 de 4000 m2) paga el 25%", 250000, e.lote1.obtenerExpensas().get(0).getImporte());
        v.verificarNumero("El lote 2 (3000 de 4000 m2) paga el 75%", 750000, e.lote2.obtenerExpensas().get(0).getImporte());
        v.verificarIgual("Vence el dia 10", LocalDate.of(2026, 1, 10), e.lote1.obtenerExpensas().get(0).getVencimiento());

        Exception error = null;
        try {
            c.emitirExpensas(2026, 1, 500000);
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("No se puede emitir dos veces el mismo periodo", error);
    }

    private void interesPorMora(Verificador v) throws Exception {
        v.grupo("Expensas: interes del 3% mensual");
        Escenario e = new Escenario();
        new ControladorExpensas(e.barrio).emitirExpensas(2026, 1, 1000000);
        Expensa expensa = e.lote1.obtenerExpensas().get(0); // importe 250000
        v.verificarNumero("Antes del vencimiento no hay interes", 0, expensa.calcularInteresMora(LocalDate.of(2026, 1, 5)));
        v.verificarNumero("Un dia despues del vencimiento: 1 mes = 3%", 7500, expensa.calcularInteresMora(LocalDate.of(2026, 1, 11)));
        v.verificarNumero("Un mes y un dia despues: 2 meses = 6%", 15000, expensa.calcularInteresMora(LocalDate.of(2026, 2, 11)));
    }

    private void pagos(Verificador v) throws Exception {
        v.grupo("Expensas: los pagos se aplican primero a intereses y despues a capital");
        Escenario e = new Escenario();
        ControladorExpensas c = new ControladorExpensas(e.barrio);
        c.emitirExpensas(2026, 1, 1000000);

        // el 11/02 hay 2 meses de atraso: interes 15000. Pago de 20000 = 15000 de interes + 5000 de capital
        c.registrarPago(e.lote1, 20000, MedioPago.EFECTIVO, LocalDate.of(2026, 2, 11));
        Expensa enero = e.lote1.obtenerExpensas().get(0);
        v.verificarNumero("Primero se paga el interes", 15000, enero.getInteresPagado());
        v.verificarNumero("Lo que sobra va al capital", 5000, enero.getCapitalPagado());

        // un pago puede cubrir mas de una expensa: se empieza por la mas antigua
        Escenario e2 = new Escenario();
        ControladorExpensas c2 = new ControladorExpensas(e2.barrio);
        c2.emitirExpensas(2026, 1, 1000000);
        c2.emitirExpensas(2026, 2, 1000000);
        c2.registrarPago(e2.lote1, 300000, MedioPago.TRANSFERENCIA, LocalDate.of(2026, 2, 5));
        v.verificar("El pago cancela la expensa de enero", e2.lote1.obtenerExpensas().get(0).estaPagada());
        v.verificarNumero("Y el resto se aplica a febrero", 42500, e2.lote1.obtenerExpensas().get(1).getCapitalPagado());

        Exception error = null;
        try {
            c2.registrarPago(e2.lote1, 99999999, MedioPago.EFECTIVO, LocalDate.of(2026, 2, 5));
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Un pago mayor que la deuda se rechaza", error);
    }

    private void morosidad(Verificador v) throws Exception {
        v.grupo("Expensas: mas de 3 periodos impagos = lote moroso");
        Escenario e = new Escenario();
        ControladorExpensas c = new ControladorExpensas(e.barrio);
        for (int mes = 1; mes <= 5; mes++) {
            c.emitirExpensas(2026, mes, 1000000);
        }
        v.verificarIgual("El 11/03 hay 3 periodos vencidos", 3, e.lote1.contarPeriodosImpagos(LocalDate.of(2026, 3, 11)));
        v.verificar("Con 3 periodos impagos NO es moroso", !e.lote1.determinarSiEsMoroso(LocalDate.of(2026, 3, 11)));
        v.verificar("Con 4 periodos impagos SI es moroso", e.lote1.determinarSiEsMoroso(LocalDate.of(2026, 4, 11)));

        LocalDate hoy = LocalDate.of(2026, 5, 11);
        c.verificarMorosidad(hoy);
        v.verificar("El lote queda marcado como moroso", e.lote1.isMoroso());
        v.verificarIgual("Se notifica al titular", 1, e.barrio.consultarNotificaciones(e.titular1).size());
        c.verificarMorosidad(hoy);
        v.verificarIgual("No se vuelve a notificar", 1, e.barrio.consultarNotificaciones(e.titular1).size());
    }

    private void ajustes(Verificador v) throws Exception {
        v.grupo("Expensas: un pago no se elimina, se corrige con un ajuste");
        Escenario e = new Escenario();
        ControladorExpensas c = new ControladorExpensas(e.barrio);
        c.emitirExpensas(2026, 1, 1000000);
        Pago pago = c.registrarPago(e.lote1, 1000, MedioPago.EFECTIVO, LocalDate.of(2026, 1, 5));

        c.corregirPago(e.lote1, pago, -400, "Se cargo de mas", LocalDate.of(2026, 1, 6));
        v.verificarIgual("El pago sigue registrado", 1, e.lote1.obtenerPagos().size());
        v.verificarNumero("El importe efectivo queda en 600", 600, pago.calcularImporteEfectivo());
        v.verificarNumero("La expensa vuelve a deber lo corregido", 249400,
                e.lote1.obtenerExpensas().get(0).calcularSaldoCapital());

        Exception error = null;
        try {
            c.corregirPago(e.lote1, pago, -50, "", LocalDate.of(2026, 1, 7));
        } catch (Exception ex) {
            error = ex;
        }
        v.verificarError("Un ajuste sin motivo se rechaza", error);
    }

    private void ordenNatural(Verificador v) throws Exception {
        v.grupo("Expensas: orden natural (Comparable) por periodo");
        Escenario e = new Escenario();
        ControladorExpensas c = new ControladorExpensas(e.barrio);
        c.emitirExpensas(2026, 3, 100000);
        c.emitirExpensas(2025, 12, 100000);
        List<Expensa> lista = new ArrayList<>(e.lote1.obtenerExpensas());
        Collections.reverse(lista);
        Collections.sort(lista);
        v.verificarIgual("Primero el periodo mas antiguo", "2025-12", lista.get(0).getPeriodo());
    }
}
