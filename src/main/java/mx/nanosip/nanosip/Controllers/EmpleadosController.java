package mx.nanosip.nanosip.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import mx.nanosip.nanosip.Controllers.Backend.Empleados;
import mx.nanosip.nanosip.Controllers.Backend.EmpleadosDAO;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EmpleadosController extends BaseController {

    // ── Top bar ──────────────────────────────────────────────
    @FXML private Button btnReporte;
    @FXML private Button btnCrear;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    // ── Contenido ────────────────────────────────────────────
    @FXML private TextField           txtBusqueda;
    @FXML private TableView<Empleados> tablaEmpleados;

    // ── Columnas ─────────────────────────────────────────────
    @FXML private TableColumn<Empleados, Integer> colId;
    @FXML private TableColumn<Empleados, String>  colNombre;
    @FXML private TableColumn<Empleados, String>  colPuesto;
    @FXML private TableColumn<Empleados, Integer> colEdad;
    @FXML private TableColumn<Empleados, String>  colRFC;
    @FXML private TableColumn<Empleados, String>  curp;

    // ── Lista maestra ─────────────────────────────────────────
    private final ObservableList<Empleados> listaCompleta = FXCollections.observableArrayList();

    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        initBase();
        configurarTabla();
        cargarDatos();
        configurarBuscador();
    }

    // ─────────────────────────────────────────────────────────
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
        try (ResultSet rs = new EmpleadosDAO().obtenerTodos()) {
            while (rs.next()) {
                listaCompleta.add(new Empleados(
                        rs.getInt   ("ID"),
                        rs.getString("Nombre"),
                        rs.getString("Puesto"),
                        rs.getByte   ("Edad"),
                        rs.getString("RFC"),
                        rs.getString("CURP")
                ));
            }
        } catch (SQLException e) {
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

    // ─────────────────────────────────────────────────────────
    //  Acciones
    // ─────────────────────────────────────────────────────────
    @FXML public void generarReporte() { /* TODO */ }
    @FXML public void crear()          { abrirModal("CrEmpleados.fxml"); }
    @FXML public void editar()         { abrirModal("CrEmpleados.fxml"); }

    @FXML public void eliminar() {
        Empleados seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona un empleado primero.");
            return;
        }

        // Confirmación antes de borrar
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar a " + seleccionado.getNombre() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                new EmpleadosDAO().eliminar(seleccionado);
                cargarDatos(); // refresca la tabla
            }
        });
    }

    // ─────────────────────────────────────────────────────────
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}