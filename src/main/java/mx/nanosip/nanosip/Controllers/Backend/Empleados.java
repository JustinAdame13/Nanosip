package mx.nanosip.nanosip.Controllers.Backend;


public class Empleados {

    private transient Integer id;
    private String nombre;
    private String puesto;
    private String rfc;
    private String curp;
    private byte edad;
    private String contrasena;
    private String permisos;

    public Empleados() {
    }

    public Empleados(String nombre, String puesto, byte edad, String rfc, String curp, String contrasena, String permisos) {
        this.nombre = nombre;
        this.puesto = puesto;
        this.edad   = edad;
        this.rfc    = rfc;
        this.curp   = curp;
        this.contrasena=contrasena;
        this.permisos=permisos;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp;
    }

    public byte getEdad() {
        return edad;
    }

    public void setEdad(byte edad) {
        this.edad = edad;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getPermisos() {
        return permisos;
    }

    public void setPermisos(String permisos) {
        this.permisos = permisos;
    }


}
