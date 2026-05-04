package mx.nanosip.nanosip.Controllers.Modals;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import mx.nanosip.nanosip.Controllers.Backend.Productos;
import mx.nanosip.nanosip.Controllers.Backend.ProductosAPI;
import mx.nanosip.nanosip.Controllers.Backend.Proveedores;
import mx.nanosip.nanosip.Controllers.Backend.ProveedoresAPI;

import java.util.ArrayList;
import java.util.List;

public class CrProductosController implements ModalController {

    @FXML private Label            lblTitulo;
    @FXML private TextField        txtClave;
    @FXML private TextField        txtNombre;
    @FXML private TextField        txtDescripcion;
    @FXML private TextField        txtMarca;
    @FXML private Spinner<Integer> spnExistencia;
    @FXML private TextField        txtPrecio;
    @FXML private TextField        txtCosto;
    @FXML private TextField        txtBuscarProveedor;
    @FXML private ListView<CheckBox> listaProveedores;

    private Stage    modalStage;
    private Productos productoEditar = null;

    private final ProductosAPI   apiProductos   = new ProductosAPI();
    private final ProveedoresAPI apiProveedores = new ProveedoresAPI();

    private final ObservableList<CheckBox> todosProveedores = FXCollections.observableArrayList();

    @Override
    public void setModalStage(Stage stage) { this.modalStage = stage; }

    @FXML
    public void initialize() {
        spnExistencia.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999999, 0));
        cargarProveedores();
        txtBuscarProveedor.textProperty().addListener((obs, ant, nuevo) -> filtrarProveedores(nuevo));
    }

    private void cargarProveedores() {
        todosProveedores.clear();
        try {
            List<Proveedores> lista = apiProveedores.obtenerTodos();
            for (Proveedores p : lista) {
                CheckBox cb = new CheckBox(p.getNombre());
                cb.setUserData(p.getId());
                todosProveedores.add(cb);
            }
        } catch (Exception e) {
            System.err.println("Error cargando proveedores: " + e.getMessage());
        }
        listaProveedores.setItems(todosProveedores);
    }

    public void setProducto(Productos p) {
        this.productoEditar = p;
        lblTitulo.setText("Editar Producto");
        txtClave      .setText(String.valueOf(p.getClave()));
        txtClave      .setDisable(true);
        txtNombre     .setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtMarca      .setText(p.getMarca());
        spnExistencia.getValueFactory().setValue(p.getInventario());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtCosto .setText(String.valueOf(p.getCosto()));


        todosProveedores.forEach(cb -> cb.setSelected(false));

        try {
            // 1. Llamamos a la API que ya devuelve el JSON que viste en el navegador
            List<Integer> idsAsignados = apiProductos.obtenerIdsProveedoresPorProducto(p.getClave());

            // 2. Marcamos visualmente los CheckBoxes
            for (CheckBox cb : todosProveedores) {
                // MUY IMPORTANTE: Asegúrate de que al cargar los CheckBoxes
                // les hayas asignado el ID del proveedor en el UserData
                Integer idProvEnCheckBox = (Integer) cb.getUserData();

                if (idsAsignados.contains(idProvEnCheckBox)) {
                    cb.setSelected(true);
                } else {
                    cb.setSelected(false);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al marcar proveedores: " + e.getMessage());
        }
    }

    private void filtrarProveedores(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            listaProveedores.setItems(todosProveedores);
            return;
        }
        String lower = filtro.toLowerCase();
        ObservableList<CheckBox> filtrados = FXCollections.observableArrayList(
                todosProveedores.stream()
                        .filter(cb -> cb.getText().toLowerCase().contains(lower))
                        .toList());
        listaProveedores.setItems(filtrados);
    }

    private List<Integer> getIdsProveedoresSeleccionados() {
        List<Integer> ids = new ArrayList<>();
        for (CheckBox cb : todosProveedores)
            if (cb.isSelected()) ids.add((Integer) cb.getUserData());
        return ids;
    }

    @FXML
    public void guardar() {
        if (!validar()) return;
        try {
            double precio     = Double.parseDouble(txtPrecio.getText().trim());
            double costo      = Double.parseDouble(txtCosto.getText().trim());
            int    existencia = spnExistencia.getValue();

            int claveProductoFinal;

            if (productoEditar == null) {
                // ── CREAR NUEVO ──
                Productos nuevo = new Productos(
                        txtNombre.getText().trim(),
                        txtMarca.getText().trim(),
                        txtDescripcion.getText().trim(),
                        existencia, precio, costo);

                // Importante: Tu API debe retornar el objeto con la Clave generada
                Productos guardado = apiProductos.guardar(nuevo);
                claveProductoFinal = guardado.getClave();
            } else {
                // ── EDITAR EXISTENTE ──
                claveProductoFinal = productoEditar.getClave();
                productoEditar.setNombre(txtNombre.getText().trim());
                productoEditar.setMarca(txtMarca.getText().trim());
                productoEditar.setDescripcion(txtDescripcion.getText().trim());
                productoEditar.setInventario(existencia);
                productoEditar.setPrecio(precio);
                productoEditar.setCosto(costo);

                apiProductos.actualizar(productoEditar);

                // Limpiamos los proveedores anteriores para re-insertar los nuevos
                apiProductos.eliminarProveedoresDeProducto(claveProductoFinal);
            }

            // ── GUARDAR RELACIÓN CON PROVEEDORES ──
            List<Integer> proveedoresIds = getIdsProveedoresSeleccionados();
            for (Integer idProv : proveedoresIds) {
                apiProductos.guardarProveedorProducto(claveProductoFinal, idProv);
            }

            cerrarModal();
        } catch (Exception e) {
            alerta("Error al guardar: " + e.getMessage());
        }
    }

    private boolean validar() {
        if (txtNombre.getText().isBlank()) { alerta("El nombre es obligatorio."); return false; }
        if (txtMarca.getText().isBlank())  { alerta("La marca es obligatoria.");  return false; }
        if (txtPrecio.getText().isBlank()) { alerta("El precio es obligatorio."); return false; }
        if (txtCosto.getText().isBlank())  { alerta("El costo es obligatorio.");  return false; }
        return true;
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    @FXML public void cerrarModal() { if (modalStage != null) modalStage.close(); }
    @FXML public void minimizar()   { if (modalStage != null) modalStage.setIconified(true); }
}