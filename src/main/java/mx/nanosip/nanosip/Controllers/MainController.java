package mx.nanosip.nanosip.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import mx.nanosip.nanosip.App;

import java.io.IOException;

public class MainController {

    @FXML private Button btnRegresar;
    @FXML private Button btnEmpleados;
    @FXML private Button btnVentas;
    @FXML private Button btnClientes;
    @FXML private Button btnProductos;
    @FXML private Button btnProveedores;

    @FXML public void VolverLogin(){
        try {
            App.setRoot("Login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void VentanaEmpleados(){
        try {
            App.setRoot("Empleados.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @FXML
    public void VentanaVentas(){

    }
    @FXML
    public void VentanaClientes(){

    }
    @FXML
    public void VentanaProductos(){

    }
    @FXML
    public void VentanaProveedores(){

    }

}
