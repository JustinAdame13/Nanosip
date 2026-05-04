package mx.nanosip.nanosip.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
// 💡 Asegúrate de que las rutas a tu modelo Empleado y Sesion sean las correctas
import mx.nanosip.nanosip.Controllers.Backend.Empleados;
import mx.nanosip.nanosip.Controllers.Backend.EmpleadosAPI;
import mx.nanosip.nanosip.Controllers.Modals.CrEmpleadosController;
import mx.nanosip.nanosip.Controllers.Backend.Sesion;

import java.io.IOException;
import java.util.List;

public class EmpleadosController extends BaseController {

    @FXML private TextField            txtBusqueda;
    @FXML private TableView<Empleados> tablaEmpleados;
    @FXML private TableColumn<Empleados, Integer> colId;
    @FXML private TableColumn<Empleados, String>  colNombre;
    @FXML private TableColumn<Empleados, String>  colPuesto;
    @FXML private TableColumn<Empleados, Integer> colEdad;
    @FXML private TableColumn<Empleados, String>  colRFC;
    @FXML private TableColumn<Empleados, String>  curp;

    // ── BOTONES PARA CONTROLAR PERMISOS ──
    @FXML private Button btnReporte;
    @FXML private Button btnCrear;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    @FXML private Button dockEmpleados; // Solo si este botón existe en esta pantalla
    @FXML private Button dockVentas;
    @FXML private Button dockClientes;
    @FXML private Button dockProductos;
    @FXML private Button dockProveed;

    private final ObservableList<Empleados> listaCompleta = FXCollections.observableArrayList();
    private final EmpleadosAPI api = new EmpleadosAPI();

    @FXML
    public void initialize() {
        initBase();
        configurarTabla();
        cargarDatos();
        configurarBuscador();

        // 💡 ¡MAGIA AQUÍ! Aplicamos los permisos al abrir la pantalla
        configurarPermisos();
        // ── LÓGICA DE PERMISOS PARA EL DOCK INFERIOR ──
        mx.nanosip.nanosip.Controllers.Backend.Empleados usuario = mx.nanosip.nanosip.Controllers.Backend.Sesion.getInstance().getUsuarioActual();

        if (usuario != null && usuario.getPermisos() != null) {
            String p = usuario.getPermisos();

            // Ocultamos/Mostramos los botones del dock inferior
            aplicarVisibilidadMenu(dockEmpleados, p.charAt(0));
            aplicarVisibilidadMenu(dockVentas,    p.charAt(1));
            aplicarVisibilidadMenu(dockClientes,  p.charAt(2));
            aplicarVisibilidadMenu(dockProductos, p.charAt(3));
            aplicarVisibilidadMenu(dockProveed,   p.charAt(4));
        }

    }

    // ─────────────────────────────────────────────────────────
    //  Sistema de Permisos (Nivel 0: Empleados)
    // ─────────────────────────────────────────────────────────
    private void configurarPermisos() {
        Empleados usuario = Sesion.getInstance().getUsuarioActual();

        if (usuario != null && usuario.getPermisos() != null && usuario.getPermisos().length() >= 5) {
            int nivel = Character.getNumericValue(usuario.getPermisos().charAt(0));

            if (btnReporte != null) {
                btnReporte.setVisible(nivel >= 1);
                btnReporte.setManaged(nivel >= 1);
            }
            if (btnCrear != null) {
                btnCrear.setVisible(nivel >= 2);
                btnCrear.setManaged(nivel >= 2);
            }
            if (btnEditar != null) {
                btnEditar.setVisible(nivel >= 3);
                btnEditar.setManaged(nivel >= 3);
            }
            if (btnEliminar != null) {
                btnEliminar.setVisible(nivel >= 4);
                btnEliminar.setManaged(nivel >= 4);
            }
        }
    }

