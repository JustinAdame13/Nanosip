package mx.nanosip.nanosip.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import mx.nanosip.nanosip.Controllers.Backend.Empleados;
import mx.nanosip.nanosip.Controllers.Backend.EmpleadosAPI;
import mx.nanosip.nanosip.Controllers.Backend.Sesion;
import mx.nanosip.nanosip.WindowDragUtil;

import java.util.concurrent.CompletableFuture;

public class LoginController {

    @FXML private HBox topbar; // Referencia a la barra superior del Login
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnIngresar;
    private double xOffset = 0;
    private double yOffset = 0;
    private final EmpleadosAPI api = new EmpleadosAPI();


    @FXML
    public void initialize() {
        // Activamos el arrastre de la ventana MANUALMENTE
        if (topbar != null) {
            topbar.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            topbar.setOnMouseDragged(event -> {
                Stage stage = (Stage) topbar.getScene().getWindow();
                if (stage != null) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });
        }
    }

    @FXML
    public void Ingresar() {
        String usuario = txtUsuario.getText().trim();
        String pass = txtPassword.getText().trim();

        if (usuario.isBlank() || pass.isBlank()) {
            mostrarAlerta("Por favor ingresa tus credenciales.");
            return;
        }

        if (btnIngresar != null) btnIngresar.setDisable(true);

        CompletableFuture.runAsync(() -> {
            try {
                // Conectamos a la API
                Empleados empleadoLogueado = api.login(usuario, pass);
                Sesion.getInstance().setUsuarioActual(empleadoLogueado);

                Platform.runLater(() -> {
                    try {
                        // 1. Cargamos tu diseño del menú principal (Main.fxml)
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/nanosip/nanosip/Main.fxml"));
                        Parent root = loader.load();

                        // 2. Creamos una ventana completamente nueva para el menú
                        Stage mainStage = new Stage();
                        Scene scene = new Scene(root);

                        // 3. ¡LA MAGIA!: Quitamos el marco de Windows y hacemos el fondo transparente
                        mainStage.initStyle(StageStyle.TRANSPARENT);
                        scene.setFill(Color.TRANSPARENT);

                        mainStage.setScene(scene);
                        mainStage.centerOnScreen();
                        mainStage.show();

                        // 4. Destruimos la ventana del Login
                        Stage loginStage = (Stage) topbar.getScene().getWindow();
                        loginStage.close();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        mostrarAlerta("Error al cargar el panel principal.");
                        if (btnIngresar != null) btnIngresar.setDisable(false);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarAlerta("Usuario o contraseña incorrectos.");
                    if (btnIngresar != null) btnIngresar.setDisable(false);
                });
            }
        });
    }

    @FXML
    public void minimizar() {
        if (topbar != null && topbar.getScene() != null) {
            Stage stage = (Stage) topbar.getScene().getWindow();
            stage.setIconified(true);
        }
    }

    @FXML
    public void cerrar() {
        System.exit(0);
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        DialogPane dp = alert.getDialogPane();
        dp.getStylesheets().add(
                getClass().getResource("/mx/nanosip/nanosip/Styles.css").toExternalForm()
        );
        dp.getStyleClass().add("alert-pane");

        alert.showAndWait();
    }
}