package mx.nanosip.nanosip.Controllers.Modals;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class CrProductosController implements ModalController {

    @FXML private Label    lblTitulo;
    @FXML private TextField txtClave;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtMarca;
    @FXML private Spinner<Integer> spnExistencia;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCosto;
    @FXML private TextField txtBuscarProveedor;
    @FXML private ListView<CheckBox> listaProveedores;

    private Stage modalStage;

    // Lista maestra de checkboxes (todos los proveedores)
    private final ObservableList<CheckBox> todosProveedores = FXCollections.observableArrayList();

    // ─────────────────────────────────────────────────────────
    //  Inicialización
    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Spinner — solo enteros no negativos
        spnExistencia.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999999, 0));

        // TODO: reemplazar con datos reales desde la API/BD
        List<String> proveedoresMock = List.of(
                "Proveedor Alpha",
                "Proveedor Beta",
                "Distribuidora Norte",
                "Comercial Sur",
                "Importaciones MX"
        );
        for (String nombre : proveedoresMock) {
            todosProveedores.add(new CheckBox(nombre));
        }
        listaProveedores.setItems(todosProveedores);

        // Filtro en tiempo real al escribir en el buscador
        txtBuscarProveedor.textProperty().addListener((obs, anterior, nuevo) -> {
            filtrarProveedores(nuevo);
        });
    }

    // ─────────────────────────────────────────────────────────
    //  Filtrado de la lista
    // ─────────────────────────────────────────────────────────
    private void filtrarProveedores(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            listaProveedores.setItems(todosProveedores);
            return;
        }
        String lower = filtro.toLowerCase();
        ObservableList<CheckBox> filtrados = FXCollections.observableArrayList(
                todosProveedores.stream()
                        .filter(cb -> cb.getText().toLowerCase().contains(lower))
                        .toList()
        );
        listaProveedores.setItems(filtrados);
    }

    // ─────────────────────────────────────────────────────────
    //  Obtener proveedores seleccionados
    // ─────────────────────────────────────────────────────────
    private List<String> getProveedoresSeleccionados() {
        List<String> seleccionados = new ArrayList<>();
        for (CheckBox cb : todosProveedores) {
            if (cb.isSelected()) seleccionados.add(cb.getText());
        }
        return seleccionados;
    }

    // ─────────────────────────────────────────────────────────
    //  Acciones
    // ─────────────────────────────────────────────────────────
    @FXML
    public void guardar() {
        // TODO: validar campos y llamar al servicio/API
        System.out.println("Proveedores seleccionados: " + getProveedoresSeleccionados());
        cerrarModal();
    }

    @FXML
    public void cerrarModal() {
        if (modalStage != null) modalStage.close();
    }

    @FXML
    public void minimizar() {
        if (modalStage != null) modalStage.setIconified(true);
    }

    @Override
    public void setModalStage(Stage stage) {
        this.modalStage = stage;
    }
}
