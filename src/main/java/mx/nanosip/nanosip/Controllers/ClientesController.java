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
import javafx.stage.StageStyle;
import mx.nanosip.nanosip.App;
import mx.nanosip.nanosip.Controllers.Backend.Clientes;
import mx.nanosip.nanosip.Controllers.Backend.ClientesAPI;
import mx.nanosip.nanosip.Controllers.Modals.CrClientesController;

// 💡 Importamos los modelos para la sesión global
import mx.nanosip.nanosip.Controllers.Backend.Empleados;
import mx.nanosip.nanosip.Controllers.Backend.Sesion;
import mx.nanosip.nanosip.WindowDragUtil;

import java.io.IOException;
import java.util.List;

public class ClientesController extends BaseController {

    @FXML private TextField txtBusqueda;
    @FXML private TableView<Clientes> tablaClientes;
    @FXML private TableColumn<Clientes, Integer> colId;
    @FXML private TableColumn<Clientes, String>  colNombre;
    @FXML private TableColumn<Clientes, String>  colRFC;
    @FXML private TableColumn<Clientes, String>  colTelefono;

    @FXML private Button dockEmpleados; // Solo si este botón existe en esta pantalla
    @FXML private Button dockVentas;
    @FXML private Button dockClientes;
    @FXML private Button dockProductos;
    @FXML private Button dockProveed;
    // ── BOTONES PARA CONTROLAR PERMISOS ──
    @FXML private Button btnReporte;
    @FXML private Button btnCrear;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    private final ObservableList<Clientes> listaCompleta = FXCollections.observableArrayList();
    private final ClientesAPI api = new ClientesAPI();

    @FXML
    public void initialize() {
        initBase();
        configurarTabla();
        cargarDatos();
        configurarBuscador();

        // 💡 ¡MAGIA AQUÍ! Aplicamos los permisos al abrir la pantalla de Clientes
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
    //  Sistema de Permisos (Nivel 2: Clientes)
    // ─────────────────────────────────────────────────────────
    private void configurarPermisos() {
        Empleados usuario = Sesion.getInstance().getUsuarioActual();

        if (usuario != null && usuario.getPermisos() != null && usuario.getPermisos().length() >= 5) {
            int nivel = Character.getNumericValue(usuario.getPermisos().charAt(2));

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
        colId      .setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre  .setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRFC     .setCellValueFactory(new PropertyValueFactory<>("rfc"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
    }

    private void cargarDatos() {
        listaCompleta.clear();
        try {
            List<Clientes> lista = api.obtenerTodos();
            listaCompleta.addAll(lista);
        } catch (Exception e) {
            System.err.println("Error cargando clientes: " + e.getMessage());
        }
    }

    private void configurarBuscador() {
        FilteredList<Clientes> listaFiltrada = new FilteredList<>(listaCompleta, c -> true);
        txtBusqueda.textProperty().addListener((obs, anterior, nuevo) -> {
            listaFiltrada.setPredicate(cli -> {
                if (nuevo == null || nuevo.isBlank()) return true;
                String f = nuevo.toLowerCase().trim();
                return cli.getNombre()    .toLowerCase().contains(f)
                        || cli.getRfc()       .toLowerCase().contains(f)
                        || cli.getTelefono()  .toLowerCase().contains(f)
                        || String.valueOf(cli.getId()).contains(f);
            });
        });
        SortedList<Clientes> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tablaClientes.comparatorProperty());
        tablaClientes.setItems(listaOrdenada);
    }

    @FXML public void crear() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/mx/nanosip/nanosip/CrClientes.fxml"));
            Parent root = loader.load();
            CrClientesController modal = loader.getController();

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initOwner(App.getStage());
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            modal.setModalStage(stage);

            javafx.scene.Node topbarNode = root.lookup("#topbar");
            if (topbarNode != null) WindowDragUtil.enable(topbarNode, stage);

            stage.showAndWait();
            cargarDatos();
        } catch (IOException e) {
            System.err.println("Error abriendo CrClientes: " + e.getMessage());
        }
    }

    @FXML public void generarReporte() {}

    @FXML public void editar() {
        Clientes seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrarAlerta("Selecciona un cliente primero."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/mx/nanosip/nanosip/CrClientes.fxml"));
            Parent root = loader.load();
            CrClientesController modal = loader.getController();

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initOwner(App.getStage());
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            modal.setModalStage(stage);
            modal.setCliente(seleccionado);

            javafx.scene.Node topbarNode = root.lookup("#topbar");
            if (topbarNode != null) WindowDragUtil.enable(topbarNode, stage);

            stage.showAndWait();
            cargarDatos();
        } catch (IOException e) {
            System.err.println("Error abriendo CrClientes: " + e.getMessage());
        }
    }

    @FXML public void eliminar() {
        Clientes seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrarAlerta("Selecciona un cliente primero."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar a " + seleccionado.getNombre() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText(null);

        DialogPane dp = confirm.getDialogPane();
        dp.getStylesheets().add(
                getClass().getResource("/mx/nanosip/nanosip/Styles.css").toExternalForm()
        );
        dp.getStyleClass().add("alert-pane");

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

    private void aplicarVisibilidadMenu(Button boton, char nivelPermiso) {
        if (boton != null) {
            boolean tieneAcceso = nivelPermiso != '0';
            boton.setVisible(tieneAcceso);
            boton.setManaged(tieneAcceso); // Esto hace que el espacio del botón desaparezca y los demás se recorran
        }
    }
}