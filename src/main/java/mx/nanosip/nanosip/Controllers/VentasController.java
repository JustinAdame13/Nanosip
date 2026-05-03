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
import mx.nanosip.nanosip.Controllers.Backend.*;
import mx.nanosip.nanosip.Controllers.Modals.CrVentasController;


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

    private final ObservableList<Ventas> listaCompleta = FXCollections.observableArrayList();
    private final VentasAPI api = new VentasAPI();

    @FXML
    public void initialize() {
        initBase();
        configurarTabla();
        cargarDatos();
        configurarBuscador();
    }

    private void configurarTabla() {
        colNum     .setCellValueFactory(new PropertyValueFactory<>("numero"));
        colEmpleado.setCellValueFactory(new PropertyValueFactory<>("idEmpleado"));
        colCliente .setCellValueFactory(new PropertyValueFactory<>("idClientes"));
        colMonto   .setCellValueFactory(new PropertyValueFactory<>("monto"));

        // Estas son las que procesaremos manualmente abajo
        colProducto.setCellValueFactory(new PropertyValueFactory<>("productos"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadTotal"));
    }

    private void cargarDatos() {
        listaCompleta.clear();
        try {
            // 1. Traemos las ventas base y el catálogo de nombres de productos
            List<Ventas> ventasBase = api.obtenerTodos();
            List<Productos> catalogo = ProductosAPI.obtenerTodos();

            for (Ventas v : ventasBase) {
                // 2. Por cada venta, consultamos sus productos (ventas-productos)
                List<VentasProductos> detalles = api.obtenerDetalles(v.getNumero());

                StringBuilder nombresResumen = new StringBuilder();
                int sumaUnidades = 0;

                for (VentasProductos det : detalles) {
                    // Sumamos la cantidad de unidades
                    sumaUnidades += det.getCantidad();

                    // Buscamos el nombre del producto por su ID
                    String nombre = catalogo.stream()
                            .filter(p -> p.getClave().equals(det.getClaveProducto()))
                            .map(Productos::getNombre)
                            .findFirst()
                            .orElse("ID: " + det.getClaveProducto());

                    nombresResumen.append(nombre).append(", ");
                }

                // 3. Limpiamos la coma final y asignamos al objeto de la tabla
                if (nombresResumen.length() > 0) {
                    nombresResumen.setLength(nombresResumen.length() - 2);
                }

                v.setProductos(nombresResumen.toString());
                v.setCantidadTotal(sumaUnidades); // Aquí se guarda el total de unidades (ej: 6)
            }

            listaCompleta.addAll(ventasBase);
        } catch (Exception e) {
            System.err.println("Error al procesar la tabla de ventas: " + e.getMessage());
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


}