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
    @FXML private TableView<?> tablaVentas;

    // ── Columnas ─────────────────────────────────────────────
    @FXML private TableColumn<?, ?> colNum;
    @FXML private TableColumn<?, ?> colEmpleado;
    @FXML private TableColumn<?, ?> colCliente;
    @FXML private TableColumn<?, ?> colMonto;
    @FXML private TableColumn<?, ?> colProducto;
    @FXML private TableColumn<?, ?> colCantidad;

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
    @FXML public void crear()  { abrirModal("CrVentas.fxml"); }
    @FXML public void editar() { abrirModal("CrVentas.fxml"); }
    @FXML public void eliminar() { /* TODO */ }
}