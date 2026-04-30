package mx.nanosip.nanosip;

import javafx.scene.Node;
import javafx.stage.Stage;

public class WindowDragUtil {

    private WindowDragUtil() {} // no instanciable

    /**
     * Permite arrastrar la ventana tomando como "asa" el nodo indicado.
     * Llama esto en initialize() de cada controller:
     *   WindowDragUtil.enable(topbar, App.getStage());
     */
    public static void enable(Node handle, Stage stage) {
        final double[] offset = new double[2];

        handle.setOnMousePressed(e -> {
            offset[0] = e.getSceneX();
            offset[1] = e.getSceneY();
        });

        handle.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - offset[0]);
            stage.setY(e.getScreenY() - offset[1]);
        });
    }
}