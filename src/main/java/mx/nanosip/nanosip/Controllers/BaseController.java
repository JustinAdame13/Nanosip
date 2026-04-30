package mx.nanosip.nanosip.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import mx.nanosip.nanosip.App;
import mx.nanosip.nanosip.WindowDragUtil;

import java.io.IOException;

public abstract class BaseController {

    // ── Dock ─────────────────────────────────────────────────
    @FXML protected StackPane dockWrapper;
    @FXML protected HBox      dockBar;
    @FXML protected HBox      topbar;

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
        Stage stage = App.getStage();
        if (stage != null) stage.setIconified(true);
    }

    @FXML public void cerrar() {
        Stage stage = App.getStage();
        if (stage != null) stage.close();
    }

    // ─────────────────────────────────────────────────────────
    //  Navegación
    // ─────────────────────────────────────────────────────────
    @FXML public void irEmpleados()   { cambiarVista("Empleados.fxml"); }
    @FXML public void irVentas()      { cambiarVista("Ventas.fxml"); }
    @FXML public void irClientes()    { cambiarVista("Clientes.fxml"); }
    @FXML public void irProductos()   { cambiarVista("Productos.fxml"); }
    @FXML public void irProveedores() { cambiarVista("Proveedores.fxml"); }
    @FXML public void cerrarSesion()  { cambiarVista("Login.fxml"); }

    protected void cambiarVista(String fxml) {
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
