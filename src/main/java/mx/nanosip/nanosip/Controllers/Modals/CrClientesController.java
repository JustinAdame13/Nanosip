package mx.nanosip.nanosip.Controllers.Modals;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CrClientesController implements ModalController {

    @FXML private Label     lblTitulo;
    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtRFC;
    @FXML private TextField txtTelefono;

    private Stage modalStage;

    @Override
    public void setModalStage(Stage stage) {
        this.modalStage = stage;
    }

    @FXML
    public void guardar() {
        // TODO: validar y llamar al servicio/API
        cerrarModal();
    }

    @FXML
    public void cerrarModal() {
        if (modalStage != null) modalStage.close();
    }


}
