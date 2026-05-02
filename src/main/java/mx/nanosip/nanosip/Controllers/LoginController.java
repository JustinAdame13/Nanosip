package mx.nanosip.nanosip.Controllers;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import mx.nanosip.nanosip.App;
import mx.nanosip.nanosip.WindowDragUtil;
import java.sql.Connection;
import java.io.IOException;


public class LoginController {
    @FXML
    private TextField IDField;
    @FXML
    private PasswordField PasswordField;
    @FXML
    private Button btnIngresar;
    @FXML
    private HBox topbar;


    @FXML
    public void initialize() {
        WindowDragUtil.enable(topbar, App.getStage());
    }

    @FXML
    public void Ingresar(){




        //cambio de ventana
        try {
            App.setRoot("Main.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @FXML public void minimizar() {
        Stage stage = App.getStage();
        if (stage != null) stage.setIconified(true);
    }

    @FXML public void cerrar() {
        Stage stage = App.getStage();
        if (stage != null) stage.close();
    }





}
