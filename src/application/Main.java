package application;

import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    FXMLLoader fxmlLoader;

    @Override
    public void start(Stage primaryStage) {
        try {
            Platform.setImplicitExit(true);

            URL location = getClass().getResource("Layout.fxml");
            fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(location);
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
            Parent root = (Parent) fxmlLoader.load(location.openStream());
            Scene scene = new Scene(root, 650, 450);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        LayoutController controller = ((LayoutController) fxmlLoader.getController());
        controller.stop();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
