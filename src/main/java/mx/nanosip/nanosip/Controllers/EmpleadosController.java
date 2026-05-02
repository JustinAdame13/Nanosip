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
import mx.nanosip.nanosip.Controllers.Backend.Empleados;
import mx.nanosip.nanosip.Controllers.Backend.EmpleadosAPI;
import mx.nanosip.nanosip.Controllers.Modals.CrEmpleadosController;

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

    private final ObservableList<Empleados> listaCompleta = FXCollections.observableArrayList();
    private final EmpleadosAPI api = new EmpleadosAPI();

    @FXML
    public void initialize() {
        initBase();
        configurarTabla();
        cargarDatos();
        configurarBuscador();
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
            stage.showAndWait();

            cargarDatos(); // refresca después de cerrar

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
}
