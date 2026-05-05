package mx.nanosip.nanosip.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mx.nanosip.nanosip.App;
import mx.nanosip.nanosip.Controllers.Backend.Empleados;
import mx.nanosip.nanosip.Controllers.Backend.Sesion;

import java.io.IOException;

public class MainController {

    // Referencia al contenedor principal para poder arrastrar la ventana
    @FXML private VBox topbar;

    // Botones del menú
    @FXML private Button dockEmpleados;
    @FXML private Button dockVentas;
    @FXML private Button dockClientes;
    @FXML private Button dockProductos;
    @FXML private Button dockProveed;

    // Variables para el arrastre manual
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // 1. Verificamos permisos
        Empleados usuario = Sesion.getInstance().getUsuarioActual();
        if (usuario == null || usuario.getPermisos() == null) return;

        String p = usuario.getPermisos();

        aplicarVisibilidadMenu(dockEmpleados, p.charAt(0));
        aplicarVisibilidadMenu(dockVentas,    p.charAt(1));
        aplicarVisibilidadMenu(dockClientes,  p.charAt(2));
        aplicarVisibilidadMenu(dockProductos, p.charAt(3));
        aplicarVisibilidadMenu(dockProveed,   p.charAt(4));

        // 2. Activamos el arrastre de la ventanita manualmente (sin usar WindowDragUtil)
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

    private void aplicarVisibilidadMenu(Button boton, char nivelPermiso) {
        if (boton != null) {
            boolean tieneAcceso = nivelPermiso != '0';
            boton.setVisible(tieneAcceso);
            boton.setManaged(tieneAcceso);
        }
    }

    // ── MÉTODO PARA CARGAR MÓDULOS EN PANTALLA COMPLETA ──
    private void abrirModulo(String fxml) {
        try {
            // 1. Cargamos el nuevo diseño (Ej. Empleados.fxml)
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/mx/nanosip/nanosip/" + fxml));
            javafx.scene.Parent root = loader.load();

            // 2. Buscamos la ventana actual (tu menú flotante)
            Stage stage = (Stage) topbar.getScene().getWindow();

            // 3. Reemplazamos el menú con la pantalla del módulo directamente
            stage.getScene().setRoot(root);
            stage.sizeToScene();
            stage.centerOnScreen();

            // Registrar arrastre en el nuevo topbar
            javafx.scene.Node nuevoTopbar = root.lookup("#topbar");
            if (nuevoTopbar != null) {
                mx.nanosip.nanosip.WindowDragUtil.enable(nuevoTopbar, stage);
            }

            // 4. Maximizamos para que ocupe todo el monitor


        } catch (Exception e) {
            System.err.println("❌ Error al abrir el módulo: " + fxml);
            e.printStackTrace();
        }
    }

    // ── RUTAS A LOS MÓDULOS ──
    @FXML public void irEmpleados() { abrirModulo("Empleados.fxml"); }
    @FXML public void irVentas()    { abrirModulo("Ventas.fxml"); }
    @FXML public void irClientes()  { abrirModulo("Clientes.fxml"); }
    @FXML public void irProductos() { abrirModulo("Productos.fxml"); }
    @FXML public void irProveedores() { abrirModulo("Proveedores.fxml"); }

    // ── CONTROLES DE LA VENTANA Y SESIÓN ──
    @FXML
    public void cerrarSesion() {
        try {
            // 1. Cargamos el diseño del Login
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/mx/nanosip/nanosip/Login.fxml"));
            javafx.scene.Parent root = loader.load();

            // 2. Creamos una ventana NUEVA sin bordes
            Stage loginStage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            loginStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            loginStage.setScene(scene);
            loginStage.centerOnScreen();
            loginStage.show();

            // 3. Cerramos el menú actual
            Stage currentStage = (Stage) topbar.getScene().getWindow();
            if (currentStage != null) {
                currentStage.close();
            }

        } catch (Exception e) {
            System.err.println("❌ Error al regresar al Login:");
            e.printStackTrace();
        }
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
}