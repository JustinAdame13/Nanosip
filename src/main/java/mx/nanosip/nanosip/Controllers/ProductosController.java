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
import mx.nanosip.nanosip.Controllers.Backend.Productos;
import mx.nanosip.nanosip.Controllers.Backend.ProductosAPI;
import mx.nanosip.nanosip.Controllers.Modals.CrProductosController;

import java.io.IOException;
import java.util.List;

public class ProductosController extends BaseController {

    @FXML private TextField txtBusqueda;
    @FXML private TableView<Productos> tablaProductos;
    @FXML private TableColumn<Productos, Integer> colClave;
    @FXML private TableColumn<Productos, String>  colNombre;
    @FXML private TableColumn<Productos, String>  colDescripcion;
    @FXML private TableColumn<Productos, Integer> colExistencia;
    @FXML private TableColumn<Productos, String>  colMarca;
    @FXML private TableColumn<Productos, Double>  colprecio;
    @FXML private TableColumn<Productos, Double>  colCosto;

    private final ObservableList<Productos> listaCompleta = FXCollections.observableArrayList();
    private final ProductosAPI api = new ProductosAPI();

    @FXML
    public void initialize() {
        initBase();
        configurarTabla();
        cargarDatos();
        configurarBuscador();
    }

    private void configurarTabla() {
        colClave      .setCellValueFactory(new PropertyValueFactory<>("clave"));
        colNombre     .setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colExistencia .setCellValueFactory(new PropertyValueFactory<>("inventario"));
        colMarca      .setCellValueFactory(new PropertyValueFactory<>("marca"));
        colprecio     .setCellValueFactory(new PropertyValueFactory<>("precio"));
        colCosto      .setCellValueFactory(new PropertyValueFactory<>("costo"));
    }

    private void cargarDatos() {
        listaCompleta.clear();
        try {
            List<Productos> lista = api.obtenerTodos();
            listaCompleta.addAll(lista);
        } catch (Exception e) {
            System.err.println("Error cargando productos: " + e.getMessage());
        }
    }

    private void configurarBuscador() {
        FilteredList<Productos> listaFiltrada = new FilteredList<>(listaCompleta, p -> true);

        txtBusqueda.textProperty().addListener((obs, anterior, nuevo) -> {
            listaFiltrada.setPredicate(prod -> {
                if (nuevo == null || nuevo.isBlank()) return true;
                String f = nuevo.toLowerCase().trim();
                return prod.getNombre()     .toLowerCase().contains(f)
                        || prod.getMarca()     .toLowerCase().contains(f)
                        || prod.getDescripcion().toLowerCase().contains(f)
                        || String.valueOf(prod.getClave()).contains(f);
            });
        });

        SortedList<Productos> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(listaOrdenada);
    }

    @FXML public void crear() {
        abrirModal("CrProductos.fxml");
        cargarDatos();
    }

    @FXML public void generarReporte() {}

    @FXML public void editar() {
        Productos seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona un producto primero.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/nanosip/nanosip/CrProductos.fxml"));
            Parent root = loader.load();

            CrProductosController modal=loader.getController();
            modal.setProducto(seleccionado);

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
        Productos seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona un producto primero.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el producto " + seleccionado.getNombre() + "?",
                ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    api.eliminar(seleccionado.getClave());
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
}