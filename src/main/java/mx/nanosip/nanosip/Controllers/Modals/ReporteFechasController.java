package mx.nanosip.nanosip.Controllers.Modals;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;

public class ReporteFechasController implements ModalController {

    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private Label      lblError;

    private Stage modalStage;
    private BiConsumer<LocalDateTime, LocalDateTime> onConfirmar;

    @Override
    public void setModalStage(Stage stage) { this.modalStage = stage; }

    public void setOnConfirmar(BiConsumer<LocalDateTime, LocalDateTime> callback) {
        this.onConfirmar = callback;
    }

    @FXML
    public void initialize() {
        dpInicio.setValue(LocalDate.now().withDayOfMonth(1));
        dpFin.setValue(LocalDate.now());
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    @FXML
    public void confirmar() {
        LocalDate inicio = dpInicio.getValue();
        LocalDate fin    = dpFin.getValue();

        if (inicio == null || fin == null) {
            mostrarError("Selecciona ambas fechas.");
            return;
        }
        if (inicio.isAfter(fin)) {
            mostrarError("La fecha de inicio no puede ser mayor a la fecha fin.");
            return;
        }

        if (onConfirmar != null) {
            onConfirmar.accept(
                    inicio.atStartOfDay(),
                    fin.atTime(23, 59, 59));
        }
        if (modalStage != null) modalStage.close();
    }

    @FXML
    public void cancelar() {
        if (modalStage != null) modalStage.close();
    }

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }
}