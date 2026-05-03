package mx.nanosip.nanosip.Controllers.Backend;

public class ProductosProveedores {
    private int claveProducto;
    private int idProveedor;

    // Getters
    public int getClaveProducto() { return claveProducto; }
    public int getIdProveedor() { return idProveedor; }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public void setClaveProducto(int claveProducto) {
        this.claveProducto = claveProducto;
    }
}