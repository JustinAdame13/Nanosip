package mx.nanosip.nanosip.Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import mx.nanosip.nanosip.Controllers.Backend.ReporteService;
import mx.nanosip.nanosip.Controllers.Modals.ReporteFechasController;
import mx.nanosip.nanosip.WindowDragUtil;

import java.io.File;

public class ReporteHelper {

    private static final ReporteService servicio = new ReporteService();

    // ── Ventas (con selector de fechas) ──────────────────────
    public static void generarVentas(Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ReporteHelper.class.getResource(
                            "/mx/nanosip/nanosip/ReporteFechas.fxml"));
            Parent root = loader.load();
            ReporteFechasController ctrl = loader.getController();

            Stage modal = new Stage();
            modal.initStyle(StageStyle.TRANSPARENT);
            modal.initOwner(owner);
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modal.setScene(scene);
            ctrl.setModalStage(modal);

            javafx.scene.Node topbarNode = root.lookup("#topbar");
            if (topbarNode != null) WindowDragUtil.enable(topbarNode, modal);

            ctrl.setOnConfirmar((inicio, fin) ->
                    elegirArchivoYGenerar(owner, "reporte_ventas.pdf", ruta -> {
                        try { servicio.generarReporteVentas(ruta, inicio, fin); }
                        catch (Exception e) { mostrarError(e); }
                    }));

            modal.showAndWait();
        } catch (Exception e) { mostrarError(e); }
    }

    // ── Empleados ────────────────────────────────────────────
    public static void generarEmpleados(Stage owner) {
        elegirArchivoYGenerar(owner, "reporte_empleados.pdf", ruta -> {
            try { servicio.generarReporteEmpleados(ruta); }
            catch (Exception e) { mostrarError(e); }
        });
    }

    // ── Clientes ─────────────────────────────────────────────
    public static void generarClientes(Stage owner) {
        elegirArchivoYGenerar(owner, "reporte_clientes.pdf", ruta -> {
            try { servicio.generarReporteClientes(ruta); }
            catch (Exception e) { mostrarError(e); }
        });
    }

    // ── Productos ────────────────────────────────────────────
    public static void generarProductos(Stage owner) {
        elegirArchivoYGenerar(owner, "reporte_productos.pdf", ruta -> {
            try { servicio.generarReporteProductos(ruta); }
            catch (Exception e) { mostrarError(e); }
        });
    }

    // ── Proveedores ──────────────────────────────────────────
    public static void generarProveedores(Stage owner) {
        elegirArchivoYGenerar(owner, "reporte_proveedores.pdf", ruta -> {
            try { servicio.generarReporteProveedores(ruta); }
            catch (Exception e) { mostrarError(e); }
        });
    }

    // ── Helper: FileChooser ──────────────────────────────────
    private static void elegirArchivoYGenerar(Stage owner,
                                              String nombreDefault,
                                              java.util.function.Consumer<String> generador) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar reporte PDF");
        chooser.setInitialFileName(nombreDefault);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        File archivo = chooser.showSaveDialog(owner);
        if (archivo != null) generador.accept(archivo.getAbsolutePath());
    }

    private static void mostrarError(Exception e) {
        System.err.println("Error generando reporte: " + e.getMessage());
        e.printStackTrace();
    }
}