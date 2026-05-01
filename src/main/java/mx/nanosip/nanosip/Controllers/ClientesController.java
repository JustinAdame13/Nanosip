package mx.nanosip.nanosip.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ClientesController extends BaseController {

    // ── Top bar ──────────────────────────────────────────────
    @FXML private Button btnReporte;
    @FXML private Button btnCrear;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnMinimizar;
    @FXML private Button btnCerrar;

    // ── Contenido ────────────────────────────────────────────
    @FXML private TextField txtBusqueda;
    @FXML private TableView<?> tablaClientes;

    // ── Columnas ─────────────────────────────────────────────
    @FXML private TableColumn<?, ?> colId;
    @FXML private TableColumn<?, ?> colNombre;
    @FXML private TableColumn<?, ?> colRFC;
    @FXML private TableColumn<?, ?> colTelefono;


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
    @FXML public void crear()  { abrirModal("CrClientes.fxml"); }
    @FXML public void editar() { abrirModal("CrClientes.fxml"); }
    @FXML public void eliminar() { /* TODO */ }
}