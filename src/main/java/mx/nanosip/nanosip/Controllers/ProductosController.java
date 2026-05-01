package mx.nanosip.nanosip.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProductosController extends BaseController {

    // ── Top bar ──────────────────────────────────────────────
    @FXML private Button btnReporte;
    @FXML private Button btnCrear;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnMinimizar;
    @FXML private Button btnCerrar;

    // ── Contenido ────────────────────────────────────────────
    @FXML private TextField txtBusqueda;
    @FXML private TableView<?> tablaProductos;

    // ── Columnas ─────────────────────────────────────────────
    @FXML private TableColumn<?, ?> colClave;
    @FXML private TableColumn<?, ?> colNombre;
    @FXML private TableColumn<?, ?> colDescripcion;
    @FXML private TableColumn<?, ?> colExistencia;
    @FXML private TableColumn<?, ?> colMarca;
    @FXML private TableColumn<?, ?> colprecio;
    @FXML private TableColumn<?, ?> colCosto;

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
    @FXML public void crear()  { abrirModal("CrProductos.fxml"); }
    @FXML public void editar() { abrirModal("CrProductos.fxml"); }
    @FXML public void eliminar() { /* TODO */ }
}
