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
import mx.nanosip.nanosip.Controllers.Backend.Proveedores;
import mx.nanosip.nanosip.Controllers.Backend.ProveedoresAPI;
import mx.nanosip.nanosip.Controllers.Modals.CrProveedoresController;

import java.io.IOException;
import java.util.List;

public class ProveedoresController extends BaseController {

    @FXML private TextField txtBusqueda;
    @FXML private TableView<Proveedores> tablaProveedores;
    @FXML private TableColumn<Proveedores, Integer> colId;
    @FXML private TableColumn<Proveedores, String>  colNombre;
    @FXML private TableColumn<Proveedores, String>  colRFC;
    @FXML private TableColumn<Proveedores, String>  colTelefono;

    private final ObservableList<Proveedores> listaCompleta = FXCollections.observableArrayList();
    private final ProveedoresAPI api = new ProveedoresAPI();

    @FXML
    public void initialize() {
        initBase();
        configurarTabla();
        cargarDatos();
        configurarBuscador();
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
            List<Proveedores> lista = api.obtenerTodos();
            listaCompleta.addAll(lista);
        } catch (Exception e) {
            System.err.println("Error cargando proveedores: " + e.getMessage());
        }
    }

    private void configurarBuscador() {
        FilteredList<Proveedores> listaFiltrada = new FilteredList<>(listaCompleta, p -> true);

        txtBusqueda.textProperty().addListener((obs, anterior, nuevo) -> {
            listaFiltrada.setPredicate(prov -> {
                if (nuevo == null || nuevo.isBlank()) return true;
                String f = nuevo.toLowerCase().trim();
                return prov.getNombre()  .toLowerCase().contains(f)
                        || prov.getRfc()   .toLowerCase().contains(f)
                        || prov.getTelefono().toLowerCase().contains(f)
                        || String.valueOf(prov.getId()).contains(f);
            });
        });

        SortedList<Proveedores> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tablaProveedores.comparatorProperty());
        tablaProveedores.setItems(listaOrdenada);
    }

    @FXML public void crear() {
        abrirModal("CrProveedores.fxml");
        cargarDatos();
    }

    @FXML public void generarReporte() {}

    @FXML public void editar() {
        Proveedores seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona un proveedor primero.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/nanosip/nanosip/CrProveedores.fxml"));
            Parent root = loader.load();

            CrProveedoresController modal=loader.getController();
            modal.setProveedor(seleccionado);


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
        Proveedores seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona un proveedor primero.");
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
}