    private void configurarTabla() {
        colId    .setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPuesto.setCellValueFactory(new PropertyValueFactory<>("puesto"));
        colEdad  .setCellValueFactory(new PropertyValueFactory<>("edad"));
        colRFC   .setCellValueFactory(new PropertyValueFactory<>("rfc"));
        curp     .setCellValueFactory(new PropertyValueFactory<>("curp"));
    }

    private void cargarDatos() {
        listaCompleta.clear();
        try {
            List<Empleados> lista = api.obtenerTodos();
            listaCompleta.addAll(lista);
        } catch (Exception e) {
            System.err.println("Error cargando empleados: " + e.getMessage());
        }
    }

    private void configurarBuscador() {
        FilteredList<Empleados> listaFiltrada = new FilteredList<>(listaCompleta, e -> true);

        txtBusqueda.textProperty().addListener((obs, anterior, nuevo) -> {
            listaFiltrada.setPredicate(emp -> {
                if (nuevo == null || nuevo.isBlank()) return true;
                String f = nuevo.toLowerCase().trim();
                return emp.getNombre().toLowerCase().contains(f)
                        || emp.getPuesto().toLowerCase().contains(f)
                        || emp.getRfc()   .toLowerCase().contains(f)
                        || emp.getCurp()  .toLowerCase().contains(f)
                        || String.valueOf(emp.getId()).contains(f);
            });
        });

        SortedList<Empleados> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tablaEmpleados.comparatorProperty());
        tablaEmpleados.setItems(listaOrdenada);
    }

    @FXML public void crear() {
        abrirModal("CrEmpleados.fxml");
        cargarDatos();
    }

    @FXML public void generarReporte() {
    }

    @FXML public void editar() {
        Empleados seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona un empleado primero.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/nanosip/nanosip/CrEmpleados.fxml"));
            Parent root = loader.load();

            CrEmpleadosController modal = loader.getController();
            modal.setEmpleado(seleccionado);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));

            // 💡 CRÍTICO: Le decimos al modal en qué ventana está para que pueda cerrarse
            modal.setModalStage(stage);

            stage.showAndWait();

            cargarDatos(); // refresca la tabla después de cerrar el modal
        } catch (IOException e) {
            System.err.println("Error abriendo modal: " + e.getMessage());
        }
    }

    @FXML public void eliminar() {
        Empleados seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona un empleado primero.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar a " + seleccionado.getNombre() + "?",
                ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    api.eliminar(seleccionado.getId());
                    cargarDatos();
                } catch (Exception e) {
                    mostrarAlerta("Error al eliminar: " + e.getMessage());
                }
            }
        });
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void cerrarSesion() {
        try {
            // 1. Cargamos el diseño del Login
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/mx/nanosip/nanosip/Login.fxml"));
            javafx.scene.Parent root = loader.load();

            // 2. Creamos la ventanita nueva y sin bordes
            javafx.stage.Stage loginStage = new javafx.stage.Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            loginStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            loginStage.setScene(scene);
            loginStage.centerOnScreen();
            loginStage.show();

            // 3. Cerramos la ventana actual gigante
            // (Asegúrate de tener declarada la variable @FXML private HBox topbar; o usa cualquier otro ID que tengas en la pantalla)
            javafx.stage.Stage currentStage = (javafx.stage.Stage) topbar.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Método universal para cambiar pantallas desde el dock inferior
    private void cambiarPantalla(String fxml) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/mx/nanosip/nanosip/" + fxml));
            javafx.scene.Parent root = loader.load();

            // Usamos la variable topbar (que ya tienes en tus FXML) para cambiar la escena
            topbar.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println("❌ Error al cambiar a: " + fxml);
            e.printStackTrace();
        }
    }
    private void aplicarVisibilidadMenu(Button boton, char nivelPermiso) {
        if (boton != null) {
            boolean tieneAcceso = nivelPermiso != '0';
            boton.setVisible(tieneAcceso);
            boton.setManaged(tieneAcceso); // Esto hace que el espacio del botón desaparezca y los demás se recorran
        }
    }
}