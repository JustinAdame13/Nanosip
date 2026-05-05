package mx.nanosip.nanosip.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import mx.nanosip.nanosip.App;
import mx.nanosip.nanosip.Controllers.Modals.ModalController;
import mx.nanosip.nanosip.WindowDragUtil;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.io.IOException;

public abstract class BaseController {

    // ── Dock ─────────────────────────────────────────────────
    @FXML protected StackPane dockWrapper;
    @FXML protected HBox      dockBar;
    @FXML protected HBox      topbar;

    @FXML protected Button btnMaximizar;

    private static final double DOCK_FULL        = 56.0;
    private static final double DOCK_HIDDEN      = 8.0;
    private static final double TRANSLATE_HIDDEN = DOCK_FULL - DOCK_HIDDEN;
    private static final double TRANSLATE_SHOWN  = 0.0;

    private Timeline showTimeline;
    private Timeline hideTimeline;

    // ─────────────────────────────────────────────────────────
    //  Inicialización base — llama esto desde cada hijo
    // ─────────────────────────────────────────────────────────
    protected void initBase() {
        dockBar.setTranslateY(TRANSLATE_HIDDEN);
        dockWrapper.setPrefHeight(DOCK_HIDDEN);
        dockWrapper.setMaxHeight(DOCK_FULL);
        buildAnimations();
        WindowDragUtil.enable(topbar, App.getStage());
    }

    // ─────────────────────────────────────────────────────────
    //  Animaciones del dock
    // ─────────────────────────────────────────────────────────
    private void buildAnimations() {
        showTimeline = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(dockBar.translateYProperty(),     TRANSLATE_SHOWN, javafx.animation.Interpolator.EASE_OUT),
                        new KeyValue(dockWrapper.prefHeightProperty(), DOCK_FULL,       javafx.animation.Interpolator.EASE_OUT)
                )
        );
        hideTimeline = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(dockBar.translateYProperty(),     TRANSLATE_HIDDEN, javafx.animation.Interpolator.EASE_IN),
                        new KeyValue(dockWrapper.prefHeightProperty(), DOCK_HIDDEN,      javafx.animation.Interpolator.EASE_IN)
                )
        );
    }

    @FXML public void mostrarDock() {
        if (hideTimeline != null) hideTimeline.stop();
        showTimeline.playFromStart();
    }

    @FXML public void ocultarDock() {
        if (showTimeline != null) showTimeline.stop();
        hideTimeline.playFromStart();
    }

    // ─────────────────────────────────────────────────────────
    //  Ventana
    // ─────────────────────────────────────────────────────────
    @FXML public void minimizar() {
        Stage stage = (Stage) topbar.getScene().getWindow();
        if (stage != null) stage.setIconified(true);
    }
    @FXML public void maximizar() {
        Stage stage = (Stage) topbar.getScene().getWindow();
        if (stage != null) {
            boolean maximizado = stage.isMaximized();
            stage.setMaximized(!maximizado);
            btnMaximizar.setText(maximizado ? "▢" : "❐");
        }
    }

    @FXML public void cerrar() {
        Stage stage = (Stage) topbar.getScene().getWindow();
        if (stage != null) stage.close();
    }

    // ─────────────────────────────────────────────────────────
    //  Navegación
    // ─────────────────────────────────────────────────────────
    @FXML
    public void irEmpleados() { cambiarPantalla("Empleados.fxml"); }
    @FXML
    public void irVentas()    { cambiarPantalla("Ventas.fxml"); }
    @FXML
    public void irClientes()  { cambiarPantalla("Clientes.fxml"); }
    @FXML
    public void irProductos() { cambiarPantalla("Productos.fxml"); }
    @FXML
    public void irProveedores() { cambiarPantalla("Proveedores.fxml"); }

    protected void cambiarVista(String fxml) {
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void abrirModal(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
            Parent root = loader.load();

            Stage modal = new Stage();
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(App.getStage());
            modal.initStyle(StageStyle.TRANSPARENT);
            modal.setResizable(false);

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modal.setScene(scene);

            // Inyectar Stage al controller del modal
            Object ctrl = loader.getController();
            if (ctrl instanceof ModalController mc) {
                mc.setModalStage(modal);
            }

            // Habilitar arrastre usando el topbar del modal
            javafx.scene.Node topbarNode = root.lookup("#topbar");
            if (topbarNode != null) {
                WindowDragUtil.enable(topbarNode, modal);
            }

            modal.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cambiarPantalla(String fxml) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/mx/nanosip/nanosip/" + fxml));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) topbar.getScene().getWindow();
            topbar.getScene().setRoot(root);

            javafx.scene.Node nuevoTopbar = root.lookup("#topbar");
            if (nuevoTopbar != null) {
                mx.nanosip.nanosip.WindowDragUtil.enable(nuevoTopbar, stage);
            }
        } catch (Exception e) {
            System.err.println("❌ Error al cambiar a: " + fxml);
            e.printStackTrace();
        }
    }


}
