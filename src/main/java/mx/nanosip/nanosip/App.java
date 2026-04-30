package mx.nanosip.nanosip;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    private static Stage stage;

    @Override
    public void start(Stage stage) throws IOException {
        App.stage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("Login.fxml"));
        scene = new Scene(fxmlLoader.load());

        stage.setTitle("Nanosip");
        stage.setScene(scene);
        stage.show();
    }

    // metodo para cambiar de ventana
    public static void setRoot(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
        scene.setRoot(loader.load());
        stage.sizeToScene();
    }

    public static Stage getStage() {
        return stage;
    }
}
