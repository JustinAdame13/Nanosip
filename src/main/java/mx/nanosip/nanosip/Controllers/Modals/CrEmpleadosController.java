package mx.nanosip.nanosip.Controllers.Modals;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

public class CrEmpleadosController implements ModalController {

    // ── Datos básicos ─────────────────────────────────────────
    @FXML private Label            lblTitulo;
    @FXML private TextField        txtId;
    @FXML private TextField        txtNombre;
    @FXML private TextField        txtPuesto;
    @FXML private TextField        txtRFC;
    @FXML private TextField        txtCURP;
    @FXML private Spinner<Integer> spnEdad;
    @FXML private PasswordField    txtPassword;
    @FXML private PasswordField    txtPasswordConfirm;
    @FXML private Label            lblPasswordError;

    // ── Permisos — Empleados ──────────────────────────────────
    @FXML private ToggleButton tEmpVer;
    @FXML private ToggleButton tEmpCrear;
    @FXML private ToggleButton tEmpEditar;
    @FXML private ToggleButton tEmpEliminar;

    // ── Permisos — Ventas ─────────────────────────────────────
    @FXML private ToggleButton tVenVer;
    @FXML private ToggleButton tVenCrear;
    @FXML private ToggleButton tVenEditar;
    @FXML private ToggleButton tVenEliminar;

    // ── Permisos — Clientes ───────────────────────────────────
    @FXML private ToggleButton tCliVer;
    @FXML private ToggleButton tCliCrear;
    @FXML private ToggleButton tCliEditar;
    @FXML private ToggleButton tCliEliminar;

    // ── Permisos — Productos ──────────────────────────────────
    @FXML private ToggleButton tProVer;
    @FXML private ToggleButton tProCrear;
    @FXML private ToggleButton tProEditar;
    @FXML private ToggleButton tProEliminar;

    // ── Permisos — Proveedores ────────────────────────────────
    @FXML private ToggleButton tPrvVer;
    @FXML private ToggleButton tPrvCrear;
    @FXML private ToggleButton tPrvEditar;
    @FXML private ToggleButton tPrvEliminar;

    private Stage modalStage;

    // ─────────────────────────────────────────────────────────
    //  Inicialización
    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        spnEdad.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(18, 99, 18));

        // Cascada de permisos
        registrarCascada(tEmpVer, tEmpCrear, tEmpEditar, tEmpEliminar);
        registrarCascada(tVenVer, tVenCrear, tVenEditar, tVenEliminar);
        registrarCascada(tCliVer, tCliCrear, tCliEditar, tCliEliminar);
        registrarCascada(tProVer, tProCrear, tProEditar, tProEliminar);
        registrarCascada(tPrvVer, tPrvCrear, tPrvEditar, tPrvEliminar);

        // Validación en tiempo real de contraseñas
        txtPasswordConfirm.textProperty().addListener((obs, anterior, nuevo) ->
                validarPasswords());
        txtPassword.textProperty().addListener((obs, anterior, nuevo) ->
                validarPasswords());
    }

    // ─────────────────────────────────────────────────────────
    //  Validación de contraseñas
    // ─────────────────────────────────────────────────────────
    private void validarPasswords() {
        String pass    = txtPassword.getText();
        String confirm = txtPasswordConfirm.getText();

        // Solo mostramos error si el campo de confirmación no está vacío
        boolean mismatch = !confirm.isEmpty() && !pass.equals(confirm);

        lblPasswordError.setVisible(mismatch);
        lblPasswordError.setManaged(mismatch);

        if (mismatch) {
            txtPasswordConfirm.getStyleClass().add("field-error-border");
        } else {
            txtPasswordConfirm.getStyleClass().remove("field-error-border");
        }
    }

    private boolean passwordsValidas() {
        return !txtPassword.getText().isEmpty()
                && txtPassword.getText().equals(txtPasswordConfirm.getText());
    }

    // ─────────────────────────────────────────────────────────
    //  Cascada de permisos
    // ─────────────────────────────────────────────────────────
    private void registrarCascada(ToggleButton ver, ToggleButton crear,
                                  ToggleButton editar, ToggleButton eliminar) {
        ToggleButton[] niveles = {ver, crear, editar, eliminar};
        for (int i = 0; i < niveles.length; i++) {
            final int nivel = i;
            niveles[i].selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    for (int j = 0; j < nivel; j++)
                        niveles[j].setSelected(true);
                } else {
                    for (int j = nivel + 1; j < niveles.length; j++)
                        niveles[j].setSelected(false);
                }
            });
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Cálculo del número de permisos
    // ─────────────────────────────────────────────────────────
    private int calcularNivel(ToggleButton ver, ToggleButton crear,
                              ToggleButton editar, ToggleButton eliminar) {
        if (eliminar.isSelected()) return 4;
        if (editar.isSelected())   return 3;
        if (crear.isSelected())    return 2;
        if (ver.isSelected())      return 1;
        return 0;
    }

    private String getPermisos() {
        return String.valueOf(calcularNivel(tEmpVer, tEmpCrear, tEmpEditar, tEmpEliminar))
                + calcularNivel(tVenVer, tVenCrear, tVenEditar, tVenEliminar)
                + calcularNivel(tCliVer, tCliCrear, tCliEditar, tCliEliminar)
                + calcularNivel(tProVer, tProCrear, tProEditar, tProEliminar)
                + calcularNivel(tPrvVer, tPrvCrear, tPrvEditar, tPrvEliminar);
    }

    // ─────────────────────────────────────────────────────────
    //  Acciones
    // ─────────────────────────────────────────────────────────
    @FXML
    public void guardar() {
        if (!passwordsValidas()) {
            lblPasswordError.setVisible(true);
            lblPasswordError.setManaged(true);
            txtPasswordConfirm.getStyleClass().add("field-error-border");
            return;
        }
        // TODO: validar resto de campos y llamar al servicio/API
        System.out.println("Permisos: " + getPermisos());
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
