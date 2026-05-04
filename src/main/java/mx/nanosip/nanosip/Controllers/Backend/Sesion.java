package mx.nanosip.nanosip.Controllers.Backend;

public class Sesion {
    private static Sesion instancia;
    private Empleados usuarioActual;

    // Constructor privado para que nadie más pueda crear instancias
    private Sesion() {}

    // Método para obtener la única instancia (Singleton)
    public static Sesion getInstance() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }

    public Empleados getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Empleados usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }
}