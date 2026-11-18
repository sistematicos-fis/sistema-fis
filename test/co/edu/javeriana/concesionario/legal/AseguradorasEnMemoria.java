package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;
import co.edu.javeriana.concesionario.legal.persistencia.AseguradoraRepositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Repositorio de aseguradoras en memoria para las pruebas de CU-23 y CU-24. */
class AseguradorasEnMemoria implements AseguradoraRepositorio {
    final List<Aseguradora> aseguradoras = new ArrayList<>();

    Aseguradora agregar(long id, String codigo, boolean cubreSoat, boolean cubreRc, boolean activa) {
        Aseguradora a = new Aseguradora(id, codigo, "Aseguradora " + codigo, "900.000.00" + id + "-1", null,
                cubreSoat, cubreRc, activa);
        aseguradoras.add(a);
        return a;
    }

    @Override public Aseguradora guardar(Aseguradora a) { aseguradoras.add(a); return a; }
    @Override public void actualizar(Aseguradora a) { }
    @Override public void eliminar(Long id) { aseguradoras.removeIf(a -> a.getId().equals(id)); }
    @Override public List<Aseguradora> listarTodas() { return new ArrayList<>(aseguradoras); }
    @Override public List<Aseguradora> listarActivas() { return aseguradoras.stream().filter(Aseguradora::isActivo).toList(); }
    @Override public Optional<Aseguradora> buscarPorId(Long id) { return aseguradoras.stream().filter(a -> a.getId().equals(id)).findFirst(); }
    @Override public Optional<Aseguradora> buscarPorCodigo(String codigo) { return aseguradoras.stream().filter(a -> a.getCodigo().equals(codigo)).findFirst(); }
    @Override public Optional<Aseguradora> buscarPorNit(String nit) { return aseguradoras.stream().filter(a -> a.getNit().equals(nit)).findFirst(); }
}
