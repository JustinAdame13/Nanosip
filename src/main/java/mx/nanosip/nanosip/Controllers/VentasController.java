package mx.nanosip.nanosip.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class VentasController extends BaseController {

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

    // ─────────────────────────────────────────────────────────
    //  Inicialización
    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        initBase();
    }

    // ─────────────────────────────────────────────────────────
    //  Acciones propias
    // ─────────────────────────────────────────────────────────
    @FXML public void generarReporte()   { /* TODO */ }
    @FXML public void crearEmpleado()    { /* TODO */ }
    @FXML public void editarEmpleado()   { /* TODO */ }
    @FXML public void eliminarEmpleado() { /* TODO */ }
}