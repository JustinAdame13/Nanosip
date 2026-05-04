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
            alerta("Error al guardar: " + e.getMessage());
        }
    }

    private boolean validar() {
        if (txtNombre.getText().isBlank())   { alerta("El nombre es obligatorio.");   return false; }
        if (txtRFC.getText().isBlank())      { alerta("El RFC es obligatorio.");      return false; }
        if (txtTelefono.getText().isBlank()) { alerta("El teléfono es obligatorio."); return false; }
        return true;
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    @FXML public void cerrarModal() { if (modalStage != null) modalStage.close(); }
}