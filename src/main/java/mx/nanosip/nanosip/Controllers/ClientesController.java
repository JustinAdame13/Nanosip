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
import mx.nanosip.nanosip.Controllers.Backend.Clientes;
import mx.nanosip.nanosip.Controllers.Backend.ClientesAPI;
import mx.nanosip.nanosip.Controllers.Modals.CrClientesController;

import java.io.IOException;
import java.util.List;

public class ClientesController extends BaseController {

    @FXML private TextField txtBusqueda;
    @FXML private TableView<Clientes> tablaClientes;
    @FXML private TableColumn<Clientes, Integer> colId;
    @FXML private TableColumn<Clientes, String>  colNombre;
    @FXML private TableColumn<Clientes, String>  colRFC;
    @FXML private TableColumn<Clientes, String>  colTelefono;

    private final ObservableList<Clientes> listaCompleta = FXCollections.observableArrayList();
    private final ClientesAPI api = new ClientesAPI();

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
            Parent root = loader.load();                          // ← primero load()
            CrClientesController modal = loader.getController(); // ← luego getController()
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            modal.setModalStage(stage);                          // ← luego setModalStage
            stage.showAndWait();
            cargarDatos();
        } catch (IOException e) {
            System.err.println("Error abriendo CrClientes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML public void generarReporte() {}

    @FXML public void editar() {
        Clientes seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrarAlerta("Selecciona un cliente primero."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/mx/nanosip/nanosip/CrClientes.fxml"));
            Parent root = loader.load();                          // ← primero load()
            CrClientesController modal = loader.getController(); // ← luego getController()
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            modal.setModalStage(stage);                          // ← luego setModalStage
            modal.setCliente(seleccionado);                      // ← al final setCliente
            stage.showAndWait();
            cargarDatos();
        } catch (IOException e) {
            System.err.println("Error abriendo CrClientes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML public void eliminar() {
        Clientes seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrarAlerta("Selecciona un cliente primero."); return; }
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