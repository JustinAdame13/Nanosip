package mx.nanosip.nanosip.Controllers.Modals;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import mx.nanosip.nanosip.Controllers.Backend.Clientes;
import mx.nanosip.nanosip.Controllers.Backend.ClientesAPI;

public class CrClientesController implements ModalController {

    @FXML private Label     lblTitulo;
    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtRFC;
    @FXML private TextField txtTelefono;

    private Stage    modalStage;
    private Clientes clienteEditar = null;
    private final ClientesAPI api  = new ClientesAPI();

    @Override
    public void setModalStage(Stage stage) { this.modalStage = stage; }

    public void setCliente(Clientes c) {
        this.clienteEditar = c;
        lblTitulo .setText("Editar Cliente");
        txtId     .setText(String.valueOf(c.getId()));
        txtId     .setDisable(true);
        txtNombre .setText(c.getNombre());
        txtRFC    .setText(c.getRfc());
        txtTelefono.setText(c.getTelefono());
    }

    @FXML
    public void guardar() {
        if (!validar()) return;
        try {
            if (clienteEditar == null) {
                api.guardar(new Clientes(
                        txtNombre.getText().trim(),
                        txtRFC.getText().trim(),
                        txtTelefono.getText().trim()));
            } else {
                clienteEditar.setNombre  (txtNombre.getText().trim());
                clienteEditar.setRfc     (txtRFC.getText().trim());
                clienteEditar.setTelefono(txtTelefono.getText().trim());
                api.actualizar(clienteEditar);
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