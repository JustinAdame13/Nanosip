package mx.nanosip.nanosip.Controllers;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.control.TextField;
import mx.nanosip.nanosip.App;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField IDField;
    @FXML
    private TextField PasswordField;
    @FXML
    private Button btnIngresar;

    @FXML
    public void Ingresar(){




        //cambio de ventana
        try {
            App.setRoot("Main.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }





}
