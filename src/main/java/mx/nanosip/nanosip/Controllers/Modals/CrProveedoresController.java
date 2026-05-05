package mx.nanosip.nanosip.Controllers.Modals;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import mx.nanosip.nanosip.Controllers.Backend.Proveedores;
import mx.nanosip.nanosip.Controllers.Backend.ProveedoresAPI;

public class CrProveedoresController implements ModalController {

    @FXML private Label     lblTitulo;
    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtRFC;
    @FXML private TextField txtTelefono;

    private Stage      modalStage;
    private Proveedores proveedorEditar = null;
    private final ProveedoresAPI api    = new ProveedoresAPI();

    @Override
    public void setModalStage(Stage stage) { this.modalStage = stage; }

    public void setProveedor(Proveedores p) {
        this.proveedorEditar = p;
        lblTitulo .setText("Editar Proveedor");
        txtId     .setText(String.valueOf(p.getId()));
        txtId     .setDisable(true);
        txtNombre .setText(p.getNombre());
        txtRFC    .setText(p.getRfc());
        txtTelefono.setText(p.getTelefono());
    }

    @FXML
    public void guardar() {
        if (!validar()) return;
        try {
            if (proveedorEditar == null) {
                api.guardar(new Proveedores(
                        txtNombre.getText().trim(),
                        txtRFC.getText().trim(),
                        txtTelefono.getText().trim()));
            } else {
                proveedorEditar.setNombre  (txtNombre.getText().trim());
                proveedorEditar.setRfc     (txtRFC.getText().trim());
                proveedorEditar.setTelefono(txtTelefono.getText().trim());
                api.actualizar(proveedorEditar);
            }
            cerrarModal();
        } catch (Exception e) {
            mostrarAlerta("Error al guardar: " + e.getMessage());
        }
    }

    private boolean validar() {
        if (txtNombre.getText().isBlank())   { mostrarAlerta("El nombre es obligatorio.");   return false; }
        if (txtRFC.getText().isBlank())      { mostrarAlerta("El RFC es obligatorio.");      return false; }
        if (txtTelefono.getText().isBlank()) { mostrarAlerta("El teléfono es obligatorio."); return false; }
        return true;
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

    @FXML public void cerrarModal() { if (modalStage != null) modalStage.close(); }
}