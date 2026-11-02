package co.edu.javeriana.concesionario.comun.seguridad.modelo;

import java.util.EnumSet;
import java.util.Set;

public enum Rol {
    ADMINISTRADOR("Administrador", EnumSet.allOf(Modulo.class)),
    ASESOR_COMERCIAL("Asesor Comercial", EnumSet.of(Modulo.CLIENTES, Modulo.FINANCIACION, Modulo.ALQUILER)),
    ANALISTA_CREDITO("Analista de Crédito", EnumSet.of(Modulo.FINANCIACION)),
    ANALISTA_SERVICIO_CLIENTE("Analista de Servicio al Cliente", EnumSet.of(Modulo.CLIENTES)),
    ADMINISTRADOR_BODEGA("Administrador de Bodega", EnumSet.of(Modulo.INVENTARIO)),
    ANALISTA_DOCUMENTACION_LEGAL("Analista de Documentación Legal", EnumSet.of(Modulo.LEGAL));

    private final String nombre;
    private final Set<Modulo> modulosPermitidos;

    Rol(String nombre, Set<Modulo> modulosPermitidos) {
        this.nombre = nombre;
        this.modulosPermitidos = modulosPermitidos;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean puedeAcceder(Modulo modulo) {
        return modulosPermitidos.contains(modulo);
    }
}
