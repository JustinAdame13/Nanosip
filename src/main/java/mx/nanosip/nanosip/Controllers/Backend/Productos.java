package mx.nanosip.nanosip.Controllers.Backend;

public class Productos {

    private Integer clave;
    private String nombre;
    private String marca;
    private String descripcion;
    private int inventario;
    private double precio;
    private double costo;

    public Productos() {}

    public Productos(String nombre, String marca, String descripcion,
                     int inventario, double precio, double costo) {
        this.nombre = nombre;
        this.marca = marca;
        this.descripcion = descripcion;
        this.inventario = inventario;
        this.precio = precio;
        this.costo = costo;
    }

    public Integer getClave() {
        return clave;
    }

    public void setClave(Integer clave) {
        this.clave = clave;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getInventario() {
        return inventario;
    }

    public void setInventario(int inventario) {
        this.inventario = inventario;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

}
