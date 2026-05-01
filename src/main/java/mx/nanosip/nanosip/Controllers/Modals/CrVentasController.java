package mx.nanosip.nanosip.Controllers.Modals;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CrVentasController implements ModalController {

    // ── FXML ──────────────────────────────────────────────────
    @FXML private TextField txtNumVenta;
    @FXML private TextField txtIdEmpleado;
    @FXML private TextField txtBuscarCliente;
    @FXML private VBox      dropdownClientes;
    @FXML private Label     lblClienteSeleccionado;
    @FXML private VBox      contenedorFilas;
    @FXML private ScrollPane scrollProductos;
    @FXML private Label     lblSubtotal;
    @FXML private Label     lblIva;
    @FXML private Label     lblTotal;

    private Stage modalStage;
    private final List<FilaProducto> filas = new ArrayList<>();

    // ── Datos mock (reemplazar con API/BD) ────────────────────
    // Formato: {clave, nombre, precio}
    private final List<String[]> productosMock = List.of(
            new String[]{"P001", "Silla Ejecutiva",   "1500.00"},
            new String[]{"P002", "Escritorio Roble",  "3200.00"},
            new String[]{"P003", "Monitor 24\"",      "4500.00"},
            new String[]{"P004", "Teclado Mecánico",  "850.00"},
            new String[]{"P005", "Mouse Inalámbrico", "350.00"}
    );

    private final List<String> clientesMock = List.of(
            "García López, Juan",
            "Martínez Ruiz, Ana",
            "Distribuidora Norte S.A.",
            "Comercial Del Valle",
            "Hernández Pérez, Luis"
    );

    private String clienteSeleccionado = null;

    private static final NumberFormat CURRENCY =
            NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    // ─────────────────────────────────────────────────────────
    //  Inicialización
    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Búsqueda de cliente con dropdown
        txtBuscarCliente.textProperty().addListener((obs, anterior, nuevo) -> {
            if (clienteSeleccionado != null) {
                clienteSeleccionado = null;
                lblClienteSeleccionado.setVisible(false);
                lblClienteSeleccionado.setManaged(false);
            }
            filtrarClientes(nuevo);
        });

        // Ocultar dropdown si pierde foco
        txtBuscarCliente.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ocultarDropdownClientes();
        });

        // Primera fila al abrir
        agregarFila();
    }

    // ─────────────────────────────────────────────────────────
    //  Dropdown de clientes
    // ─────────────────────────────────────────────────────────
    private void filtrarClientes(String filtro) {
        dropdownClientes.getChildren().clear();

        if (filtro == null || filtro.isBlank()) {
            ocultarDropdownClientes();
            return;
        }

        String lower = filtro.toLowerCase();
        List<String> resultados = clientesMock.stream()
                .filter(c -> c.toLowerCase().contains(lower))
                .toList();

        if (resultados.isEmpty()) {
            ocultarDropdownClientes();
            return;
        }

        for (String cliente : resultados) {
            Label item = new Label(cliente);
            item.getStyleClass().add("dropdown-item");
            item.setMaxWidth(Double.MAX_VALUE);
            item.setOnMousePressed(e -> seleccionarCliente(cliente));
            dropdownClientes.getChildren().add(item);
        }

        dropdownClientes.setVisible(true);
        dropdownClientes.setManaged(true);
    }

    private void seleccionarCliente(String cliente) {
        clienteSeleccionado = cliente;
        txtBuscarCliente.setText(cliente);
        ocultarDropdownClientes();
        lblClienteSeleccionado.setText("✔  " + cliente);
        lblClienteSeleccionado.setVisible(true);
        lblClienteSeleccionado.setManaged(true);
    }

    private void ocultarDropdownClientes() {
        dropdownClientes.setVisible(false);
        dropdownClientes.setManaged(false);
        dropdownClientes.getChildren().clear();
    }

    // ─────────────────────────────────────────────────────────
    //  Filas de productos
    // ─────────────────────────────────────────────────────────
    @FXML
    public void agregarFila() {
        FilaProducto fila = new FilaProducto();
        filas.add(fila);
        contenedorFilas.getChildren().add(fila.getRoot());

        // Hacer scroll al fondo al agregar nueva fila
        scrollProductos.layout();
        scrollProductos.setVvalue(1.0);
    }

    private void eliminarFila(FilaProducto fila) {
        filas.remove(fila);
        contenedorFilas.getChildren().remove(fila.getRoot());
        recalcularTotal();
    }

    // ─────────────────────────────────────────────────────────
    //  Cálculo del total
    // ─────────────────────────────────────────────────────────
    void recalcularTotal() {
        double subtotal = filas.stream()
                .mapToDouble(FilaProducto::getSubtotal)
                .sum();
        double iva   = subtotal * 0.16;
        double total = subtotal + iva;

        lblSubtotal.setText(CURRENCY.format(subtotal));
        lblIva.setText(CURRENCY.format(iva));
        lblTotal.setText(CURRENCY.format(total));
    }

    // ─────────────────────────────────────────────────────────
    //  Guardar
    // ─────────────────────────────────────────────────────────
    @FXML
    public void guardar() {
        // TODO: validar campos y llamar al servicio/API
        cerrarModal();
    }

    @FXML
    public void cerrarModal() {
        if (modalStage != null) modalStage.close();
    }

    @FXML
    public void minimizar() {
        if (modalStage != null) modalStage.setIconified(true);
    }

    @Override
    public void setModalStage(Stage stage) {
        this.modalStage = stage;
    }

    // ═════════════════════════════════════════════════════════
    //  Clase interna — representa una fila de producto
    // ═════════════════════════════════════════════════════════
    private class FilaProducto {

        private final StackPane root;
        private final VBox      contenido;
        private final TextField txtBuscar;
        private final VBox      dropdown;
        private final Label     lblConfirmacion;
        private final TextField txtCantidad;
        private final Label     lblPrecio;
        private final Label     lblSubtotalFila;
        private final Button    btnEliminar;

        private String   claveSeleccionada = null;
        private double   precioUnitario    = 0.0;

        FilaProducto() {
            // ── Campo búsqueda + dropdown ──────────────────────
            txtBuscar = new TextField();
            txtBuscar.setPromptText("🔍  Clave o nombre…");
            txtBuscar.getStyleClass().add("login-field");
            txtBuscar.setPrefWidth(220);

            dropdown = new VBox();
            dropdown.getStyleClass().add("search-dropdown");
            dropdown.setVisible(false);
            dropdown.setManaged(false);
            dropdown.setTranslateY(36);

            StackPane stackBuscar = new StackPane(txtBuscar, dropdown);
            stackBuscar.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            stackBuscar.setPrefWidth(220);

            lblConfirmacion = new Label();
            lblConfirmacion.getStyleClass().add("field-confirm");
            lblConfirmacion.setVisible(false);
            lblConfirmacion.setManaged(false);

            VBox colProducto = new VBox(4, stackBuscar, lblConfirmacion);
            colProducto.setPrefWidth(220);

            // ── Cantidad ───────────────────────────────────────
            txtCantidad = new TextField("1");
            txtCantidad.getStyleClass().add("login-field");
            txtCantidad.setPrefWidth(70);
            txtCantidad.setAlignment(Pos.CENTER);

            // ── Precio unitario (solo lectura) ─────────────────
            lblPrecio = new Label("$0.00");
            lblPrecio.getStyleClass().add("fila-precio");
            lblPrecio.setPrefWidth(100);
            lblPrecio.setAlignment(Pos.CENTER_RIGHT);

            // ── Subtotal fila ──────────────────────────────────
            lblSubtotalFila = new Label("$0.00");
            lblSubtotalFila.getStyleClass().add("fila-subtotal");
            lblSubtotalFila.setPrefWidth(100);
            lblSubtotalFila.setAlignment(Pos.CENTER_RIGHT);

            // ── Botón eliminar ─────────────────────────────────
            btnEliminar = new Button("✕");
            btnEliminar.getStyleClass().add("fila-btn-eliminar");
            btnEliminar.setPrefWidth(32);
            btnEliminar.setPrefHeight(32);
            btnEliminar.setOnAction(e -> eliminarFila(this));

            // ── HBox de la fila ────────────────────────────────
            HBox fila = new HBox(8, colProducto, txtCantidad,
                    lblPrecio, lblSubtotalFila, btnEliminar);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("producto-fila");
            fila.setPadding(new javafx.geometry.Insets(6, 6, 6, 6));

            contenido = new VBox(fila);
            root = new StackPane(contenido);

            // ── Listeners ──────────────────────────────────────
            txtBuscar.textProperty().addListener((obs, ant, nuevo) -> {
                claveSeleccionada = null;
                lblConfirmacion.setVisible(false);
                lblConfirmacion.setManaged(false);
                filtrarProductos(nuevo);
            });

            txtBuscar.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) ocultarDropdown();
            });

            txtCantidad.textProperty().addListener((obs, ant, nuevo) ->
                    actualizarSubtotalFila());
        }

        // ── Filtrado de productos ──────────────────────────────
        private void filtrarProductos(String filtro) {
            dropdown.getChildren().clear();

            if (filtro == null || filtro.isBlank()) {
                ocultarDropdown();
                return;
            }

            String lower = filtro.toLowerCase();
            List<String[]> resultados = productosMock.stream()
                    .filter(p -> p[0].toLowerCase().contains(lower)
                            || p[1].toLowerCase().contains(lower))
                    .toList();

            if (resultados.isEmpty()) {
                ocultarDropdown();
                return;
            }

            for (String[] prod : resultados) {
                // prod: [clave, nombre, precio]
                Label item = new Label(prod[0] + " — " + prod[1]
                        + "  (" + CURRENCY.format(Double.parseDouble(prod[2])) + ")");
                item.getStyleClass().add("dropdown-item");
                item.setMaxWidth(Double.MAX_VALUE);
                item.setOnMousePressed(e -> seleccionarProducto(prod));
                dropdown.getChildren().add(item);
            }

            dropdown.setVisible(true);
            dropdown.setManaged(true);
        }

        private void seleccionarProducto(String[] prod) {
            claveSeleccionada = prod[0];
            precioUnitario    = Double.parseDouble(prod[2]);

            txtBuscar.setText(prod[0] + " — " + prod[1]);
            ocultarDropdown();

            lblConfirmacion.setText("✔  " + prod[1]);
            lblConfirmacion.setVisible(true);
            lblConfirmacion.setManaged(true);

            lblPrecio.setText(CURRENCY.format(precioUnitario));
            actualizarSubtotalFila();
        }

        private void ocultarDropdown() {
            dropdown.setVisible(false);
            dropdown.setManaged(false);
            dropdown.getChildren().clear();
        }

        // ── Cálculo del subtotal de esta fila ──────────────────
        private void actualizarSubtotalFila() {
            double subtotal = getSubtotal();
            lblSubtotalFila.setText(CURRENCY.format(subtotal));
            recalcularTotal();
        }

        double getSubtotal() {
            try {
                int cantidad = Integer.parseInt(txtCantidad.getText().trim());
                return cantidad > 0 ? precioUnitario * cantidad : 0.0;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        StackPane getRoot() { return root; }
    }
}
