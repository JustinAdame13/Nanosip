package mx.nanosip.nanosip.Controllers.Backend;
import java.time.LocalDateTime;

public class Ventas {

    private Integer numero;
    private Integer idEmpleado;
    private Integer idClientes;
    private LocalDateTime fecha;
    private double monto;

    public Ventas() {}

    public Ventas(Integer idEmpleado, Integer idClientes, double monto) {
        this.idEmpleado = idEmpleado;
        this.idClientes = idClientes;
        this.monto = monto;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Integer getIdClientes() {
        return idClientes;
    }

    public void setIdClientes(Integer idClientes) {
        this.idClientes = idClientes;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }
}
