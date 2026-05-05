package mx.nanosip.nanosip.Controllers.Backend;


import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReporteService {

    // Colores Nanosip
    private static final DeviceRgb COLOR_MORADO_OSCURO = new DeviceRgb(36, 32, 56);
    private static final DeviceRgb COLOR_MORADO_ACENTO = new DeviceRgb(114, 90, 193);
    private static final DeviceRgb COLOR_MORADO_CLARO  = new DeviceRgb(237, 233, 250);
    private static final DeviceRgb COLOR_TEXTO_GRIS    = new DeviceRgb(122, 116, 134);
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_SOLO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VentasAPI         ventasAPI         = new VentasAPI();
    private final EmpleadosAPI      empleadosAPI      = new EmpleadosAPI();
    private final ClientesAPI       clientesAPI       = new ClientesAPI();
    private final VentasProductosAPI ventasProductosAPI = new VentasProductosAPI();

    // ══════════════════════════════════════════════════════════
    //  REPORTE VENTAS
    // ══════════════════════════════════════════════════════════
    public void generarReporteVentas(String rutaArchivo,
                                     LocalDateTime fechaInicio,
                                     LocalDateTime fechaFin) throws Exception {

        List<Ventas>    todasVentas    = ventasAPI.obtenerTodos();
        List<Empleados> todosEmpleados = empleadosAPI.obtenerTodos();
        List<Clientes>  todosClientes  = clientesAPI.obtenerTodos();
        List<Productos> todosProductos = ProductosAPI.obtenerTodos();

        // Filtrar por rango de fechas
        List<Ventas> ventasFiltradas = todasVentas.stream()
                .filter(v -> v.getFecha() != null
                        && !v.getFecha().isBefore(fechaInicio)
                        && !v.getFecha().isAfter(fechaFin))
                .collect(Collectors.toList());

        // Obtener detalles de cada venta filtrada
        Map<Integer, List<VentasProductos>> detallesPorVenta = new HashMap<>();
        for (Ventas v : ventasFiltradas) {
            try {
                List<VentasProductos> det = ventasProductosAPI.obtenerPorVenta(v.getNumero());
                detallesPorVenta.put(v.getNumero(), det);
            } catch (Exception e) {
                detallesPorVenta.put(v.getNumero(), new ArrayList<>());
            }
        }

        // Monto total
        double montoTotal = ventasFiltradas.stream()
                .mapToDouble(Ventas::getMonto).sum();

        // Ventas por empleado
        Map<Integer, Double> montosPorEmpleado = new LinkedHashMap<>();
        Map<Integer, Long>   cantidadPorEmpleado = new LinkedHashMap<>();
        for (Ventas v : ventasFiltradas) {
            montosPorEmpleado.merge(v.getIdEmpleado(), v.getMonto(), Double::sum);
            cantidadPorEmpleado.merge(v.getIdEmpleado(), 1L, Long::sum);
        }

        // Producto más vendido
        Map<Integer, Integer> cantidadPorProducto = new HashMap<>();
        for (List<VentasProductos> detalles : detallesPorVenta.values()) {
            for (VentasProductos d : detalles) {
                cantidadPorProducto.merge(d.getClaveProducto(),
                        d.getCantidad() != null ? d.getCantidad() : 0, Integer::sum);
            }
        }
        Integer claveProductoTop = cantidadPorProducto.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);
        String nombreProductoTop = claveProductoTop == null ? "N/A" :
                todosProductos.stream()
                        .filter(p -> p.getClave().equals(claveProductoTop))
                        .map(Productos::getNombre)
                        .findFirst().orElse("ID:" + claveProductoTop);
        int cantidadProductoTop = claveProductoTop == null ? 0 :
                cantidadPorProducto.getOrDefault(claveProductoTop, 0);

        // Helpers
        Map<Integer, String> nombreEmpleado = new HashMap<>();
        for (Empleados e : todosEmpleados)
            nombreEmpleado.put(e.getId(), e.getNombre());

        // ── Generar PDF ──
        PdfWriter   writer = new PdfWriter(rutaArchivo);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf);

        agregarEncabezado(doc, "Reporte de Ventas",
                "Período: " + fechaInicio.format(FORMATO_SOLO_FECHA)
                        + "  →  " + fechaFin.format(FORMATO_SOLO_FECHA));

        // Resumen general
        agregarSubtitulo(doc, "Resumen General");
        Table resumen = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();
        agregarCeldaResumen(resumen, "Total de ventas en el período", String.valueOf(ventasFiltradas.size()));
        agregarCeldaResumen(resumen, "Monto total generado", String.format("$%.2f", montoTotal));
        agregarCeldaResumen(resumen, "Producto más vendido", nombreProductoTop + " (" + cantidadProductoTop + " uds.)");
        agregarCeldaResumen(resumen, "Empleado con más ventas",
                montosPorEmpleado.isEmpty() ? "N/A" :
                        nombreEmpleado.getOrDefault(
                                montosPorEmpleado.entrySet().stream()
                                        .max(Map.Entry.comparingByValue())
                                        .map(Map.Entry::getKey).orElse(-1), "Desconocido"));
        doc.add(resumen);
        doc.add(new Paragraph("\n"));

        // Tabla ventas por empleado
        agregarSubtitulo(doc, "Ventas por Empleado");
        Table tablaEmp = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                .useAllAvailableWidth();
        agregarCeldaEncabezado(tablaEmp, "Empleado");
        agregarCeldaEncabezado(tablaEmp, "N° de Ventas");
        agregarCeldaEncabezado(tablaEmp, "Monto Total");

        montosPorEmpleado.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    String nombre = nombreEmpleado.getOrDefault(entry.getKey(), "ID:" + entry.getKey());
                    long   cant   = cantidadPorEmpleado.getOrDefault(entry.getKey(), 0L);
                    agregarCeldaDato(tablaEmp, nombre);
                    agregarCeldaDato(tablaEmp, String.valueOf(cant));
                    agregarCeldaDato(tablaEmp, String.format("$%.2f", entry.getValue()));
                });
        doc.add(tablaEmp);
        doc.add(new Paragraph("\n"));

        // Gráfica de barras
        if (!montosPorEmpleado.isEmpty()) {
            agregarSubtitulo(doc, "Gráfica — Monto de Ventas por Empleado");
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            montosPorEmpleado.forEach((idEmp, monto) -> {
                String nombre = nombreEmpleado.getOrDefault(idEmp, "ID:" + idEmp);
                dataset.addValue(monto, "Monto", nombre);
            });
            JFreeChart grafica = ChartFactory.createBarChart(
                    "", "Empleado", "Monto ($)",
                    dataset, PlotOrientation.VERTICAL, false, false, false);
            estilizarGrafica(grafica);
            doc.add(chartToImage(grafica, 500, 260));
            doc.add(new Paragraph("\n"));
        }

        // Tabla detalle de ventas
        agregarSubtitulo(doc, "Detalle de Ventas");
        Table tablaVentas = new Table(UnitValue.createPercentArray(new float[]{15, 25, 25, 20, 15}))
                .useAllAvailableWidth();
        agregarCeldaEncabezado(tablaVentas, "N° Venta");
        agregarCeldaEncabezado(tablaVentas, "Empleado");
        agregarCeldaEncabezado(tablaVentas, "Cliente");
        agregarCeldaEncabezado(tablaVentas, "Fecha");
        agregarCeldaEncabezado(tablaVentas, "Monto");

        Map<Integer, String> nombreCliente = new HashMap<>();
        for (Clientes c : todosClientes)
            nombreCliente.put(c.getId(), c.getNombre());

        for (Ventas v : ventasFiltradas) {
            agregarCeldaDato(tablaVentas, String.valueOf(v.getNumero()));
            agregarCeldaDato(tablaVentas, nombreEmpleado.getOrDefault(v.getIdEmpleado(), "ID:" + v.getIdEmpleado()));
            agregarCeldaDato(tablaVentas, nombreCliente.getOrDefault(v.getIdClientes(), "ID:" + v.getIdClientes()));
            agregarCeldaDato(tablaVentas, v.getFecha() != null ? v.getFecha().format(FORMATO_FECHA) : "—");
            agregarCeldaDato(tablaVentas, String.format("$%.2f", v.getMonto()));
        }
        doc.add(tablaVentas);

        agregarPiePagina(doc);
        doc.close();
    }

    // ══════════════════════════════════════════════════════════
    //  REPORTE EMPLEADOS
    // ══════════════════════════════════════════════════════════
    public void generarReporteEmpleados(String rutaArchivo) throws Exception {
        List<Empleados> empleados    = empleadosAPI.obtenerTodos();
        List<Ventas>    todasVentas  = ventasAPI.obtenerTodos();

        // Ventas por empleado
        Map<Integer, Long>   cantVentasPorEmp = new HashMap<>();
        Map<Integer, Double> montoPorEmp      = new HashMap<>();
        for (Ventas v : todasVentas) {
            cantVentasPorEmp.merge(v.getIdEmpleado(), 1L, Long::sum);
            montoPorEmp.merge(v.getIdEmpleado(), v.getMonto(), Double::sum);
        }

        PdfWriter   writer = new PdfWriter(rutaArchivo);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf);

        agregarEncabezado(doc, "Reporte de Empleados",
                "Generado el " + LocalDateTime.now().format(FORMATO_FECHA));

        // Resumen
        agregarSubtitulo(doc, "Resumen General");
        Table resumen = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();
        agregarCeldaResumen(resumen, "Total de empleados registrados", String.valueOf(empleados.size()));

        Empleados topVendedor = empleados.stream()
                .max(Comparator.comparingLong(e ->
                        cantVentasPorEmp.getOrDefault(e.getId(), 0L)))
                .orElse(null);
        agregarCeldaResumen(resumen, "Empleado con más ventas",
                topVendedor == null ? "N/A" : topVendedor.getNombre()
                        + " (" + cantVentasPorEmp.getOrDefault(topVendedor.getId(), 0L) + " ventas)");
        doc.add(resumen);
        doc.add(new Paragraph("\n"));

        // Tabla empleados
        agregarSubtitulo(doc, "Lista de Empleados y Rendimiento");
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{25, 20, 15, 20, 20}))
                .useAllAvailableWidth();
        agregarCeldaEncabezado(tabla, "Nombre");
        agregarCeldaEncabezado(tabla, "Puesto");
        agregarCeldaEncabezado(tabla, "Edad");
        agregarCeldaEncabezado(tabla, "N° Ventas");
        agregarCeldaEncabezado(tabla, "Monto Generado");

        for (Empleados e : empleados) {
            agregarCeldaDato(tabla, e.getNombre());
            agregarCeldaDato(tabla, e.getPuesto());
            agregarCeldaDato(tabla, String.valueOf(e.getEdad()));
            agregarCeldaDato(tabla, String.valueOf(cantVentasPorEmp.getOrDefault(e.getId(), 0L)));
            agregarCeldaDato(tabla, String.format("$%.2f", montoPorEmp.getOrDefault(e.getId(), 0.0)));
        }
        doc.add(tabla);
        doc.add(new Paragraph("\n"));

        // Gráfica
        if (!cantVentasPorEmp.isEmpty()) {
            agregarSubtitulo(doc, "Gráfica — Ventas por Empleado");
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Empleados e : empleados) {
                dataset.addValue(cantVentasPorEmp.getOrDefault(e.getId(), 0L),
                        "Ventas", e.getNombre());
            }
            JFreeChart grafica = ChartFactory.createBarChart(
                    "", "Empleado", "N° Ventas",
                    dataset, PlotOrientation.VERTICAL, false, false, false);
            estilizarGrafica(grafica);
            doc.add(chartToImage(grafica, 500, 260));
        }

        agregarPiePagina(doc);
        doc.close();
    }

    // ══════════════════════════════════════════════════════════
    //  REPORTE CLIENTES
    // ══════════════════════════════════════════════════════════
    public void generarReporteClientes(String rutaArchivo) throws Exception {
        List<Clientes> clientes    = clientesAPI.obtenerTodos();
        List<Ventas>   todasVentas = ventasAPI.obtenerTodos();

        Map<Integer, Long>   comprasPorCliente = new HashMap<>();
        Map<Integer, Double> gastoPorCliente   = new HashMap<>();
        for (Ventas v : todasVentas) {
            comprasPorCliente.merge(v.getIdClientes(), 1L, Long::sum);
            gastoPorCliente.merge(v.getIdClientes(), v.getMonto(), Double::sum);
        }

        PdfWriter   writer = new PdfWriter(rutaArchivo);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf);

        agregarEncabezado(doc, "Reporte de Clientes",
                "Generado el " + LocalDateTime.now().format(FORMATO_FECHA));

        // Resumen
        agregarSubtitulo(doc, "Resumen General");
        Table resumen = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();
        agregarCeldaResumen(resumen, "Total de clientes registrados", String.valueOf(clientes.size()));

        Clientes clienteTop = clientes.stream()
                .max(Comparator.comparingLong(c ->
                        comprasPorCliente.getOrDefault(c.getId(), 0L)))
                .orElse(null);
        agregarCeldaResumen(resumen, "Cliente más frecuente",
                clienteTop == null ? "N/A" : clienteTop.getNombre()
                        + " (" + comprasPorCliente.getOrDefault(clienteTop.getId(), 0L) + " compras)");

        Clientes clienteTopGasto = clientes.stream()
                .max(Comparator.comparingDouble(c ->
                        gastoPorCliente.getOrDefault(c.getId(), 0.0)))
                .orElse(null);
        agregarCeldaResumen(resumen, "Cliente con mayor gasto",
                clienteTopGasto == null ? "N/A" : clienteTopGasto.getNombre()
                        + String.format(" ($%.2f)", gastoPorCliente.getOrDefault(clienteTopGasto.getId(), 0.0)));
        doc.add(resumen);
        doc.add(new Paragraph("\n"));

        // Tabla
        agregarSubtitulo(doc, "Lista de Clientes y Actividad");
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{30, 20, 20, 15, 15}))
                .useAllAvailableWidth();
        agregarCeldaEncabezado(tabla, "Nombre");
        agregarCeldaEncabezado(tabla, "RFC");
        agregarCeldaEncabezado(tabla, "Teléfono");
        agregarCeldaEncabezado(tabla, "N° Compras");
        agregarCeldaEncabezado(tabla, "Total Gastado");

        clientes.stream()
                .sorted(Comparator.comparingDouble(
                        (Clientes c) -> gastoPorCliente.getOrDefault(c.getId(), 0.0)).reversed())
                .forEach(c -> {
                    agregarCeldaDato(tabla, c.getNombre());
                    agregarCeldaDato(tabla, c.getRfc());
                    agregarCeldaDato(tabla, c.getTelefono() != null ? c.getTelefono() : "—");
                    agregarCeldaDato(tabla, String.valueOf(comprasPorCliente.getOrDefault(c.getId(), 0L)));
                    agregarCeldaDato(tabla, String.format("$%.2f", gastoPorCliente.getOrDefault(c.getId(), 0.0)));
                });
        doc.add(tabla);

        agregarPiePagina(doc);
        doc.close();
    }

    // ══════════════════════════════════════════════════════════
    //  REPORTE PRODUCTOS
    // ══════════════════════════════════════════════════════════
    public void generarReporteProductos(String rutaArchivo) throws Exception {
        List<Productos> productos    = ProductosAPI.obtenerTodos();
        List<Ventas>    todasVentas  = ventasAPI.obtenerTodos();

        Map<Integer, Integer> vendidosPorProducto = new HashMap<>();
        for (Ventas v : todasVentas) {
            try {
                List<VentasProductos> detalles = ventasProductosAPI.obtenerPorVenta(v.getNumero());
                for (VentasProductos d : detalles) {
                    vendidosPorProducto.merge(d.getClaveProducto(),
                            d.getCantidad() != null ? d.getCantidad() : 0, Integer::sum);
                }
            } catch (Exception ignored) {}
        }

        PdfWriter   writer = new PdfWriter(rutaArchivo);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf);

        agregarEncabezado(doc, "Reporte de Productos",
                "Generado el " + LocalDateTime.now().format(FORMATO_FECHA));

        // Resumen
        agregarSubtitulo(doc, "Resumen General");
        Table resumen = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();
        agregarCeldaResumen(resumen, "Total de productos en catálogo", String.valueOf(productos.size()));

        Productos topVendido = productos.stream()
                .max(Comparator.comparingInt(p ->
                        vendidosPorProducto.getOrDefault(p.getClave(), 0)))
                .orElse(null);
        agregarCeldaResumen(resumen, "Producto más vendido",
                topVendido == null ? "N/A" : topVendido.getNombre()
                        + " (" + vendidosPorProducto.getOrDefault(topVendido.getClave(), 0) + " uds.)");

        long sinMovimiento = productos.stream()
                .filter(p -> vendidosPorProducto.getOrDefault(p.getClave(), 0) == 0)
                .count();
        agregarCeldaResumen(resumen, "Productos sin ventas registradas", String.valueOf(sinMovimiento));
        doc.add(resumen);
        doc.add(new Paragraph("\n"));

        // Tabla
        agregarSubtitulo(doc, "Catálogo de Productos");
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{25, 15, 15, 15, 15, 15}))
                .useAllAvailableWidth();
        agregarCeldaEncabezado(tabla, "Nombre");
        agregarCeldaEncabezado(tabla, "Marca");
        agregarCeldaEncabezado(tabla, "Inventario");
        agregarCeldaEncabezado(tabla, "Precio");
        agregarCeldaEncabezado(tabla, "Costo");
        agregarCeldaEncabezado(tabla, "Uds. Vendidas");

        productos.stream()
                .sorted(Comparator.comparingInt(
                        (Productos p) -> vendidosPorProducto.getOrDefault(p.getClave(), 0)).reversed())
                .forEach(p -> {
                    agregarCeldaDato(tabla, p.getNombre());
                    agregarCeldaDato(tabla, p.getMarca());
                    agregarCeldaDato(tabla, String.valueOf(p.getInventario()));
                    agregarCeldaDato(tabla, String.format("$%.2f", p.getPrecio()));
                    agregarCeldaDato(tabla, String.format("$%.2f", p.getCosto()));
                    agregarCeldaDato(tabla, String.valueOf(vendidosPorProducto.getOrDefault(p.getClave(), 0)));
                });
        doc.add(tabla);
        doc.add(new Paragraph("\n"));

        // Gráfica top 10
        agregarSubtitulo(doc, "Gráfica — Top 10 Productos Más Vendidos");
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        productos.stream()
                .filter(p -> vendidosPorProducto.getOrDefault(p.getClave(), 0) > 0)
                .sorted(Comparator.comparingInt(
                        (Productos p) -> vendidosPorProducto.getOrDefault(p.getClave(), 0)).reversed())
                .limit(10)
                .forEach(p -> dataset.addValue(
                        vendidosPorProducto.getOrDefault(p.getClave(), 0),
                        "Vendidos", p.getNombre()));

        if (dataset.getRowCount() > 0) {
            JFreeChart grafica = ChartFactory.createBarChart(
                    "", "Producto", "Unidades",
                    dataset, PlotOrientation.VERTICAL, false, false, false);
            estilizarGrafica(grafica);
            doc.add(chartToImage(grafica, 500, 260));
        }

        agregarPiePagina(doc);
        doc.close();
    }

    // ══════════════════════════════════════════════════════════
    //  REPORTE PROVEEDORES
    // ══════════════════════════════════════════════════════════
    public void generarReporteProveedores(String rutaArchivo) throws Exception {
        ProveedoresAPI proveedoresAPI = new ProveedoresAPI();
        ProductosAPI   productosAPI   = new ProductosAPI();

        List<Proveedores>        proveedores  = proveedoresAPI.obtenerTodos();
        List<Productos>          productos    = ProductosAPI.obtenerTodos();
        ProductoProveedorAPI     ppAPI        = new ProductoProveedorAPI();
        List<ProductosProveedores> relaciones = ppAPI.obtenerTodas();

        Map<Integer, List<Integer>> productosPorProveedor = new HashMap<>();
        for (ProductosProveedores pp : relaciones) {
            productosPorProveedor
                    .computeIfAbsent(pp.getIdProveedor(), k -> new ArrayList<>())
                    .add(pp.getClaveProducto());
        }

        Map<Integer, String> nombreProducto = new HashMap<>();
        for (Productos p : productos)
            nombreProducto.put(p.getClave(), p.getNombre());

        PdfWriter   writer = new PdfWriter(rutaArchivo);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf);

        agregarEncabezado(doc, "Reporte de Proveedores",
                "Generado el " + LocalDateTime.now().format(FORMATO_FECHA));

        // Resumen
        agregarSubtitulo(doc, "Resumen General");
        Table resumen = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();
        agregarCeldaResumen(resumen, "Total de proveedores registrados", String.valueOf(proveedores.size()));

        Proveedores topProveedor = proveedores.stream()
                .max(Comparator.comparingInt(pv ->
                        productosPorProveedor.getOrDefault(pv.getId(), new ArrayList<>()).size()))
                .orElse(null);
        agregarCeldaResumen(resumen, "Proveedor con más productos",
                topProveedor == null ? "N/A" : topProveedor.getNombre()
                        + " (" + productosPorProveedor.getOrDefault(topProveedor.getId(), new ArrayList<>()).size() + " productos)");
        doc.add(resumen);
        doc.add(new Paragraph("\n"));

        // Tabla
        agregarSubtitulo(doc, "Lista de Proveedores y Productos que Surten");
        for (Proveedores pv : proveedores) {
            Table tablaProveedor = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth();
            Cell encabezadoProv = new Cell()
                    .add(new Paragraph(pv.getNombre() + "  |  RFC: " + pv.getRfc()
                            + "  |  Tel: " + (pv.getTelefono() != null ? pv.getTelefono() : "—"))
                            .setFontColor(ColorConstants.WHITE)
                            .setBold()
                            .setFontSize(10))
                    .setBackgroundColor(COLOR_MORADO_ACENTO)
                    .setPadding(6);
            tablaProveedor.addCell(encabezadoProv);

            List<Integer> clavesProductos =
                    productosPorProveedor.getOrDefault(pv.getId(), new ArrayList<>());
            if (clavesProductos.isEmpty()) {
                tablaProveedor.addCell(new Cell()
                        .add(new Paragraph("Sin productos asignados")
                                .setFontColor(COLOR_TEXTO_GRIS).setFontSize(9))
                        .setPadding(5));
            } else {
                String lista = clavesProductos.stream()
                        .map(cl -> nombreProducto.getOrDefault(cl, "ID:" + cl))
                        .collect(Collectors.joining(", "));
                tablaProveedor.addCell(new Cell()
                        .add(new Paragraph(lista).setFontSize(9))
                        .setPadding(5));
            }
            doc.add(tablaProveedor);
            doc.add(new Paragraph("\n").setFontSize(4));
        }

        agregarPiePagina(doc);
        doc.close();
    }

    // ══════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ══════════════════════════════════════════════════════════
    private void agregarEncabezado(Document doc, String titulo, String subtitulo) {
        // Banda morada oscura
        Table banda = new Table(UnitValue.createPercentArray(new float[]{100}))
                .useAllAvailableWidth();
        Cell celdaBanda = new Cell()
                .add(new Paragraph("NANOSIP")
                        .setFontSize(28).setBold()
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph(titulo)
                        .setFontSize(16)
                        .setFontColor(new DeviceRgb(202, 196, 206))
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(COLOR_MORADO_OSCURO)
                .setPadding(20)
                .setBorder(null);
        banda.addCell(celdaBanda);
        doc.add(banda);

        // Subtítulo (fechas o fecha generación)
        doc.add(new Paragraph(subtitulo)
                .setFontSize(10)
                .setFontColor(COLOR_TEXTO_GRIS)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(6).setMarginBottom(14));
    }

    private void agregarSubtitulo(Document doc, String texto) {
        doc.add(new Paragraph(texto)
                .setFontSize(13).setBold()
                .setFontColor(COLOR_MORADO_OSCURO)
                .setMarginBottom(6)
                .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(COLOR_MORADO_ACENTO, 1.5f)));
    }

    private void agregarCeldaEncabezado(Table tabla, String texto) {
        tabla.addHeaderCell(new Cell()
                .add(new Paragraph(texto).setBold().setFontSize(10)
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(COLOR_MORADO_ACENTO)
                .setPadding(7)
                .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(COLOR_MORADO_OSCURO, 1)));
    }

    private void agregarCeldaDato(Table tabla, String texto) {
        tabla.addCell(new Cell()
                .add(new Paragraph(texto != null ? texto : "—").setFontSize(9))
                .setPadding(6)
                .setBackgroundColor(tabla.getNumberOfRows() % 2 == 0
                        ? COLOR_MORADO_CLARO : ColorConstants.WHITE));
    }

    private void agregarCeldaResumen(Table tabla, String etiqueta, String valor) {
        tabla.addCell(new Cell()
                .add(new Paragraph(etiqueta).setFontSize(10)
                        .setFontColor(COLOR_TEXTO_GRIS))
                .setPadding(8).setBorder(null)
                .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(COLOR_MORADO_CLARO, 1)));
        tabla.addCell(new Cell()
                .add(new Paragraph(valor).setFontSize(11).setBold()
                        .setFontColor(COLOR_MORADO_ACENTO))
                .setPadding(8).setBorder(null)
                .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(COLOR_MORADO_CLARO, 1)));
    }

    private void agregarPiePagina(Document doc) {
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Generado por Nanosip  •  " + LocalDateTime.now().format(FORMATO_FECHA))
                .setFontSize(8).setFontColor(COLOR_TEXTO_GRIS)
                .setTextAlignment(TextAlignment.RIGHT));
    }

    private void estilizarGrafica(JFreeChart chart) {
        chart.setBackgroundPaint(java.awt.Color.WHITE);
        var plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new java.awt.Color(245, 243, 250));
        plot.setRangeGridlinePaint(new java.awt.Color(226, 222, 238));
        var renderer = (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new java.awt.Color(114, 90, 193));
        renderer.setShadowVisible(false);
        plot.getDomainAxis().setTickLabelFont(
                new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
    }

    private Image chartToImage(JFreeChart chart, int width, int height) throws Exception {
        BufferedImage      img    = chart.createBufferedImage(width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return new Image(ImageDataFactory.create(baos.toByteArray()))
                .setWidth(UnitValue.createPercentValue(100));
    }
}