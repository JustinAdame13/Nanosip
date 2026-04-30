package mx.nanosip.nanosip.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import mx.nanosip.nanosip.App;
import mx.nanosip.nanosip.WindowDragUtil;

import java.io.IOException;

public class MainController {

    @FXML private Button btnRegresar;
    @FXML private Button btnEmpleados;
    @FXML private Button btnVentas;
    @FXML private Button btnClientes;
    @FXML private Button btnProductos;
    @FXML private Button btnProveedores;

    @FXML
    private HBox topbar;

    @FXML
    public void initialize() {
        WindowDragUtil.enable(topbar, App.getStage());
    }

    @FXML public void minimizar() {
        Stage stage = App.getStage();
        if (stage != null) stage.setIconified(true);
    }

    @FXML public void cerrar() {
        Stage stage = App.getStage();
        if (stage != null) stage.close();
    }

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
