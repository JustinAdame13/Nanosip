package mx.nanosip.nanosip.Controllers.Backend;

public class VentasProductos {
    private Integer numeroVenta;
    private Integer claveProducto;
    private Integer cantidad;

    public VentasProductos() {}

    public VentasProductos(Integer numeroVenta, Integer claveProducto, Integer cantidad) {
        this.numeroVenta = numeroVenta;
        this.claveProducto = claveProducto;
        this.cantidad = cantidad;
    }

    // Getters y Setters
    public Integer getNumeroVenta() { return numeroVenta; }
    public Integer getClaveProducto() { return claveProducto; }
    public Integer getCantidad() { return cantidad; }

    public void setNumeroVenta(Integer numeroVenta) {
        this.numeroVenta = numeroVenta;
    }

    public void setClaveProducto(Integer claveProducto) {
        this.claveProducto = claveProducto;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
