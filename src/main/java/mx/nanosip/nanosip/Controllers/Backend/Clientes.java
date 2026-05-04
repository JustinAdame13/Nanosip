package mx.nanosip.nanosip.Controllers.Backend;

public class Clientes {
    private Integer id;
    private String nombre;
    private String rfc;
    private String telefono;

    public Clientes() {}

    public Clientes(String nombre, String rfc, String telefono) {
        this.nombre = nombre;
        this.rfc = rfc;
        this.telefono = telefono;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
