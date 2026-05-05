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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.stage.Popup;
import javafx.scene.layout.VBox;

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

    private Popup popupClientes;

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

    // Mapa que guarda las cantidades originales de la venta que se está editando
    // clave = claveProducto, valor = cantidad original
    private final Map<Integer, Integer> stockOriginal = new HashMap<>();

    // ─────────────────────────────────────────────────────────
    //  Inner class: fila de producto
    // ─────────────────────────────────────────────────────────
    private class FilaProducto {
        final HBox                hbox;
        final ComboBox<Productos> cmbProducto;
        final Spinner<Integer>    spnCantidad;
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

            // ── Spinner de cantidad ───────────────────────────
            SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1);
            spnCantidad = new Spinner<>();
            spnCantidad.setValueFactory(factory);
            spnCantidad.setEditable(true);
            spnCantidad.setPrefWidth(80);
            spnCantidad.getStyleClass().add("modal-spinner");

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

            // ── Listener producto seleccionado ────────────────
            cmbProducto.valueProperty().addListener((obs, ant, nuevo) -> {
                if (nuevo != null) {
                    lblPrecioUnit.setText(String.format("$%.2f", nuevo.getPrecio()));

                    // Calcular stock real disponible (restando lo que usan otras filas del mismo producto)
                    int stockDisponible = calcularStockDisponible(nuevo, this);
                    factory.setMax(stockDisponible);
                    factory.setValue(Math.min(factory.getValue(), stockDisponible));
                } else {
                    lblPrecioUnit.setText("$0.00");
                    factory.setMax(1);
                }
                actualizarSubtotalFila(this);
                recalcularTotales();
            });

            // ── Listener cantidad ─────────────────────────────
            spnCantidad.valueProperty().addListener((obs, ant, nuevo) -> {
                if (nuevo != null && cmbProducto.getValue() != null) {
                    int stockDisponible = calcularStockDisponible(cmbProducto.getValue(), this);
                    if (nuevo > stockDisponible) {
                        factory.setValue(stockDisponible);
                        return;
                    }
                }
                actualizarSubtotalFila(this);
                recalcularTotales();
            });

            // Validar también cuando se escribe directo en el spinner
            spnCantidad.getEditor().textProperty().addListener((obs, ant, nuevo) -> {
                try {
                    int valor = Integer.parseInt(nuevo);
                    if (cmbProducto.getValue() != null) {
                        int stockDisponible = calcularStockDisponible(cmbProducto.getValue(), this);
                        if (valor > stockDisponible) {
                            spnCantidad.getEditor().setText(String.valueOf(stockDisponible));
                        } else if (valor < 1) {
                            spnCantidad.getEditor().setText("1");
                        }
                    }
                } catch (NumberFormatException ignored) {}
                actualizarSubtotalFila(this);
                recalcularTotales();
            });

            // ── Ensamble del HBox ─────────────────────────────
            hbox = new HBox(8, cmbProducto, spnCantidad, lblPrecioUnit, lblSubtotalFila, btnEliminar);
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setPadding(new Insets(2, 4, 2, 4));
        }

        double getSubtotal() {
            Productos p = cmbProducto.getValue();
            if (p == null) return 0;
            return p.getPrecio() * spnCantidad.getValue();
        }

        int getCantidad() {
            return spnCantidad.getValue();
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Inicialización
    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        if (txtIdEmpleado != null) {
            txtIdEmpleado.setEditable(false);

            // Opcional: Si además quieres que se vea "gris" para que sea obvio que está bloqueado:
            txtIdEmpleado.setDisable(true);
        }
        cargarClientes();
        cargarProductos();
        configurarBuscadorCliente();
        agregarFila(); // Una fila vacía de inicio
        Empleados actual = Sesion.getInstance().getUsuarioActual();
        if (actual != null) txtIdEmpleado.setText(String.valueOf(actual.getId()));

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
        // Creamos el Popup una sola vez
        popupClientes = new Popup();
        popupClientes.setAutoHide(true);

        txtBuscarCliente.textProperty().addListener((obs, ant, nuevo) -> {
            if (nuevo == null || nuevo.isBlank()) {
                popupClientes.hide();
                return;
            }

            String f = nuevo.toLowerCase().trim();
            List<Clientes> filtrados = todosClientes.stream()
                    .filter(c -> c.getNombre().toLowerCase().contains(f)
                            || c.getRfc().toLowerCase().contains(f)
                            || c.getTelefono().contains(f))
                    .toList();

            if (filtrados.isEmpty()) {
                popupClientes.hide();
                return;
            }

            // Construimos el contenido del popup
            VBox contenido = new VBox();
            contenido.getStylesheets().add(
                    getClass().getResource("/mx/nanosip/nanosip/Styles.css").toExternalForm()
            );
            contenido.getStyleClass().add("search-dropdown");
            contenido.setMaxWidth(txtBuscarCliente.getWidth());
            contenido.setMinWidth(txtBuscarCliente.getWidth());

            for (Clientes c : filtrados) {
                javafx.scene.control.Label opcion =
                        new javafx.scene.control.Label(c.getNombre() + "  |  " + c.getRfc());
                opcion.setMaxWidth(Double.MAX_VALUE);
                opcion.setPadding(new javafx.geometry.Insets(6, 12, 6, 12));
                opcion.getStyleClass().add("dropdown-item");
                opcion.setOnMouseClicked(e -> {
                    seleccionarCliente(c);
                    popupClientes.hide();
                });
                contenido.getChildren().add(opcion);
            }

            popupClientes.getContent().clear();
            popupClientes.getContent().add(contenido);

            // Posicionamos el popup justo debajo del campo de búsqueda
            javafx.geometry.Bounds bounds = txtBuscarCliente.localToScreen(
                    txtBuscarCliente.getBoundsInLocal()
            );

            // Si el nodo aún no está en pantalla, no mostramos el popup
            if (bounds == null) return;

            if (!popupClientes.isShowing()) {
                popupClientes.show(
                        txtBuscarCliente,
                        bounds.getMinX(),
                        bounds.getMaxY() + 2
                );
            } else {
                popupClientes.setX(bounds.getMinX());
                popupClientes.setY(bounds.getMaxY() + 2);
            }
        });

        // Ocultamos el popup si el campo pierde foco
        txtBuscarCliente.focusedProperty().addListener((obs, ant, nuevo) -> {
            if (!nuevo) {
                // Pequeño delay para permitir que el click en una opción se registre
                javafx.animation.PauseTransition pause =
                        new javafx.animation.PauseTransition(javafx.util.Duration.millis(150));
                pause.setOnFinished(e -> popupClientes.hide());
                pause.play();
            }
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

        // Solo mostramos el popup si el nodo ya está en pantalla
        if (popupClientes != null && popupClientes.isShowing()) {
            popupClientes.hide();
        }
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

        todosClientes.stream()
                .filter(c -> c.getId().equals(v.getIdClientes()))
                .findFirst()
                .ifPresent(this::seleccionarCliente);

        filas.clear();
        contenedorFilas.getChildren().clear();
        stockOriginal.clear();

        try {
            List<VentasProductos> detalles = apiVentas.obtenerDetalles(v.getNumero());

            for (VentasProductos detalle : detalles) {
                // Guardamos las cantidades originales para calcular stock disponible correctamente
                stockOriginal.merge(detalle.getClaveProducto(),
                        detalle.getCantidad() != null ? detalle.getCantidad() : 0,
                        Integer::sum);

                FilaProducto fila = new FilaProducto(todosProductos);

                todosProductos.stream()
                        .filter(p -> p.getClave().equals(detalle.getClaveProducto()))
                        .findFirst()
                        .ifPresent(fila.cmbProducto::setValue);

                fila.spnCantidad.getValueFactory().setValue(
                        detalle.getCantidad() != null ? detalle.getCantidad() : 1);

                filas.add(fila);
                contenedorFilas.getChildren().add(fila.hbox);
            }

            recalcularTotales();

        } catch (Exception e) {
            System.err.println("Error cargando detalles de venta: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Guardar asdasdasdasdasdasd
    // ─────────────────────────────────────────────────────────
    @FXML
    public void guardar() {
        if (!validar()) return;

        try {
            double totalCalculado = filas.stream()
                    .mapToDouble(FilaProducto::getSubtotal).sum() * 1.16;

            int idVendedor = Sesion.getInstance().getUsuarioActual().getId();
            Integer idCliente = clienteSeleccionado.getId();

            ProductosAPI apiProductos2 = new ProductosAPI();

            if (ventaEditar == null) {
                // ── CREAR NUEVA VENTA ──
                Ventas nueva = new Ventas();
                nueva.setIdEmpleado(idVendedor);
                nueva.setIdClientes(idCliente);
                nueva.setMonto(totalCalculado);

                Ventas ventaGuardada = apiVentas.guardar(nueva);

                for (FilaProducto fila : filas) {
                    VentasProductos detalle = new VentasProductos();
                    detalle.setNumeroVenta(ventaGuardada.getNumero());
                    detalle.setClaveProducto(fila.cmbProducto.getValue().getClave());
                    detalle.setCantidad(fila.getCantidad());
                    apiVentas.guardarDetalle(detalle);

                    // Descontar inventario
                    apiProductos2.actualizarInventario(
                            fila.cmbProducto.getValue().getClave(),
                            -fila.getCantidad());
                }

            } else {
                // ── EDITAR VENTA EXISTENTE ──

                // 1. Restaurar stock de los productos originales
                for (Map.Entry<Integer, Integer> entry : stockOriginal.entrySet()) {
                    apiProductos2.actualizarInventario(entry.getKey(), entry.getValue());
                }

                // 2. Eliminar detalles anteriores
                apiVentas.eliminarDetalles(ventaEditar.getNumero());

                // 3. Actualizar la venta
                ventaEditar.setIdClientes(idCliente);
                ventaEditar.setMonto(totalCalculado);
                apiVentas.actualizar(ventaEditar);

                // 4. Guardar nuevos detalles y descontar nuevo inventario
                for (FilaProducto fila : filas) {
                    VentasProductos detalle = new VentasProductos();
                    detalle.setNumeroVenta(ventaEditar.getNumero());
                    detalle.setClaveProducto(fila.cmbProducto.getValue().getClave());
                    detalle.setCantidad(fila.getCantidad());
                    apiVentas.guardarDetalle(detalle);

                    // Descontar inventario nuevo
                    apiProductos2.actualizarInventario(
                            fila.cmbProducto.getValue().getClave(),
                            -fila.getCantidad());
                }
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
        boolean cantidadInvalida = filas.stream()
                .anyMatch(f -> f.getCantidad() <= 0);
        if (cantidadInvalida) {
            mostrarError("Las cantidades deben ser números mayores a 0.");
            return false;
        }
        return true;
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(msg);

        DialogPane dp = alert.getDialogPane();
        dp.getStylesheets().add(
                getClass().getResource("/mx/nanosip/nanosip/Styles.css").toExternalForm()
        );
        dp.getStyleClass().add("alert-pane");

        alert.showAndWait();
    }

    @FXML public void cerrarModal() { if (modalStage != null) modalStage.close(); }
    @FXML public void minimizar()   { if (modalStage != null) modalStage.setIconified(true); }

    @Override
    public void setModalStage(Stage stage) { this.modalStage = stage; }

    private int calcularStockDisponible(Productos producto, FilaProducto filaActual) {
        // Stock real del producto
        int stockReal = producto.getInventario();

        // Si estamos editando una venta, sumamos el stock que ya estaba comprometido
        // por esta venta (para no penalizar dos veces)
        int yaComprometidoEnEstaVenta = 0;
        if (ventaEditar != null) {
            yaComprometidoEnEstaVenta = stockOriginal.getOrDefault(producto.getClave(), 0);
        }

        // Restamos lo que otras filas de esta misma sesión ya están usando del mismo producto
        int usadoEnOtrasFilas = filas.stream()
                .filter(f -> f != filaActual)
                .filter(f -> f.cmbProducto.getValue() != null)
                .filter(f -> f.cmbProducto.getValue().getClave().equals(producto.getClave()))
                .mapToInt(FilaProducto::getCantidad)
                .sum();

        return Math.max(0, stockReal + yaComprometidoEnEstaVenta - usadoEnOtrasFilas);
    }
}