package mx.nanosip.nanosip.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import mx.nanosip.nanosip.App;
import mx.nanosip.nanosip.WindowDragUtil;

import java.io.IOException;

public class EmpleadosController {

    // ── Top bar ──────────────────────────────────────────────
    @FXML private Button btnReporte;
    @FXML private Button btnCrear;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnMinimizar;
    @FXML private Button btnCerrar;

    // ── Contenido ────────────────────────────────────────────
    @FXML private TextField txtBusqueda;
    @FXML private TableView<?> tablaEmpleados;

    // ── Columnas ─────────────────────────────────────────────
    @FXML private TableColumn<?, ?> colId;
    @FXML private TableColumn<?, ?> colNombre;
    @FXML private TableColumn<?, ?> colApellido;
    @FXML private TableColumn<?, ?> colPuesto;
    @FXML private TableColumn<?, ?> colDept;
    @FXML private TableColumn<?, ?> colEstatus;

    // ── Dock ─────────────────────────────────────────────────
    @FXML private StackPane dockWrapper;
    @FXML private HBox      dockBar;
    @FXML private HBox topbar;

    /** Altura total del dock cuando está completamente visible (px) */
    private static final double DOCK_FULL   = 56.0;
    /** Cuántos px quedan visibles cuando el dock está "oculto" */
    private static final double DOCK_HIDDEN = 8.0;

    /** translateY cuando el dock está oculto: casi todo el panel baja */
    private static final double TRANSLATE_HIDDEN = DOCK_FULL - DOCK_HIDDEN;  // 48
    /** translateY cuando el dock está visible: panel en posición normal */
    private static final double TRANSLATE_SHOWN  = 0.0;

    private Timeline showTimeline;
    private Timeline hideTimeline;

    // ─────────────────────────────────────────────────────────
    //  Inicialización
    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // El dock arranca casi oculto (translateY = 48, solo 8px visibles)
        dockBar.setTranslateY(TRANSLATE_HIDDEN);

        // Fijar altura del wrapper al valor "lengüeta"
        dockWrapper.setPrefHeight(DOCK_HIDDEN);
        dockWrapper.setMaxHeight(DOCK_FULL);   // permite crecer al hacer hover

        buildAnimations();
        WindowDragUtil.enable(topbar, App.getStage());
    }

    // ─────────────────────────────────────────────────────────
    //  Animaciones del dock
    // ─────────────────────────────────────────────────────────
    private void buildAnimations() {
        // Mostrar: translateY 48 → 0, wrapper crece a 56px
        showTimeline = new Timeline(
            new KeyFrame(Duration.millis(220),
                new KeyValue(dockBar.translateYProperty(),    TRANSLATE_SHOWN,  javafx.animation.Interpolator.EASE_OUT),
                new KeyValue(dockWrapper.prefHeightProperty(), DOCK_FULL,        javafx.animation.Interpolator.EASE_OUT)
            )
        );

        // Ocultar: translateY 0 → 48, wrapper encoge a 8px
        hideTimeline = new Timeline(
            new KeyFrame(Duration.millis(200),
                new KeyValue(dockBar.translateYProperty(),    TRANSLATE_HIDDEN, javafx.animation.Interpolator.EASE_IN),
                new KeyValue(dockWrapper.prefHeightProperty(), DOCK_HIDDEN,      javafx.animation.Interpolator.EASE_IN)
            )
        );
    }

    @FXML
    public void mostrarDock() {
        if (hideTimeline != null) hideTimeline.stop();
        showTimeline.playFromStart();
    }

    @FXML
    public void ocultarDock() {
        if (showTimeline != null) showTimeline.stop();
        hideTimeline.playFromStart();
    }

    // ─────────────────────────────────────────────────────────
    //  Acciones top bar
    // ─────────────────────────────────────────────────────────
    @FXML public void generarReporte()  { /* TODO */ }
    @FXML public void crearEmpleado()   { /* TODO */ }
    @FXML public void editarEmpleado()  { /* TODO */ }
    @FXML public void eliminarEmpleado(){ /* TODO */ }

    @FXML public void minimizar() {
        Stage stage = App.getStage();
        if (stage != null) stage.setIconified(true);
    }

    @FXML public void cerrar() {
        Stage stage = App.getStage();
        if (stage != null) stage.close();
    }

    // ─────────────────────────────────────────────────────────
    //  Navegación desde el dock
    // ─────────────────────────────────────────────────────────
    @FXML public void irEmpleados()   { cambiarVista("Empleados.fxml"); }
    @FXML public void irVentas()      { cambiarVista("Ventas.fxml"); }
    @FXML public void irClientes()    { cambiarVista("Clientes.fxml"); }
    @FXML public void irProductos()   { cambiarVista("Productos.fxml"); }
    @FXML public void irProveedores() { cambiarVista("Proveedores.fxml"); }

    @FXML public void cerrarSesion()  { cambiarVista("Login.fxml"); }

    private void cambiarVista(String fxml) {
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
