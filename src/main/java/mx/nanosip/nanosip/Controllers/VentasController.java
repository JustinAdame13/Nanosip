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
import mx.nanosip.nanosip.Controllers.Backend.Ventas;
import mx.nanosip.nanosip.Controllers.Backend.VentasAPI;
import mx.nanosip.nanosip.Controllers.Modals.CrVentasController;

// 💡 Importamos los modelos para poder leer la sesión global
import mx.nanosip.nanosip.Controllers.Backend.Empleados;
import mx.nanosip.nanosip.Controllers.Backend.Sesion;

import java.io.IOException;
import java.util.List;

public class VentasController extends BaseController {

    @FXML private TextField txtBusqueda;
    @FXML private TableView<Ventas> tablaVentas;
    @FXML private TableColumn<Ventas, Integer> colNum;
    @FXML private TableColumn<Ventas, Integer> colEmpleado;
    @FXML private TableColumn<Ventas, Integer> colCliente;
    @FXML private TableColumn<Ventas, Double>  colMonto;
    @FXML private TableColumn<Ventas, ?>       colProducto;
    @FXML private TableColumn<Ventas, ?>       colCantidad;

    // ── BOTONES PARA CONTROLAR PERMISOS ──
    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    @FXML private Button dockEmpleados; // Solo si este botón existe en esta pantalla
    @FXML private Button dockVentas;
    @FXML private Button dockClientes;
    @FXML private Button dockProductos;
    @FXML private Button dockProveed;
    private final ObservableList<Ventas> listaCompleta = FXCollections.observableArrayList();
    private final VentasAPI api = new VentasAPI();

    @FXML
    public void initialize() {
        initBase();
        configurarTabla();
        cargarDatos();
        configurarBuscador();

        // 💡 ¡MAGIA AQUÍ! Aplicamos los permisos al abrir la pantalla de Ventas
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
    //  Sistema de Permisos (Nivel 1: Ventas)
    // ─────────────────────────────────────────────────────────
    private void configurarPermisos() {
        Empleados usuario = Sesion.getInstance().getUsuarioActual();

        // Validamos que exista una sesión y que la cadena tenga al menos 5 dígitos
        if (usuario != null && usuario.getPermisos() != null && usuario.getPermisos().length() >= 5) {
            // Sacamos el nivel del módulo de Ventas (Posición 1 en la cadena)
            int nivel = Character.getNumericValue(usuario.getPermisos().charAt(1));

            // Nivel 1 = Solo ver (El menú se encarga de mostrar u ocultar la pantalla completa)
            // Nivel 2 = Crear (>= 2)
            // Nivel 3 = Editar (>= 3)
            // Nivel 4 = Eliminar (>= 4)

            if (btnNuevo != null) {
                btnNuevo.setVisible(nivel >= 2);
                btnNuevo.setManaged(nivel >= 2); // Oculta el espacio físico del botón si es false
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
        colNum     .setCellValueFactory(new PropertyValueFactory<>("numero"));
        colEmpleado.setCellValueFactory(new PropertyValueFactory<>("idEmpleado"));
        colCliente .setCellValueFactory(new PropertyValueFactory<>("idClientes"));
        colMonto   .setCellValueFactory(new PropertyValueFactory<>("monto"));
        // colProducto y colCantidad se mapean cuando el backend los soporte
    }

    private void cargarDatos() {
        listaCompleta.clear();
        try {
            List<Ventas> lista = api.obtenerTodos();
            listaCompleta.addAll(lista);
        } catch (Exception e) {
            System.err.println("Error cargando ventas: " + e.getMessage());
        }
    }

    private void configurarBuscador() {
        FilteredList<Ventas> listaFiltrada = new FilteredList<>(listaCompleta, v -> true);

        txtBusqueda.textProperty().addListener((obs, anterior, nuevo) -> {
            listaFiltrada.setPredicate(venta -> {
                if (nuevo == null || nuevo.isBlank()) return true;
                String f = nuevo.toLowerCase().trim();
                return String.valueOf(venta.getNumero())    .contains(f)
                        || String.valueOf(venta.getIdEmpleado()).contains(f)
                        || String.valueOf(venta.getIdClientes()).contains(f)
                        || String.valueOf(venta.getMonto())    .contains(f);
            });
        });

        SortedList<Ventas> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tablaVentas.comparatorProperty());
        tablaVentas.setItems(listaOrdenada);
    }

    @FXML public void crear() {
        abrirModal("CrVentas.fxml");
        cargarDatos();
    }

    @FXML public void generarReporte() {}

    @FXML public void editar() {
        Ventas seleccionado = tablaVentas.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona una venta primero.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/nanosip/nanosip/CrVentas.fxml"));
            Parent root = loader.load();

            CrVentasController modal=loader.getController();
            modal.setVenta(seleccionado);

            Stage stage = new Stage();
            modal.setModalStage(stage);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            cargarDatos();
        } catch (IOException e) {
            System.err.println("Error abriendo modal: " + e.getMessage());
        }
    }

    @FXML public void eliminar() {
        Ventas seleccionado = tablaVentas.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona una venta primero.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la venta #" + seleccionado.getNumero() + "?",
                ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    api.eliminar(seleccionado.getNumero());
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