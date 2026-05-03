package mx.nanosip.nanosip.Controllers.Modals;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mx.nanosip.nanosip.Controllers.Backend.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CrVentasController implements ModalController {

    @FXML private Label     lblTitulo;
    @FXML private TextField txtNumVenta;
    @FXML private TextField txtIdEmpleado;
    @FXML private TextField txtBuscarCliente;
    @FXML private VBox      dropdownClientes;
    @FXML private Label     lblClienteSeleccionado;
    @FXML private VBox      contenedorFilas;
    @FXML private Label     lblSubtotal;
    @FXML private Label     lblIva;
    @FXML private Label     lblTotal;

    private Stage    modalStage;
    private Ventas   ventaEditar          = null;
    private Clientes clienteSeleccionado  = null;

    private final VentasAPI    apiVentas    = new VentasAPI();
    private final ClientesAPI  apiClientes  = new ClientesAPI();
    private final ProductosAPI apiProductos = new ProductosAPI();

    private List<Clientes>  todosClientes  = new ArrayList<>();
    private List<Productos> todosProductos = new ArrayList<>();

    // Cada fila dinámica de la tabla de productos
    private final List<FilaProducto> filas = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    //  Inner class: fila de producto
    // ─────────────────────────────────────────────────────────
    private class FilaProducto {
        final HBox              hbox;
        final ComboBox<Productos> cmbProducto;
        final TextField           txtCantidad;
        final Label               lblPrecioUnit;
        final Label               lblSubtotalFila;

        FilaProducto(List<Productos> productos) {
            // ── Producto ──────────────────────────────────────
            cmbProducto = new ComboBox<>();
            cmbProducto.getItems().addAll(productos);
            cmbProducto.setPromptText("Seleccionar producto…");
            cmbProducto.setPrefWidth(220);
            cmbProducto.setConverter(new javafx.util.StringConverter<>() {
                public String toString(Productos p)   { return p == null ? "" : p.getNombre(); }
                public Productos fromString(String s) { return null; }
            });

            // ── Cantidad ──────────────────────────────────────
            txtCantidad = new TextField("1");
            txtCantidad.setPrefWidth(70);
            txtCantidad.setAlignment(Pos.CENTER);

            // ── Precio unitario ───────────────────────────────
            lblPrecioUnit = new Label("$0.00");
            lblPrecioUnit.setPrefWidth(100);
            lblPrecioUnit.setAlignment(Pos.CENTER_RIGHT);

            // ── Subtotal fila ─────────────────────────────────
            lblSubtotalFila = new Label("$0.00");
            lblSubtotalFila.setPrefWidth(100);
            lblSubtotalFila.setAlignment(Pos.CENTER_RIGHT);

            // ── Botón eliminar ────────────────────────────────
            Button btnEliminar = new Button("✕");
            btnEliminar.setPrefWidth(32);
            btnEliminar.getStyleClass().add("wmbtn-close");
            btnEliminar.setOnAction(e -> eliminarFila(this));

            // ── Listeners para recalcular ─────────────────────
            cmbProducto.valueProperty().addListener((obs, ant, nuevo) -> {
                if (nuevo != null) {
                    lblPrecioUnit.setText(String.format("$%.2f", nuevo.getPrecio()));
                } else {
                    lblPrecioUnit.setText("$0.00");
                }
                actualizarSubtotalFila(this);
                recalcularTotales();
            });

            txtCantidad.textProperty().addListener((obs, ant, nuevo) -> {
                actualizarSubtotalFila(this);
                recalcularTotales();
            });

            // ── Ensamble del HBox ─────────────────────────────
            hbox = new HBox(8, cmbProducto, txtCantidad, lblPrecioUnit, lblSubtotalFila, btnEliminar);
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setPadding(new Insets(2, 4, 2, 4));
            HBox.setHgrow(cmbProducto, Priority.NEVER);
        }

        double getSubtotal() {
            Productos p = cmbProducto.getValue();
            if (p == null) return 0;
            try {
                int cant = Integer.parseInt(txtCantidad.getText().trim());
                return p.getPrecio() * cant;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Inicialización
    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        cargarClientes();
        cargarProductos();
        configurarBuscadorCliente();
        agregarFila(); // Una fila vacía de inicio
    }

    private void cargarClientes() {
        try {
            todosClientes = apiClientes.obtenerTodos();
        } catch (Exception e) {
            System.err.println("Error cargando clientes: " + e.getMessage());
        }
    }

    private void cargarProductos() {
        try {
            todosProductos = apiProductos.obtenerTodos();
        } catch (Exception e) {
            System.err.println("Error cargando productos: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Buscador de clientes con dropdown
    // ─────────────────────────────────────────────────────────
    private void configurarBuscadorCliente() {
        txtBuscarCliente.textProperty().addListener((obs, ant, nuevo) -> {
            if (nuevo == null || nuevo.isBlank()) {
                dropdownClientes.setVisible(false);
                dropdownClientes.setManaged(false);
                return;
            }
            String f = nuevo.toLowerCase().trim();
            List<Clientes> filtrados = todosClientes.stream()
                    .filter(c -> c.getNombre().toLowerCase().contains(f)
                            || c.getRfc().toLowerCase().contains(f)
                            || c.getTelefono().contains(f))
                    .toList();

            dropdownClientes.getChildren().clear();
            for (Clientes c : filtrados) {
                Label opcion = new Label(c.getNombre() + "  |  " + c.getRfc());
                opcion.setMaxWidth(Double.MAX_VALUE);
                opcion.setPadding(new Insets(6, 12, 6, 12));
                opcion.getStyleClass().add("dropdown-item");
                opcion.setOnMouseClicked(e -> seleccionarCliente(c));
                dropdownClientes.getChildren().add(opcion);
            }

            boolean hay = !filtrados.isEmpty();
            dropdownClientes.setVisible(hay);
            dropdownClientes.setManaged(hay);
        });
    }

    private void seleccionarCliente(Clientes c) {
        clienteSeleccionado = c;
        txtBuscarCliente.setText(c.getNombre());
        dropdownClientes.setVisible(false);
        dropdownClientes.setManaged(false);
        lblClienteSeleccionado.setText("✓ " + c.getNombre() + "  —  RFC: " + c.getRfc());
        lblClienteSeleccionado.setVisible(true);
        lblClienteSeleccionado.setManaged(true);
    }

    // ─────────────────────────────────────────────────────────
    //  Agregar / eliminar filas
    // ─────────────────────────────────────────────────────────
    @FXML
    public void agregarFila() {
        FilaProducto fila = new FilaProducto(todosProductos);
        filas.add(fila);
        contenedorFilas.getChildren().add(fila.hbox);
        recalcularTotales();
    }

    private void eliminarFila(FilaProducto fila) {
        filas.remove(fila);
        contenedorFilas.getChildren().remove(fila.hbox);
        recalcularTotales();
    }

    // ─────────────────────────────────────────────────────────
    //  Cálculos
    // ─────────────────────────────────────────────────────────
    private void actualizarSubtotalFila(FilaProducto fila) {
        fila.lblSubtotalFila.setText(String.format("$%.2f", fila.getSubtotal()));
    }

    private void recalcularTotales() {
        double subtotal = filas.stream().mapToDouble(FilaProducto::getSubtotal).sum();
        double iva      = subtotal * 0.16;
        double total    = subtotal + iva;
        lblSubtotal.setText(String.format("$%.2f", subtotal));
        lblIva     .setText(String.format("$%.2f", iva));
        lblTotal   .setText(String.format("$%.2f", total));
    }

    // ─────────────────────────────────────────────────────────
    //  Modo edición
    // ─────────────────────────────────────────────────────────
    public void setVenta(Ventas v) {
        this.ventaEditar = v;
        txtNumVenta.setText(String.valueOf(v.getNumero()));
        txtIdEmpleado.setText(String.valueOf(v.getIdEmpleado()));

        // Preseleccionar cliente
        todosClientes.stream()
                .filter(c -> c.getId().equals(v.getIdClientes()))
                .findFirst()
                .ifPresent(this::seleccionarCliente);
    }

    // ─────────────────────────────────────────────────────────
    //  Guardar
    // ─────────────────────────────────────────────────────────
    @FXML
    public void guardar() {
        if (!validar()) return;

        try {
            double total = filas.stream().mapToDouble(FilaProducto::getSubtotal).sum() * 1.16;
            int idEmpleado = txtIdEmpleado.getText().isBlank() ? 1
                    : Integer.parseInt(txtIdEmpleado.getText().trim());

            if (ventaEditar == null) {
                Ventas nueva = new Ventas(idEmpleado, clienteSeleccionado.getId(), total);
                nueva.setFecha(LocalDateTime.now());
                apiVentas.guardar(nueva);
            } else {
                ventaEditar.setIdClientes(clienteSeleccionado.getId());
                ventaEditar.setMonto(total);
                apiVentas.actualizar(ventaEditar);
            }
            cerrarModal();
        } catch (Exception e) {
            mostrarError("Error al registrar venta: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Validación
    // ─────────────────────────────────────────────────────────
    private boolean validar() {
        if (clienteSeleccionado == null) {
            mostrarError("Selecciona un cliente.");
            return false;
        }
        if (filas.isEmpty()) {
            mostrarError("Agrega al menos un producto.");
            return false;
        }
        boolean algunoSinProducto = filas.stream()
                .anyMatch(f -> f.cmbProducto.getValue() == null);
        if (algunoSinProducto) {
            mostrarError("Selecciona el producto en todas las filas.");
            return false;
        }
        boolean cantidadInvalida = filas.stream().anyMatch(f -> {
            try { return Integer.parseInt(f.txtCantidad.getText().trim()) <= 0; }
            catch (NumberFormatException e) { return true; }
        });
        if (cantidadInvalida) {
            mostrarError("Las cantidades deben ser números mayores a 0.");
            return false;
        }
        return true;
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML public void cerrarModal() { if (modalStage != null) modalStage.close(); }
    @FXML public void minimizar()   { if (modalStage != null) modalStage.setIconified(true); }

    @Override
    public void setModalStage(Stage stage) { this.modalStage = stage; }
}